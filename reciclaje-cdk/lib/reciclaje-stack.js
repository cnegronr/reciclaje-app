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
        // 5. Script de Inicialización (UserData): Instala Docker, Docker Compose y prepara el despliegue automático
        const userData = ec2.UserData.forLinux();
        userData.addCommands('sudo dnf update -y', 'sudo dnf install -y docker git docker-compose-plugin', 'sudo systemctl enable --now docker', 'sudo usermod -aG docker ec2-user', 'git config --system --add safe.directory /home/ec2-user/reciclaje-app', 'mkdir -p /home/ec2-user', 'cd /home/ec2-user', 'if [ ! -d "reciclaje-app" ]; then', '  git clone https://github.com/cnegronr/reciclaje-app reciclaje-app', 'fi', 'cd /home/ec2-user/reciclaje-app', 'if [ ! -f ".env" ]; then', `  cat << 'EOF' > .env`, 'POSTGRES_DB=reciclaje_db', 'POSTGRES_USER=reciclaje_user', 'POSTGRES_PASSWORD=SuperSecretProdPostgresPass2026!', 'JWT_SECRET=SuperSecretKeyForJWTAuth2026WithEnoughBitLengthForHMACSHA256Signature!', `AWS_S3_BUCKET=${fotosBucket.bucketName}`, `AWS_REGION=${this.region}`, 'ADMIN_INITIAL_EMAIL=admin@reciclajelitoral.cl', 'ADMIN_INITIAL_NAME=Administrador General', 'ADMIN_INITIAL_PASSWORD=AdminReciclaje2026!', 'SPRING_PROFILES_ACTIVE=prod', 'EOF', 'fi', 'chown -R ec2-user:ec2-user /home/ec2-user/reciclaje-app', 'sudo -u ec2-user -i sh -c "cd /home/ec2-user/reciclaje-app && docker compose up -d --build"');
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
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoicmVjaWNsYWplLXN0YWNrLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXMiOlsicmVjaWNsYWplLXN0YWNrLnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiI7OztBQUFBLG1DQUFtQztBQUNuQywyQ0FBMkM7QUFDM0MseUNBQXlDO0FBQ3pDLDJDQUEyQztBQUUzQyx5QkFBeUI7QUFDekIsNkJBQTZCO0FBQzdCLHlCQUF5QjtBQUV6QixNQUFhLGNBQWUsU0FBUSxHQUFHLENBQUMsS0FBSztJQUMzQyxZQUFZLEtBQWdCLEVBQUUsRUFBVSxFQUFFLEtBQXNCO1FBQzlELEtBQUssQ0FBQyxLQUFLLEVBQUUsRUFBRSxFQUFFLEtBQUssQ0FBQyxDQUFDO1FBRXhCLDBEQUEwRDtRQUMxRCxNQUFNLFdBQVcsR0FBRyxJQUFJLEVBQUUsQ0FBQyxNQUFNLENBQUMsSUFBSSxFQUFFLHNCQUFzQixFQUFFO1lBQzlELFVBQVUsRUFBRSwyQkFBMkIsSUFBSSxDQUFDLE9BQU8sSUFBSSxJQUFJLENBQUMsTUFBTSxFQUFFO1lBQ3BFLGFBQWEsRUFBRSxHQUFHLENBQUMsYUFBYSxDQUFDLE9BQU87WUFDeEMsaUJBQWlCLEVBQUUsSUFBSTtZQUN2QixJQUFJLEVBQUU7Z0JBQ0o7b0JBQ0UsY0FBYyxFQUFFO3dCQUNkLEVBQUUsQ0FBQyxXQUFXLENBQUMsR0FBRzt3QkFDbEIsRUFBRSxDQUFDLFdBQVcsQ0FBQyxHQUFHO3dCQUNsQixFQUFFLENBQUMsV0FBVyxDQUFDLElBQUk7d0JBQ25CLEVBQUUsQ0FBQyxXQUFXLENBQUMsTUFBTTtxQkFDdEI7b0JBQ0QsY0FBYyxFQUFFLENBQUMsR0FBRyxDQUFDO29CQUNyQixjQUFjLEVBQUUsQ0FBQyxHQUFHLENBQUM7aUJBQ3RCO2FBQ0Y7U0FDRixDQUFDLENBQUM7UUFFSCxrR0FBa0c7UUFDbEcsTUFBTSxHQUFHLEdBQUcsSUFBSSxHQUFHLENBQUMsR0FBRyxDQUFDLElBQUksRUFBRSxjQUFjLEVBQUU7WUFDNUMsTUFBTSxFQUFFLENBQUM7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QUMsVUFBVSxDQUFDLE1BQU0sRUFBRTtZQUNqRCxHQUFHLENBQUMsT0FBTyxDQUFDLENBQUMsQ0FBQyxFQUFFLE9BQU8sRUFBRSxDQUFDLENBQUMsQ0FBQyxFQUFFLENBQUM7U0FDaEMsQ0FBQyxDQUFDO1FBRUgsNkNBQTZDO1FBQzdDLE1BQU0sR0FBRyxHQUFHLElBQUksR0FBRyxDQUFDLE1BQU0sQ0FBQyxJQUFJLEVBQUUsb0JBQW9CLEVBQUU7WUFDckQsVUFBVSxFQUFFLFdBQVcsQ0FBQyxVQUFVO1NBQ25DLENBQUMsQ0FBQztRQUVILGdDQUFnQztRQUNoQyxJQUFJLEdBQUcsQ0FBQyxTQUFTLENBQUMsSUFBSSxFQUFFLGtCQUFrQixFQUFFO1lBQzFDLEtBQUssRUFBRSxXQUFXLENBQUMsVUFBVTtZQUM3QixXQUFXLEVBQUUsK0NBQStDO1NBQzdELENBQUMsQ0FBQztRQUVILElBQUksR0FBRyxDQUFDLFNBQVMsQ0FBQyxJQUFJLEVBQUUsa0JBQWtCLEVBQUU7WUFDMUMsS0FBSyxFQUFFLFdBQVcsQ0FBQyxVQUFVO1lBQzdCLFdBQVcsRUFBRSxnREFBZ0Q7U0FDOUQsQ0FBQyxDQUFDO1FBRUgsSUFBSSxHQUFHLENBQUMsU0FBUyxDQUFDLElBQUksRUFBRSxnQkFBZ0IsRUFBRTtZQUN4QyxLQUFLLEVBQUUsR0FBRyxDQUFDLEdBQUc7WUFDZCxXQUFXLEVBQUUsa0RBQWtEO1NBQ2hFLENBQUMsQ0FBQztRQUVILElBQUksR0FBRyxDQUFDLFNBQVMsQ0FBQyxJQUFJLEVBQUUsY0FBYyxFQUFFO1lBQ3RDLEtBQUssRUFBRSxVQUFVLEdBQUcsQ0FBQyxHQUFHLEVBQUU7WUFDMUIsV0FBVyxFQUFFLGdFQUFnRTtTQUM5RSxDQUFDLENBQUM7SUFDTCxDQUFDO0NBQ0Y7QUE1SkQsd0NBNEpDIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0ICogYXMgY2RrIGZyb20gJ2F3cy1jZGstbGliJztcbmltcG9ydCAqIGFzIGVjMiBmcm9tICdhd3MtY2RrLWxpYi9hd3MtZWMyJztcbmltcG9ydCAqIGFzIHMzIGZyb20gJ2F3cy1jZGstbGliL2F3cy1zMyc7XG5pbXBvcnQgKiBhcyBpYW0gZnJvbSAnYXdzLWNkay1saWIvYXdzLWlhbSc7XG5pbXBvcnQgeyBDb25zdHJ1Y3QgfSBmcm9tICdjb25zdHJ1Y3RzJztcbmltcG9ydCAqIGFzIGZzIGZyb20gJ2ZzJztcbmltcG9ydCAqIGFzIHBhdGggZnJvbSAncGF0aCc7XG5pbXBvcnQgKiBhcyBvcyBmcm9tICdvcyc7XG5cbmV4cG9ydCBjbGFzcyBSZWNpY2xhamVTdGFjayBleHRlbmRzIGNkay5TdGFjayB7XG4gIGNvbnN0cnVjdG9yKHNjb3BlOiBDb25zdHJ1Y3QsIGlkOiBzdHJpbmcsIHByb3BzPzogY2RrLlN0YWNrUHJvcHMpIHtcbiAgICBzdXBlcihzY29wZSwgaWQsIHByb3BzKTtcblxuICAgIC8vIDEuIEJ1Y2tldCBTMyBwYXJhIEFsbWFjZW5hbWllbnRvIGRlIEZvdG9zIGRlIEluc3BlY2Npw7NuXG4gICAgY29uc3QgZm90b3NCdWNrZXQgPSBuZXcgczMuQnVja2V0KHRoaXMsICdSZWNpY2xhamVGb3Rvc0J1Y2tldCcsIHtcbiAgICAgIGJ1Y2tldE5hbWU6IGByZWNpY2xhamUtbGl0b3JhbC1mb3Rvcy0ke3RoaXMuYWNjb3VudH0tJHt0aGlzLnJlZ2lvbn1gLFxuICAgICAgcmVtb3ZhbFBvbGljeTogY2RrLlJlbW92YWxQb2xpY3kuREVTVFJPWSxcbiAgICAgIGF1dG9EZWxldGVPYmplY3RzOiB0cnVlLFxuICAgICAgY29yczogW1xuICAgICAgICB7XG4gICAgICAgICAgYWxsb3dlZE1ldGhvZHM6IFtcbiAgICAgICAgICAgIHMzLkh0dHBNZXRob2RzLkdFVCxcbiAgICAgICAgICAgIHMzLkh0dHBNZXRob2RzLlBVVCxcbiAgICAgICAgICAgIHMzLkh0dHBNZXRob2RzLlBPU1QsXG4gICAgICAgICAgICBzMy5IdHRwTWV0aG9kcy5ERUxFVEUsXG4gICAgICAgICAgXSxcbiAgICAgICAgICBhbGxvd2VkT3JpZ2luczogWycqJ10sXG4gICAgICAgICAgYWxsb3dlZEhlYWRlcnM6IFsnKiddLFxuICAgICAgICB9LFxuICAgICAgXSxcbiAgICB9KTtcblxuICAgIC8vIDIuIFZQQyAoUmVkIFByaXZhZGEgVmlydHVhbCAtIFN1YnJlZGVzIFDDumJsaWNhcyDDum5pY2FtZW50ZSBwYXJhIGVsaW1pbmFyIGNvc3RvcyBkZSBOQVQgR2F0ZXdheSlcbiAgICBjb25zdCB2cGMgPSBuZXcgZWMyLlZwYyh0aGlzLCAnUmVjaWNsYWplVnBjJywge1xuICAgICAgbWF4QXpzOiAxLFxuICAgICAgbmF0R2F0ZXdheXM6IDAsXG4gICAgICBzdWJuZXRDb25maWd1cmF0aW9uOiBbXG4gICAgICAgIHtcbiAgICAgICAgICBjaWRyTWFzazogMjQsXG4gICAgICAgICAgbmFtZTogJ1B1YmxpY1N1Ym5ldCcsXG4gICAgICAgICAgc3VibmV0VHlwZTogZWMyLlN1Ym5ldFR5cGUuUFVCTElDLFxuICAgICAgICB9LFxuICAgICAgXSxcbiAgICB9KTtcblxuICAgIC8vIDMuIEdydXBvIGRlIFNlZ3VyaWRhZCAoU2VjdXJpdHkgR3JvdXApXG4gICAgY29uc3QgZWMyU2VjdXJpdHlHcm91cCA9IG5ldyBlYzIuU2VjdXJpdHlHcm91cCh0aGlzLCAnUmVjaWNsYWplRWMyU2cnLCB7XG4gICAgICB2cGMsXG4gICAgICBkZXNjcmlwdGlvbjogJ1Blcm1pdGlyIHRyYWZpY28gSFRUUCwgSFRUUFMgeSBTU0ggaGFjaWEgbGEgYXBwIFJlY2ljbGFqZSBMaXRvcmFsJyxcbiAgICAgIGFsbG93QWxsT3V0Ym91bmQ6IHRydWUsXG4gICAgfSk7XG5cbiAgICBlYzJTZWN1cml0eUdyb3VwLmFkZEluZ3Jlc3NSdWxlKGVjMi5QZWVyLmFueUlwdjQoKSwgZWMyLlBvcnQudGNwKDgwKSwgJ1Blcm1pdGlyIHRyYWZpY28gV2ViIEhUVFAnKTtcbiAgICBlYzJTZWN1cml0eUdyb3VwLmFkZEluZ3Jlc3NSdWxlKGVjMi5QZWVyLmFueUlwdjQoKSwgZWMyLlBvcnQudGNwKDQ0MyksICdQZXJtaXRpciB0cmFmaWNvIFdlYiBIVFRQUycpO1xuICAgIGVjMlNlY3VyaXR5R3JvdXAuYWRkSW5ncmVzc1J1bGUoZWMyLlBlZXIuYW55SXB2NCgpLCBlYzIuUG9ydC50Y3AoMjIpLCAnUGVybWl0aXIgYWNjZXNvIFNTSCcpO1xuXG4gICAgLy8gNC4gUm9sIGRlIElBTSBwYXJhIGxhIEluc3RhbmNpYSBFQzIgY29uIFBlcm1pc29zIGhhY2lhIFMzIHkgU1NNIFNlc3Npb24gTWFuYWdlclxuICAgIGNvbnN0IGVjMlJvbGUgPSBuZXcgaWFtLlJvbGUodGhpcywgJ1JlY2ljbGFqZUVjMlJvbGUnLCB7XG4gICAgICBhc3N1bWVkQnk6IG5ldyBpYW0uU2VydmljZVByaW5jaXBhbCgnZWMyLmFtYXpvbmF3cy5jb20nKSxcbiAgICAgIGRlc2NyaXB0aW9uOiAnUm9sIGRlIEVDMiBjb24gYWNjZXNvIGEgUzMgeSBTU00gU2Vzc2lvbiBNYW5hZ2VyJyxcbiAgICB9KTtcblxuICAgIGZvdG9zQnVja2V0LmdyYW50UmVhZFdyaXRlKGVjMlJvbGUpO1xuICAgIGVjMlJvbGUuYWRkTWFuYWdlZFBvbGljeShpYW0uTWFuYWdlZFBvbGljeS5mcm9tQXdzTWFuYWdlZFBvbGljeU5hbWUoJ0FtYXpvblNTTU1hbmFnZWRJbnN0YW5jZUNvcmUnKSk7XG5cbiAgICAvLyA1LiBTY3JpcHQgZGUgSW5pY2lhbGl6YWNpw7NuIChVc2VyRGF0YSk6IEluc3RhbGEgRG9ja2VyLCBEb2NrZXIgQ29tcG9zZSB5IHByZXBhcmEgZWwgZGVzcGxpZWd1ZSBhdXRvbcOhdGljb1xuICAgIGNvbnN0IHVzZXJEYXRhID0gZWMyLlVzZXJEYXRhLmZvckxpbnV4KCk7XG4gICAgdXNlckRhdGEuYWRkQ29tbWFuZHMoXG4gICAgICAnc3VkbyBkbmYgdXBkYXRlIC15JyxcbiAgICAgICdzdWRvIGRuZiBpbnN0YWxsIC15IGRvY2tlciBnaXQgZG9ja2VyLWNvbXBvc2UtcGx1Z2luJyxcbiAgICAgICdzdWRvIHN5c3RlbWN0bCBlbmFibGUgLS1ub3cgZG9ja2VyJyxcbiAgICAgICdzdWRvIHVzZXJtb2QgLWFHIGRvY2tlciBlYzItdXNlcicsXG4gICAgICAnZ2l0IGNvbmZpZyAtLXN5c3RlbSAtLWFkZCBzYWZlLmRpcmVjdG9yeSAvaG9tZS9lYzItdXNlci9yZWNpY2xhamUtYXBwJyxcbiAgICAgICdta2RpciAtcCAvaG9tZS9lYzItdXNlcicsXG4gICAgICAnY2QgL2hvbWUvZWMyLXVzZXInLFxuICAgICAgJ2lmIFsgISAtZCBcInJlY2ljbGFqZS1hcHBcIiBdOyB0aGVuJyxcbiAgICAgICcgIGdpdCBjbG9uZSBodHRwczovL2dpdGh1Yi5jb20vY25lZ3JvbnIvcmVjaWNsYWplLWFwcCByZWNpY2xhamUtYXBwJyxcbiAgICAgICdmaScsXG4gICAgICAnY2QgL2hvbWUvZWMyLXVzZXIvcmVjaWNsYWplLWFwcCcsXG4gICAgICAnaWYgWyAhIC1mIFwiLmVudlwiIF07IHRoZW4nLFxuICAgICAgYCAgY2F0IDw8ICdFT0YnID4gLmVudmAsXG4gICAgICAnUE9TVEdSRVNfREI9cmVjaWNsYWplX2RiJyxcbiAgICAgICdQT1NUR1JFU19VU0VSPXJlY2ljbGFqZV91c2VyJyxcbiAgICAgICdQT1NUR1JFU19QQVNTV09SRD1TdXBlclNlY3JldFByb2RQb3N0Z3Jlc1Bhc3MyMDI2IScsXG4gICAgICAnSldUX1NFQ1JFVD1TdXBlclNlY3JldEtleUZvckpXVEF1dGgyMDI2V2l0aEVub3VnaEJpdExlbmd0aEZvckhNQUNTSEEyNTZTaWduYXR1cmUhJyxcbiAgICAgIGBBV1NfUzNfQlVDS0VUPSR7Zm90b3NCdWNrZXQuYnVja2V0TmFtZX1gLFxuICAgICAgYEFXU19SRUdJT049JHt0aGlzLnJlZ2lvbn1gLFxuICAgICAgJ0FETUlOX0lOSVRJQUxfRU1BSUw9YWRtaW5AcmVjaWNsYWplbGl0b3JhbC5jbCcsXG4gICAgICAnQURNSU5fSU5JVElBTF9OQU1FPUFkbWluaXN0cmFkb3IgR2VuZXJhbCcsXG4gICAgICAnQURNSU5fSU5JVElBTF9QQVNTV09SRD1BZG1pblJlY2ljbGFqZTIwMjYhJyxcbiAgICAgICdTUFJJTkdfUFJPRklMRVNfQUNUSVZFPXByb2QnLFxuICAgICAgJ0VPRicsXG4gICAgICAnZmknLFxuICAgICAgJ2Nob3duIC1SIGVjMi11c2VyOmVjMi11c2VyIC9ob21lL2VjMi11c2VyL3JlY2ljbGFqZS1hcHAnLFxuICAgICAgJ3N1ZG8gLXUgZWMyLXVzZXIgLWkgc2ggLWMgXCJjZCAvaG9tZS9lYzItdXNlci9yZWNpY2xhamUtYXBwICYmIGRvY2tlciBjb21wb3NlIHVwIC1kIC0tYnVpbGRcIidcbiAgICApO1xuXG4gICAgLy8gRGV0ZWN0YXIgZSBpbnllY3RhciBhdXRvbcOhdGljYW1lbnRlIGxhIGNsYXZlIHDDumJsaWNhIGxvY2FsICh+Ly5zc2gvaWRfZWQyNTUxOS5wdWIgbyB+Ly5zc2gvaWRfcnNhLnB1YilcbiAgICBsZXQgbG9jYWxQdWJsaWNLZXkgPSAnJztcbiAgICB0cnkge1xuICAgICAgY29uc3QgZWQyNTUxOVBhdGggPSBwYXRoLmpvaW4ob3MuaG9tZWRpcigpLCAnLnNzaCcsICdpZF9lZDI1NTE5LnB1YicpO1xuICAgICAgY29uc3QgcnNhUGF0aCA9IHBhdGguam9pbihvcy5ob21lZGlyKCksICcuc3NoJywgJ2lkX3JzYS5wdWInKTtcblxuICAgICAgaWYgKGZzLmV4aXN0c1N5bmMoZWQyNTUxOVBhdGgpKSB7XG4gICAgICAgIGxvY2FsUHVibGljS2V5ID0gZnMucmVhZEZpbGVTeW5jKGVkMjU1MTlQYXRoLCAndXRmOCcpLnRyaW0oKTtcbiAgICAgIH0gZWxzZSBpZiAoZnMuZXhpc3RzU3luYyhyc2FQYXRoKSkge1xuICAgICAgICBsb2NhbFB1YmxpY0tleSA9IGZzLnJlYWRGaWxlU3luYyhyc2FQYXRoLCAndXRmOCcpLnRyaW0oKTtcbiAgICAgIH1cbiAgICB9IGNhdGNoIChlKSB7XG4gICAgICBjb25zb2xlLndhcm4oJ05vIHNlIHB1ZG8gbGVlciBsYSBjbGF2ZSBwdWJsaWNhIGxvY2FsIGVuIH4vLnNzaC8nLCBlKTtcbiAgICB9XG5cbiAgICBpZiAobG9jYWxQdWJsaWNLZXkpIHtcbiAgICAgIHVzZXJEYXRhLmFkZENvbW1hbmRzKFxuICAgICAgICAnbWtkaXIgLXAgL2hvbWUvZWMyLXVzZXIvLnNzaCcsXG4gICAgICAgICdjaG1vZCA3MDAgL2hvbWUvZWMyLXVzZXIvLnNzaCcsXG4gICAgICAgIGBlY2hvIFwiJHtsb2NhbFB1YmxpY0tleX1cIiA+PiAvaG9tZS9lYzItdXNlci8uc3NoL2F1dGhvcml6ZWRfa2V5c2AsXG4gICAgICAgICdjaG1vZCA2MDAgL2hvbWUvZWMyLXVzZXIvLnNzaC9hdXRob3JpemVkX2tleXMnLFxuICAgICAgICAnY2hvd24gLVIgZWMyLXVzZXI6ZWMyLXVzZXIgL2hvbWUvZWMyLXVzZXIvLnNzaCdcbiAgICAgICk7XG4gICAgfVxuXG4gICAgLy8gT2J0ZW5lciBvcGNpb25hbG1lbnRlIGVsIEtleVBhaXIgbmFtZSBkZXNkZSBjb250ZXh0byBkZSBDREs6IGNkayBkZXBsb3kgLWMga2V5TmFtZT1taS1sbGF2ZS1zc2hcbiAgICBjb25zdCBrZXlOYW1lID0gdGhpcy5ub2RlLnRyeUdldENvbnRleHQoJ2tleU5hbWUnKTtcblxuICAgIC8vIDYuIEluc3RhbmNpYSBFQzIgR3Jhdml0b24gKHQ0Zy5zbWFsbDogMiB2Q1BVIEFSTTY0LCAyIEdCIFJBTSAtIH4kNi0kOCBVU0QvbWVzKVxuICAgIGNvbnN0IGVjMkluc3RhbmNlID0gbmV3IGVjMi5JbnN0YW5jZSh0aGlzLCAnUmVjaWNsYWplRWMySW5zdGFuY2UnLCB7XG4gICAgICB2cGMsXG4gICAgICBpbnN0YW5jZVR5cGU6IGVjMi5JbnN0YW5jZVR5cGUub2YoZWMyLkluc3RhbmNlQ2xhc3MuVDRHLCBlYzIuSW5zdGFuY2VTaXplLlNNQUxMKSxcbiAgICAgIG1hY2hpbmVJbWFnZTogZWMyLk1hY2hpbmVJbWFnZS5sYXRlc3RBbWF6b25MaW51eDIwMjMoe1xuICAgICAgICBjcHVUeXBlOiBlYzIuQW1hem9uTGludXhDcHVUeXBlLkFSTV82NCxcbiAgICAgIH0pLFxuICAgICAgc2VjdXJpdHlHcm91cDogZWMyU2VjdXJpdHlHcm91cCxcbiAgICAgIHJvbGU6IGVjMlJvbGUsXG4gICAgICB1c2VyRGF0YTogdXNlckRhdGEsXG4gICAgICB2cGNTdWJuZXRzOiB7IHN1Ym5ldFR5cGU6IGVjMi5TdWJuZXRUeXBlLlBVQkxJQyB9LFxuICAgICAgLi4uKGtleU5hbWUgPyB7IGtleU5hbWUgfSA6IHt9KSxcbiAgICB9KTtcblxuICAgIC8vIDcuIERpcmVjY2nDs24gSVAgRWzDoXN0aWNhIChFbGFzdGljIElQKSBmaWphXG4gICAgY29uc3QgZWlwID0gbmV3IGVjMi5DZm5FSVAodGhpcywgJ1JlY2ljbGFqZUVsYXN0aWNJUCcsIHtcbiAgICAgIGluc3RhbmNlSWQ6IGVjMkluc3RhbmNlLmluc3RhbmNlSWQsXG4gICAgfSk7XG5cbiAgICAvLyBPdXRwdXRzIGRlIGxhIEluZnJhZXN0cnVjdHVyYVxuICAgIG5ldyBjZGsuQ2ZuT3V0cHV0KHRoaXMsICdCdWNrZXROYW1lT3V0cHV0Jywge1xuICAgICAgdmFsdWU6IGZvdG9zQnVja2V0LmJ1Y2tldE5hbWUsXG4gICAgICBkZXNjcmlwdGlvbjogJ05vbWJyZSBkZWwgQnVja2V0IFMzIHBhcmEgRm90b3MgZGUgSW5zcGVjY2nDs24nLFxuICAgIH0pO1xuXG4gICAgbmV3IGNkay5DZm5PdXRwdXQodGhpcywgJ0luc3RhbmNlSWRPdXRwdXQnLCB7XG4gICAgICB2YWx1ZTogZWMySW5zdGFuY2UuaW5zdGFuY2VJZCxcbiAgICAgIGRlc2NyaXB0aW9uOiAnSUQgZGUgbGEgSW5zdGFuY2lhIEVDMiBHcmF2aXRvbiAocGFyYSBBV1MgU1NNKScsXG4gICAgfSk7XG5cbiAgICBuZXcgY2RrLkNmbk91dHB1dCh0aGlzLCAnUHVibGljSXBPdXRwdXQnLCB7XG4gICAgICB2YWx1ZTogZWlwLnJlZixcbiAgICAgIGRlc2NyaXB0aW9uOiAnSVAgUHVibGljYSBFbMOhc3RpY2EgZGUgbGEgSW5zdGFuY2lhIEVDMiBHcmF2aXRvbicsXG4gICAgfSk7XG5cbiAgICBuZXcgY2RrLkNmbk91dHB1dCh0aGlzLCAnQXBwVXJsT3V0cHV0Jywge1xuICAgICAgdmFsdWU6IGBodHRwOi8vJHtlaXAucmVmfWAsXG4gICAgICBkZXNjcmlwdGlvbjogJ1VSTCBkZSBhY2Nlc28gZGlyZWN0byBhIGxhIEFwbGljYWNpb24gV2ViIGRlIFJlY2ljbGFqZSBMaXRvcmFsJyxcbiAgICB9KTtcbiAgfVxufVxuIl19