"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ReciclajeStack = void 0;
const cdk = require("aws-cdk-lib");
const ec2 = require("aws-cdk-lib/aws-ec2");
const s3 = require("aws-cdk-lib/aws-s3");
const iam = require("aws-cdk-lib/aws-iam");
const fs = require("fs");
const path = require("path");
const os = require("os");
class ReciclajeStack extends cdk.Stack {
    constructor(scope, id, props) {
        super(scope, id, props);
        // 1. Bucket S3 para Almacenamiento de Fotos de Inspección
        const fotosBucket = new s3.Bucket(this, 'ReciclajeFotosBucket', {
            bucketName: `reciclaje-litoral-fotos-${this.account}-${this.region}`,
            removalPolicy: cdk.RemovalPolicy.DESTROY,
            autoDeleteObjects: true,
            cors: [
                {
                    allowedMethods: [
                        s3.HttpMethods.GET,
                        s3.HttpMethods.PUT,
                        s3.HttpMethods.POST,
                        s3.HttpMethods.DELETE,
                    ],
                    allowedOrigins: ['*'],
                    allowedHeaders: ['*'],
                },
            ],
        });
        // 2. VPC (Red Privada Virtual - Subredes Públicas únicamente para eliminar costos de NAT Gateway)
        const vpc = new ec2.Vpc(this, 'ReciclajeVpc', {
            maxAzs: 1,
            natGateways: 0,
            subnetConfiguration: [
                {
                    cidrMask: 24,
                    name: 'PublicSubnet',
                    subnetType: ec2.SubnetType.PUBLIC,
                },
            ],
        });
        // 3. Grupo de Seguridad (Security Group)
        const ec2SecurityGroup = new ec2.SecurityGroup(this, 'ReciclajeEc2Sg', {
            vpc,
            description: 'Permitir trafico HTTP, HTTPS y SSH hacia la app Reciclaje Litoral',
            allowAllOutbound: true,
        });
        ec2SecurityGroup.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(80), 'Permitir trafico Web HTTP');
        ec2SecurityGroup.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(443), 'Permitir trafico Web HTTPS');
        ec2SecurityGroup.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(22), 'Permitir acceso SSH');
        // 4. Rol de IAM para la Instancia EC2 con Permisos hacia S3 y SSM Session Manager
        const ec2Role = new iam.Role(this, 'ReciclajeEc2Role', {
            assumedBy: new iam.ServicePrincipal('ec2.amazonaws.com'),
            description: 'Rol de EC2 con acceso a S3 y SSM Session Manager',
        });
        fotosBucket.grantReadWrite(ec2Role);
        ec2Role.addManagedPolicy(iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonSSMManagedInstanceCore'));
        // Permisos para leer y descifrar parámetros de configuración desde AWS SSM Parameter Store
        ec2Role.addToPolicy(new iam.PolicyStatement({
            actions: [
                'ssm:GetParameters',
                'ssm:GetParameter',
                'ssm:GetParametersByPath',
                'kms:Decrypt',
            ],
            resources: [
                `arn:aws:ssm:${this.region}:${this.account}:parameter/reciclaje-app/*`,
            ],
        }));
        // 5. Script de Inicialización (UserData): Instala Docker, Docker Compose y prepara el despliegue automático
        const userData = ec2.UserData.forLinux();
        userData.addCommands('sudo dnf update -y', 'sudo dnf install -y git docker', 'sudo mkdir -p /usr/local/lib/docker/cli-plugins', 'sudo curl -SL https://github.com/docker/compose/releases/download/v2.24.5/docker-compose-linux-aarch64 -o /usr/local/lib/docker/cli-plugins/docker-compose', 'sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose', 'sudo ln -sf /usr/local/lib/docker/cli-plugins/docker-compose /usr/local/bin/docker-compose', 'sudo systemctl enable --now docker', 'sudo usermod -aG docker ec2-user', 'git config --system --add safe.directory /home/ec2-user/reciclaje-app', 'mkdir -p /home/ec2-user', 'cd /home/ec2-user', 'if [ ! -d "reciclaje-app" ]; then', '  git clone https://github.com/cnegronr/reciclaje-app reciclaje-app', 'fi', 'cd /home/ec2-user/reciclaje-app', 'echo "Obteniendo variables de entorno desde AWS SSM Parameter Store..."', `aws ssm get-parameters-by-path --region ${this.region} --path "/reciclaje-app/prod/" --with-decryption --query "Parameters[*].[Name,Value]" --output text | while IFS="$(printf '\\t')" read -r name val; do echo "\${name##*/}=\$val"; done > /home/ec2-user/reciclaje-app/.env`, 'chmod 600 /home/ec2-user/reciclaje-app/.env', 'chown -R ec2-user:ec2-user /home/ec2-user/reciclaje-app', 'sudo -u ec2-user -i sh -c "cd /home/ec2-user/reciclaje-app && docker compose up -d --build"');
        // Detectar e inyectar automáticamente la clave pública local (~/.ssh/id_ed25519.pub o ~/.ssh/id_rsa.pub)
        let localPublicKey = '';
        try {
            const ed25519Path = path.join(os.homedir(), '.ssh', 'id_ed25519.pub');
            const rsaPath = path.join(os.homedir(), '.ssh', 'id_rsa.pub');
            if (fs.existsSync(ed25519Path)) {
                localPublicKey = fs.readFileSync(ed25519Path, 'utf8').trim();
            }
            else if (fs.existsSync(rsaPath)) {
                localPublicKey = fs.readFileSync(rsaPath, 'utf8').trim();
            }
        }
        catch (e) {
            console.warn('No se pudo leer la clave publica local en ~/.ssh/', e);
        }
        if (localPublicKey) {
            userData.addCommands('mkdir -p /home/ec2-user/.ssh', 'chmod 700 /home/ec2-user/.ssh', `echo "${localPublicKey}" >> /home/ec2-user/.ssh/authorized_keys`, 'chmod 600 /home/ec2-user/.ssh/authorized_keys', 'chown -R ec2-user:ec2-user /home/ec2-user/.ssh');
        }
        // Obtener opcionalmente el KeyPair name desde contexto de CDK: cdk deploy -c keyName=mi-llave-ssh
        const keyName = this.node.tryGetContext('keyName');
        // 6. Instancia EC2 Graviton (t4g.small: 2 vCPU ARM64, 2 GB RAM - ~$6-$8 USD/mes)
        const ec2Instance = new ec2.Instance(this, 'ReciclajeEc2Instance', {
            vpc,
            instanceType: ec2.InstanceType.of(ec2.InstanceClass.T4G, ec2.InstanceSize.SMALL),
            machineImage: ec2.MachineImage.latestAmazonLinux2023({
                cpuType: ec2.AmazonLinuxCpuType.ARM_64,
            }),
            securityGroup: ec2SecurityGroup,
            role: ec2Role,
            userData: userData,
            vpcSubnets: { subnetType: ec2.SubnetType.PUBLIC },
            ...(keyName ? { keyName } : {}),
        });
        // 7. Dirección IP Elástica (Elastic IP) fija
        const eip = new ec2.CfnEIP(this, 'ReciclajeElasticIP', {
            instanceId: ec2Instance.instanceId,
        });
        // Outputs de la Infraestructura
        new cdk.CfnOutput(this, 'BucketNameOutput', {
            value: fotosBucket.bucketName,
            description: 'Nombre del Bucket S3 para Fotos de Inspección',
        });
        new cdk.CfnOutput(this, 'InstanceIdOutput', {
            value: ec2Instance.instanceId,
            description: 'ID de la Instancia EC2 Graviton (para AWS SSM)',
        });
        new cdk.CfnOutput(this, 'PublicIpOutput', {
            value: eip.ref,
            description: 'IP Publica Elástica de la Instancia EC2 Graviton',
        });
        new cdk.CfnOutput(this, 'AppUrlOutput', {
            value: `http://${eip.ref}`,
            description: 'URL de acceso directo a la Aplicacion Web de Reciclaje Litoral',
        });
    }
}
exports.ReciclajeStack = ReciclajeStack;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoicmVjaWNsYWplLXN0YWNrLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXMiOlsicmVjaWNsYWplLXN0YWNrLnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiI7OztBQUFBLG1DQUFtQztBQUNuQywyQ0FBMkM7QUFDM0MseUNBQXlDO0FBQ3pDLDJDQUEyQztBQUUzQyx5QkFBeUI7QUFDekIsNkJBQTZCO0FBQzdCLHlCQUF5QjtBQUV6QixNQUFhLGNBQWUsU0FBUSxHQUFHLENBQUMsS0FBSztJQUMzQyxZQUFZLEtBQWdCLEVBQUUsRUFBVSxFQUFFLEtBQXNCO1FBQzlELEtBQUssQ0FBQyxLQUFLLEVBQUUsRUFBRSxFQUFFLEtBQUssQ0FBQyxDQUFDO1FBRXhCLDBEQUEwRDtRQUMxRCxNQUFNLFdBQVcsR0FBRyxJQUFJLEVBQUUsQ0FBQyxNQUFNLENBQUMsSUFBSSxFQUFFLHNCQUFzQixFQUFFO1lBQzlELFVBQVUsRUFBRSwyQkFBMkIsSUFBSSxDQUFDLE9BQU8sSUFBSSxJQUFJLENBQUMsTUFBTSxFQUFFO1lBQ3BFLGFBQWEsRUFBRSxHQUFHLENBQUMsYUFBYSxDQUFDLE9BQU87WUFDeEMsaUJBQWlCLEVBQUUsSUFBSTtZQUN2QixJQUFJLEVBQUU7Z0JBQ0o7b0JBQ0UsY0FBYyxFQUFFO3dCQUNkLEVBQUUsQ0FBQyxXQUFXLENBQUMsR0FBRzt3QkFDbEIsRUFBRSxDQUFDLFdBQVcsQ0FBQyxHQUFHO3dCQUNsQixFQUFFLENBQUMsV0FBVyxDQUFDLElBQUk7d0JBQ25CLEVBQUUsQ0FBQyxXQUFXLENBQUMsTUFBTTtxQkFDdEI7b0JBQ0QsY0FBYyxFQUFFLENBQUMsR0FBRyxDQUFDO29CQUNyQixjQUFjLEVBQUUsQ0FBQyxHQUFHLENBQUM7aUJBQ3RCO2FBQ0Y7U0FDRixDQUFDLENBQUM7UUFFSCxrR0FBa0c7UUFDbEcsTUFBTSxHQUFHLEdBQUcsSUFBSSxHQUFHLENBQUMsR0FBRyxDQUFDLElBQUksRUFBRSxjQUFjLEVBQUU7WUFDNUMsTUFBTSxFQUFFLENBQUM7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RCx3Q0FvS0MiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgKiBhcyBjZGsgZnJvbSAnYXdzLWNkay1saWInO1xuaW1wb3J0ICogYXMgZWMyIGZyb20gJ2F3cy1jZGstbGliL2F3cy1lYzInO1xuaW1wb3J0ICogYXMgczMgZnJvbSAnYXdzLWNkay1saWIvYXdzLXMzJztcbmltcG9ydCAqIGFzIGlhbSBmcm9tICdhd3MtY2RrLWxpYi9hd3MtaWFtJztcbmltcG9ydCB7IENvbnN0cnVjdCB9IGZyb20gJ2NvbnN0cnVjdHMnO1xuaW1wb3J0ICogYXMgZnMgZnJvbSAnZnMnO1xuaW1wb3J0ICogYXMgcGF0aCBmcm9tICdwYXRoJztcbmltcG9ydCAqIGFzIG9zIGZyb20gJ29zJztcblxuZXhwb3J0IGNsYXNzIFJlY2ljbGFqZVN0YWNrIGV4dGVuZHMgY2RrLlN0YWNrIHtcbiAgY29uc3RydWN0b3Ioc2NvcGU6IENvbnN0cnVjdCwgaWQ6IHN0cmluZywgcHJvcHM/OiBjZGsuU3RhY2tQcm9wcykge1xuICAgIHN1cGVyKHNjb3BlLCBpZCwgcHJvcHMpO1xuXG4gICAgLy8gMS4gQnVja2V0IFMzIHBhcmEgQWxtYWNlbmFtaWVudG8gZGUgRm90b3MgZGUgSW5zcGVjY2nDs25cbiAgICBjb25zdCBmb3Rvc0J1Y2tldCA9IG5ldyBzMy5CdWNrZXQodGhpcywgJ1JlY2ljbGFqZUZvdG9zQnVja2V0Jywge1xuICAgICAgYnVja2V0TmFtZTogYHJlY2ljbGFqZS1saXRvcmFsLWZvdG9zLSR7dGhpcy5hY2NvdW50fS0ke3RoaXMucmVnaW9ufWAsXG4gICAgICByZW1vdmFsUG9saWN5OiBjZGsuUmVtb3ZhbFBvbGljeS5ERVNUUk9ZLFxuICAgICAgYXV0b0RlbGV0ZU9iamVjdHM6IHRydWUsXG4gICAgICBjb3JzOiBbXG4gICAgICAgIHtcbiAgICAgICAgICBhbGxvd2VkTWV0aG9kczogW1xuICAgICAgICAgICAgczMuSHR0cE1ldGhvZHMuR0VULFxuICAgICAgICAgICAgczMuSHR0cE1ldGhvZHMuUFVULFxuICAgICAgICAgICAgczMuSHR0cE1ldGhvZHMuUE9TVCxcbiAgICAgICAgICAgIHMzLkh0dHBNZXRob2RzLkRFTEVURSxcbiAgICAgICAgICBdLFxuICAgICAgICAgIGFsbG93ZWRPcmlnaW5zOiBbJyonXSxcbiAgICAgICAgICBhbGxvd2VkSGVhZGVyczogWycqJ10sXG4gICAgICAgIH0sXG4gICAgICBdLFxuICAgIH0pO1xuXG4gICAgLy8gMi4gVlBDIChSZWQgUHJpdmFkYSBWaXJ0dWFsIC0gU3VicmVkZXMgUMO6YmxpY2FzIMO6bmljYW1lbnRlIHBhcmEgZWxpbWluYXIgY29zdG9zIGRlIE5BVCBHYXRld2F5KVxuICAgIGNvbnN0IHZwYyA9IG5ldyBlYzIuVnBjKHRoaXMsICdSZWNpY2xhamVWcGMnLCB7XG4gICAgICBtYXhBenM6IDEsXG4gICAgICBuYXRHYXRld2F5czogMCxcbiAgICAgIHN1Ym5ldENvbmZpZ3VyYXRpb246IFtcbiAgICAgICAge1xuICAgICAgICAgIGNpZHJNYXNrOiAyNCxcbiAgICAgICAgICBuYW1lOiAnUHVibGljU3VibmV0JyxcbiAgICAgICAgICBzdWJuZXRUeXBlOiBlYzIuU3VibmV0VHlwZS5QVUJMSUMsXG4gICAgICAgIH0sXG4gICAgICBdLFxuICAgIH0pO1xuXG4gICAgLy8gMy4gR3J1cG8gZGUgU2VndXJpZGFkIChTZWN1cml0eSBHcm91cClcbiAgICBjb25zdCBlYzJTZWN1cml0eUdyb3VwID0gbmV3IGVjMi5TZWN1cml0eUdyb3VwKHRoaXMsICdSZWNpY2xhamVFYzJTZycsIHtcbiAgICAgIHZwYyxcbiAgICAgIGRlc2NyaXB0aW9uOiAnUGVybWl0aXIgdHJhZmljbyBIVFRQLCBIVFRQUyB5IFNTSCBoYWNpYSBsYSBhcHAgUmVjaWNsYWplIExpdG9yYWwnLFxuICAgICAgYWxsb3dBbGxPdXRib3VuZDogdHJ1ZSxcbiAgICB9KTtcblxuICAgIGVjMlNlY3VyaXR5R3JvdXAuYWRkSW5ncmVzc1J1bGUoZWMyLlBlZXIuYW55SXB2NCgpLCBlYzIuUG9ydC50Y3AoODApLCAnUGVybWl0aXIgdHJhZmljbyBXZWIgSFRUUCcpO1xuICAgIGVjMlNlY3VyaXR5R3JvdXAuYWRkSW5ncmVzc1J1bGUoZWMyLlBlZXIuYW55SXB2NCgpLCBlYzIuUG9ydC50Y3AoNDQzKSwgJ1Blcm1pdGlyIHRyYWZpY28gV2ViIEhUVFBTJyk7XG4gICAgZWMyU2VjdXJpdHlHcm91cC5hZGRJbmdyZXNzUnVsZShlYzIuUGVlci5hbnlJcHY0KCksIGVjMi5Qb3J0LnRjcCgyMiksICdQZXJtaXRpciBhY2Nlc28gU1NIJyk7XG5cbiAgICAvLyA0LiBSb2wgZGUgSUFNIHBhcmEgbGEgSW5zdGFuY2lhIEVDMiBjb24gUGVybWlzb3MgaGFjaWEgUzMgeSBTU00gU2Vzc2lvbiBNYW5hZ2VyXG4gICAgY29uc3QgZWMyUm9sZSA9IG5ldyBpYW0uUm9sZSh0aGlzLCAnUmVjaWNsYWplRWMyUm9sZScsIHtcbiAgICAgIGFzc3VtZWRCeTogbmV3IGlhbS5TZXJ2aWNlUHJpbmNpcGFsKCdlYzIuYW1hem9uYXdzLmNvbScpLFxuICAgICAgZGVzY3JpcHRpb246ICdSb2wgZGUgRUMyIGNvbiBhY2Nlc28gYSBTMyB5IFNTTSBTZXNzaW9uIE1hbmFnZXInLFxuICAgIH0pO1xuXG4gICAgZm90b3NCdWNrZXQuZ3JhbnRSZWFkV3JpdGUoZWMyUm9sZSk7XG4gICAgZWMyUm9sZS5hZGRNYW5hZ2VkUG9saWN5KGlhbS5NYW5hZ2VkUG9saWN5LmZyb21Bd3NNYW5hZ2VkUG9saWN5TmFtZSgnQW1hem9uU1NNTWFuYWdlZEluc3RhbmNlQ29yZScpKTtcblxuICAgIC8vIFBlcm1pc29zIHBhcmEgbGVlciB5IGRlc2NpZnJhciBwYXLDoW1ldHJvcyBkZSBjb25maWd1cmFjacOzbiBkZXNkZSBBV1MgU1NNIFBhcmFtZXRlciBTdG9yZVxuICAgIGVjMlJvbGUuYWRkVG9Qb2xpY3koXG4gICAgICBuZXcgaWFtLlBvbGljeVN0YXRlbWVudCh7XG4gICAgICAgIGFjdGlvbnM6IFtcbiAgICAgICAgICAnc3NtOkdldFBhcmFtZXRlcnMnLFxuICAgICAgICAgICdzc206R2V0UGFyYW1ldGVyJyxcbiAgICAgICAgICAnc3NtOkdldFBhcmFtZXRlcnNCeVBhdGgnLFxuICAgICAgICAgICdrbXM6RGVjcnlwdCcsXG4gICAgICAgIF0sXG4gICAgICAgIHJlc291cmNlczogW1xuICAgICAgICAgIGBhcm46YXdzOnNzbToke3RoaXMucmVnaW9ufToke3RoaXMuYWNjb3VudH06cGFyYW1ldGVyL3JlY2ljbGFqZS1hcHAvKmAsXG4gICAgICAgIF0sXG4gICAgICB9KVxuICAgICk7XG5cbiAgICAvLyA1LiBTY3JpcHQgZGUgSW5pY2lhbGl6YWNpw7NuIChVc2VyRGF0YSk6IEluc3RhbGEgRG9ja2VyLCBEb2NrZXIgQ29tcG9zZSB5IHByZXBhcmEgZWwgZGVzcGxpZWd1ZSBhdXRvbcOhdGljb1xuICAgIGNvbnN0IHVzZXJEYXRhID0gZWMyLlVzZXJEYXRhLmZvckxpbnV4KCk7XG4gICAgdXNlckRhdGEuYWRkQ29tbWFuZHMoXG4gICAgICAnc3VkbyBkbmYgdXBkYXRlIC15JyxcbiAgICAgICdzdWRvIGRuZiBpbnN0YWxsIC15IGdpdCBkb2NrZXInLFxuICAgICAgJ3N1ZG8gbWtkaXIgLXAgL3Vzci9sb2NhbC9saWIvZG9ja2VyL2NsaS1wbHVnaW5zJyxcbiAgICAgICdzdWRvIGN1cmwgLVNMIGh0dHBzOi8vZ2l0aHViLmNvbS9kb2NrZXIvY29tcG9zZS9yZWxlYXNlcy9kb3dubG9hZC92Mi4yNC41L2RvY2tlci1jb21wb3NlLWxpbnV4LWFhcmNoNjQgLW8gL3Vzci9sb2NhbC9saWIvZG9ja2VyL2NsaS1wbHVnaW5zL2RvY2tlci1jb21wb3NlJyxcbiAgICAgICdzdWRvIGNobW9kICt4IC91c3IvbG9jYWwvbGliL2RvY2tlci9jbGktcGx1Z2lucy9kb2NrZXItY29tcG9zZScsXG4gICAgICAnc3VkbyBsbiAtc2YgL3Vzci9sb2NhbC9saWIvZG9ja2VyL2NsaS1wbHVnaW5zL2RvY2tlci1jb21wb3NlIC91c3IvbG9jYWwvYmluL2RvY2tlci1jb21wb3NlJyxcbiAgICAgICdzdWRvIHN5c3RlbWN0bCBlbmFibGUgLS1ub3cgZG9ja2VyJyxcbiAgICAgICdzdWRvIHVzZXJtb2QgLWFHIGRvY2tlciBlYzItdXNlcicsXG4gICAgICAnZ2l0IGNvbmZpZyAtLXN5c3RlbSAtLWFkZCBzYWZlLmRpcmVjdG9yeSAvaG9tZS9lYzItdXNlci9yZWNpY2xhamUtYXBwJyxcbiAgICAgICdta2RpciAtcCAvaG9tZS9lYzItdXNlcicsXG4gICAgICAnY2QgL2hvbWUvZWMyLXVzZXInLFxuICAgICAgJ2lmIFsgISAtZCBcInJlY2ljbGFqZS1hcHBcIiBdOyB0aGVuJyxcbiAgICAgICcgIGdpdCBjbG9uZSBodHRwczovL2dpdGh1Yi5jb20vY25lZ3JvbnIvcmVjaWNsYWplLWFwcCByZWNpY2xhamUtYXBwJyxcbiAgICAgICdmaScsXG4gICAgICAnY2QgL2hvbWUvZWMyLXVzZXIvcmVjaWNsYWplLWFwcCcsXG4gICAgICAnZWNobyBcIk9idGVuaWVuZG8gdmFyaWFibGVzIGRlIGVudG9ybm8gZGVzZGUgQVdTIFNTTSBQYXJhbWV0ZXIgU3RvcmUuLi5cIicsXG4gICAgICBgYXdzIHNzbSBnZXQtcGFyYW1ldGVycy1ieS1wYXRoIC0tcmVnaW9uICR7dGhpcy5yZWdpb259IC0tcGF0aCBcIi9yZWNpY2xhamUtYXBwL3Byb2QvXCIgLS13aXRoLWRlY3J5cHRpb24gLS1xdWVyeSBcIlBhcmFtZXRlcnNbKl0uW05hbWUsVmFsdWVdXCIgLS1vdXRwdXQgdGV4dCB8IHdoaWxlIElGUz1cIiQocHJpbnRmICdcXFxcdCcpXCIgcmVhZCAtciBuYW1lIHZhbDsgZG8gZWNobyBcIlxcJHtuYW1lIyMqL309XFwkdmFsXCI7IGRvbmUgPiAvaG9tZS9lYzItdXNlci9yZWNpY2xhamUtYXBwLy5lbnZgLFxuICAgICAgJ2NobW9kIDYwMCAvaG9tZS9lYzItdXNlci9yZWNpY2xhamUtYXBwLy5lbnYnLFxuICAgICAgJ2Nob3duIC1SIGVjMi11c2VyOmVjMi11c2VyIC9ob21lL2VjMi11c2VyL3JlY2ljbGFqZS1hcHAnLFxuICAgICAgJ3N1ZG8gLXUgZWMyLXVzZXIgLWkgc2ggLWMgXCJjZCAvaG9tZS9lYzItdXNlci9yZWNpY2xhamUtYXBwICYmIGRvY2tlciBjb21wb3NlIHVwIC1kIC0tYnVpbGRcIidcbiAgICApO1xuXG4gICAgLy8gRGV0ZWN0YXIgZSBpbnllY3RhciBhdXRvbcOhdGljYW1lbnRlIGxhIGNsYXZlIHDDumJsaWNhIGxvY2FsICh+Ly5zc2gvaWRfZWQyNTUxOS5wdWIgbyB+Ly5zc2gvaWRfcnNhLnB1YilcbiAgICBsZXQgbG9jYWxQdWJsaWNLZXkgPSAnJztcbiAgICB0cnkge1xuICAgICAgY29uc3QgZWQyNTUxOVBhdGggPSBwYXRoLmpvaW4ob3MuaG9tZWRpcigpLCAnLnNzaCcsICdpZF9lZDI1NTE5LnB1YicpO1xuICAgICAgY29uc3QgcnNhUGF0aCA9IHBhdGguam9pbihvcy5ob21lZGlyKCksICcuc3NoJywgJ2lkX3JzYS5wdWInKTtcblxuICAgICAgaWYgKGZzLmV4aXN0c1N5bmMoZWQyNTUxOVBhdGgpKSB7XG4gICAgICAgIGxvY2FsUHVibGljS2V5ID0gZnMucmVhZEZpbGVTeW5jKGVkMjU1MTlQYXRoLCAndXRmOCcpLnRyaW0oKTtcbiAgICAgIH0gZWxzZSBpZiAoZnMuZXhpc3RzU3luYyhyc2FQYXRoKSkge1xuICAgICAgICBsb2NhbFB1YmxpY0tleSA9IGZzLnJlYWRGaWxlU3luYyhyc2FQYXRoLCAndXRmOCcpLnRyaW0oKTtcbiAgICAgIH1cbiAgICB9IGNhdGNoIChlKSB7XG4gICAgICBjb25zb2xlLndhcm4oJ05vIHNlIHB1ZG8gbGVlciBsYSBjbGF2ZSBwdWJsaWNhIGxvY2FsIGVuIH4vLnNzaC8nLCBlKTtcbiAgICB9XG5cbiAgICBpZiAobG9jYWxQdWJsaWNLZXkpIHtcbiAgICAgIHVzZXJEYXRhLmFkZENvbW1hbmRzKFxuICAgICAgICAnbWtkaXIgLXAgL2hvbWUvZWMyLXVzZXIvLnNzaCcsXG4gICAgICAgICdjaG1vZCA3MDAgL2hvbWUvZWMyLXVzZXIvLnNzaCcsXG4gICAgICAgIGBlY2hvIFwiJHtsb2NhbFB1YmxpY0tleX1cIiA+PiAvaG9tZS9lYzItdXNlci8uc3NoL2F1dGhvcml6ZWRfa2V5c2AsXG4gICAgICAgICdjaG1vZCA2MDAgL2hvbWUvZWMyLXVzZXIvLnNzaC9hdXRob3JpemVkX2tleXMnLFxuICAgICAgICAnY2hvd24gLVIgZWMyLXVzZXI6ZWMyLXVzZXIgL2hvbWUvZWMyLXVzZXIvLnNzaCdcbiAgICAgICk7XG4gICAgfVxuXG4gICAgLy8gT2J0ZW5lciBvcGNpb25hbG1lbnRlIGVsIEtleVBhaXIgbmFtZSBkZXNkZSBjb250ZXh0byBkZSBDREs6IGNkayBkZXBsb3kgLWMga2V5TmFtZT1taS1sbGF2ZS1zc2hcbiAgICBjb25zdCBrZXlOYW1lID0gdGhpcy5ub2RlLnRyeUdldENvbnRleHQoJ2tleU5hbWUnKTtcblxuICAgIC8vIDYuIEluc3RhbmNpYSBFQzIgR3Jhdml0b24gKHQ0Zy5zbWFsbDogMiB2Q1BVIEFSTTY0LCAyIEdCIFJBTSAtIH4kNi0kOCBVU0QvbWVzKVxuICAgIGNvbnN0IGVjMkluc3RhbmNlID0gbmV3IGVjMi5JbnN0YW5jZSh0aGlzLCAnUmVjaWNsYWplRWMySW5zdGFuY2UnLCB7XG4gICAgICB2cGMsXG4gICAgICBpbnN0YW5jZVR5cGU6IGVjMi5JbnN0YW5jZVR5cGUub2YoZWMyLkluc3RhbmNlQ2xhc3MuVDRHLCBlYzIuSW5zdGFuY2VTaXplLlNNQUxMKSxcbiAgICAgIG1hY2hpbmVJbWFnZTogZWMyLk1hY2hpbmVJbWFnZS5sYXRlc3RBbWF6b25MaW51eDIwMjMoe1xuICAgICAgICBjcHVUeXBlOiBlYzIuQW1hem9uTGludXhDcHVUeXBlLkFSTV82NCxcbiAgICAgIH0pLFxuICAgICAgc2VjdXJpdHlHcm91cDogZWMyU2VjdXJpdHlHcm91cCxcbiAgICAgIHJvbGU6IGVjMlJvbGUsXG4gICAgICB1c2VyRGF0YTogdXNlckRhdGEsXG4gICAgICB2cGNTdWJuZXRzOiB7IHN1Ym5ldFR5cGU6IGVjMi5TdWJuZXRUeXBlLlBVQkxJQyB9LFxuICAgICAgLi4uKGtleU5hbWUgPyB7IGtleU5hbWUgfSA6IHt9KSxcbiAgICB9KTtcblxuICAgIC8vIDcuIERpcmVjY2nDs24gSVAgRWzDoXN0aWNhIChFbGFzdGljIElQKSBmaWphXG4gICAgY29uc3QgZWlwID0gbmV3IGVjMi5DZm5FSVAodGhpcywgJ1JlY2ljbGFqZUVsYXN0aWNJUCcsIHtcbiAgICAgIGluc3RhbmNlSWQ6IGVjMkluc3RhbmNlLmluc3RhbmNlSWQsXG4gICAgfSk7XG5cbiAgICAvLyBPdXRwdXRzIGRlIGxhIEluZnJhZXN0cnVjdHVyYVxuICAgIG5ldyBjZGsuQ2ZuT3V0cHV0KHRoaXMsICdCdWNrZXROYW1lT3V0cHV0Jywge1xuICAgICAgdmFsdWU6IGZvdG9zQnVja2V0LmJ1Y2tldE5hbWUsXG4gICAgICBkZXNjcmlwdGlvbjogJ05vbWJyZSBkZWwgQnVja2V0IFMzIHBhcmEgRm90b3MgZGUgSW5zcGVjY2nDs24nLFxuICAgIH0pO1xuXG4gICAgbmV3IGNkay5DZm5PdXRwdXQodGhpcywgJ0luc3RhbmNlSWRPdXRwdXQnLCB7XG4gICAgICB2YWx1ZTogZWMySW5zdGFuY2UuaW5zdGFuY2VJZCxcbiAgICAgIGRlc2NyaXB0aW9uOiAnSUQgZGUgbGEgSW5zdGFuY2lhIEVDMiBHcmF2aXRvbiAocGFyYSBBV1MgU1NNKScsXG4gICAgfSk7XG5cbiAgICBuZXcgY2RrLkNmbk91dHB1dCh0aGlzLCAnUHVibGljSXBPdXRwdXQnLCB7XG4gICAgICB2YWx1ZTogZWlwLnJlZixcbiAgICAgIGRlc2NyaXB0aW9uOiAnSVAgUHVibGljYSBFbMOhc3RpY2EgZGUgbGEgSW5zdGFuY2lhIEVDMiBHcmF2aXRvbicsXG4gICAgfSk7XG5cbiAgICBuZXcgY2RrLkNmbk91dHB1dCh0aGlzLCAnQXBwVXJsT3V0cHV0Jywge1xuICAgICAgdmFsdWU6IGBodHRwOi8vJHtlaXAucmVmfWAsXG4gICAgICBkZXNjcmlwdGlvbjogJ1VSTCBkZSBhY2Nlc28gZGlyZWN0byBhIGxhIEFwbGljYWNpb24gV2ViIGRlIFJlY2ljbGFqZSBMaXRvcmFsJyxcbiAgICB9KTtcbiAgfVxufVxuIl19