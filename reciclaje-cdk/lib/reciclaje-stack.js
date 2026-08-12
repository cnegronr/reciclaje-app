"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ReciclajeStack = void 0;
const cdk = require("aws-cdk-lib");
const ec2 = require("aws-cdk-lib/aws-ec2");
const s3 = require("aws-cdk-lib/aws-s3");
const iam = require("aws-cdk-lib/aws-iam");
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
        // 4. Rol de IAM para la Instancia EC2 con Permisos hacia el Bucket S3
        const ec2Role = new iam.Role(this, 'ReciclajeEc2Role', {
            assumedBy: new iam.ServicePrincipal('ec2.amazonaws.com'),
            description: 'Rol de EC2 con acceso de lectura/escritura al bucket de fotos S3',
        });
        fotosBucket.grantReadWrite(ec2Role);
        // 5. Script de Inicialización (UserData): Instala Docker, Docker Compose y levanta el stack
        const userData = ec2.UserData.forLinux();
        userData.addCommands('sudo dnf update -y', 'sudo dnf install -y docker git docker-compose-plugin', 'sudo systemctl enable --now docker', 'sudo usermod -aG docker ec2-user', 'mkdir -p /home/ec2-user/app', 'cd /home/ec2-user/app', 'echo "Servidor de Reciclaje Litoral listo para desplegar con docker compose up -d --build"');
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
        });
        // 7. Dirección IP Elástica (Elastic IP) fija
        const eip = new ec2.CfnEIP(this, 'ReciclajeElasticIP', {
            instanceId: ec2Instance.instanceId,
        });
        // Output Outputs de la Infraestructura
        new cdk.CfnOutput(this, 'BucketNameOutput', {
            value: fotosBucket.bucketName,
            description: 'Nombre del Bucket S3 para Fotos de Inspección',
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
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoicmVjaWNsYWplLXN0YWNrLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXMiOlsicmVjaWNsYWplLXN0YWNrLnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiI7OztBQUFBLG1DQUFtQztBQUNuQywyQ0FBMkM7QUFDM0MseUNBQXlDO0FBQ3pDLDJDQUEyQztBQUczQyxNQUFhLGNBQWUsU0FBUSxHQUFHLENBQUMsS0FBSztJQUMzQyxZQUFZLEtBQWdCLEVBQUUsRUFBVSxFQUFFLEtBQXNCO1FBQzlELEtBQUssQ0FBQyxLQUFLLEVBQUUsRUFBRSxFQUFFLEtBQUssQ0FBQyxDQUFDO1FBRXhCLDBEQUEwRDtRQUMxRCxNQUFNLFdBQVcsR0FBRyxJQUFJLEVBQUUsQ0FBQyxNQUFNLENBQUMsSUFBSSxFQUFFLHNCQUFzQixFQUFFO1lBQzlELFVBQVUsRUFBRSwyQkFBMkIsSUFBSSxDQUFDLE9BQU8sSUFBSSxJQUFJLENBQUMsTUFBTSxFQUFFO1lBQ3BFLGFBQWEsRUFBRSxHQUFHLENBQUMsYUFBYSxDQUFDLE9BQU87WUFDeEMsaUJBQWlCLEVBQUUsSUFBSTtZQUN2QixJQUFJLEVBQUU7Z0JBQ0o7b0JBQ0UsY0FBYyxFQUFFO3dCQUNkLEVBQUUsQ0FBQyxXQUFXLENBQUMsR0FBRzt3QkFDbEIsRUFBRSxDQUFDLFdBQVcsQ0FBQyxHQUFHO3dCQUNsQixFQUFFLENBQUMsV0FBVyxDQUFDLElBQUk7d0JBQ25CLEVBQUUsQ0FBQyxXQUFXLENBQUMsTUFBTTtxQkFDdEI7b0JBQ0QsY0FBYyxFQUFFLENBQUMsR0FBRyxDQUFDO29CQUNyQixjQUFjLEVBQUUsQ0FBQyxHQUFHLENBQUM7aUJBQ3RCO2FBQ0Y7U0FDRixDQUFDLENBQUM7UUFFSCxrR0FBa0c7UUFDbEcsTUFBTSxHQUFHLEdBQUcsSUFBSSxHQUFHLENBQUMsR0FBRyxDQUFDLElBQUksRUFBRSxjQUFjLEVBQUU7WUFDNUMsTUFBTSxFQUFFLENBQUM7WUFDVCxXQUFXLEVBQUUsQ0FBQztZQUNkLG1CQUFtQixFQUFFO2dC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QUFFO1lBQ3hDLEtBQUssRUFBRSxHQUFHLENBQUMsR0FBRztZQUNkLFdBQVcsRUFBRSxrREFBa0Q7U0FDaEUsQ0FBQyxDQUFDO1FBRUgsSUFBSSxHQUFHLENBQUMsU0FBUyxDQUFDLElBQUksRUFBRSxjQUFjLEVBQUU7WUFDdEMsS0FBSyxFQUFFLFVBQVUsR0FBRyxDQUFDLEdBQUcsRUFBRTtZQUMxQixXQUFXLEVBQUUsZ0VBQWdFO1NBQzlFLENBQUMsQ0FBQztJQUNMLENBQUM7Q0FDRjtBQXJHRCx3Q0FxR0MiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgKiBhcyBjZGsgZnJvbSAnYXdzLWNkay1saWInO1xuaW1wb3J0ICogYXMgZWMyIGZyb20gJ2F3cy1jZGstbGliL2F3cy1lYzInO1xuaW1wb3J0ICogYXMgczMgZnJvbSAnYXdzLWNkay1saWIvYXdzLXMzJztcbmltcG9ydCAqIGFzIGlhbSBmcm9tICdhd3MtY2RrLWxpYi9hd3MtaWFtJztcbmltcG9ydCB7IENvbnN0cnVjdCB9IGZyb20gJ2NvbnN0cnVjdHMnO1xuXG5leHBvcnQgY2xhc3MgUmVjaWNsYWplU3RhY2sgZXh0ZW5kcyBjZGsuU3RhY2sge1xuICBjb25zdHJ1Y3RvcihzY29wZTogQ29uc3RydWN0LCBpZDogc3RyaW5nLCBwcm9wcz86IGNkay5TdGFja1Byb3BzKSB7XG4gICAgc3VwZXIoc2NvcGUsIGlkLCBwcm9wcyk7XG5cbiAgICAvLyAxLiBCdWNrZXQgUzMgcGFyYSBBbG1hY2VuYW1pZW50byBkZSBGb3RvcyBkZSBJbnNwZWNjacOzblxuICAgIGNvbnN0IGZvdG9zQnVja2V0ID0gbmV3IHMzLkJ1Y2tldCh0aGlzLCAnUmVjaWNsYWplRm90b3NCdWNrZXQnLCB7XG4gICAgICBidWNrZXROYW1lOiBgcmVjaWNsYWplLWxpdG9yYWwtZm90b3MtJHt0aGlzLmFjY291bnR9LSR7dGhpcy5yZWdpb259YCxcbiAgICAgIHJlbW92YWxQb2xpY3k6IGNkay5SZW1vdmFsUG9saWN5LkRFU1RST1ksXG4gICAgICBhdXRvRGVsZXRlT2JqZWN0czogdHJ1ZSxcbiAgICAgIGNvcnM6IFtcbiAgICAgICAge1xuICAgICAgICAgIGFsbG93ZWRNZXRob2RzOiBbXG4gICAgICAgICAgICBzMy5IdHRwTWV0aG9kcy5HRVQsXG4gICAgICAgICAgICBzMy5IdHRwTWV0aG9kcy5QVVQsXG4gICAgICAgICAgICBzMy5IdHRwTWV0aG9kcy5QT1NULFxuICAgICAgICAgICAgczMuSHR0cE1ldGhvZHMuREVMRVRFLFxuICAgICAgICAgIF0sXG4gICAgICAgICAgYWxsb3dlZE9yaWdpbnM6IFsnKiddLFxuICAgICAgICAgIGFsbG93ZWRIZWFkZXJzOiBbJyonXSxcbiAgICAgICAgfSxcbiAgICAgIF0sXG4gICAgfSk7XG5cbiAgICAvLyAyLiBWUEMgKFJlZCBQcml2YWRhIFZpcnR1YWwgLSBTdWJyZWRlcyBQw7pibGljYXMgw7puaWNhbWVudGUgcGFyYSBlbGltaW5hciBjb3N0b3MgZGUgTkFUIEdhdGV3YXkpXG4gICAgY29uc3QgdnBjID0gbmV3IGVjMi5WcGModGhpcywgJ1JlY2ljbGFqZVZwYycsIHtcbiAgICAgIG1heEF6czogMSxcbiAgICAgIG5hdEdhdGV3YXlzOiAwLFxuICAgICAgc3VibmV0Q29uZmlndXJhdGlvbjogW1xuICAgICAgICB7XG4gICAgICAgICAgY2lkck1hc2s6IDI0LFxuICAgICAgICAgIG5hbWU6ICdQdWJsaWNTdWJuZXQnLFxuICAgICAgICAgIHN1Ym5ldFR5cGU6IGVjMi5TdWJuZXRUeXBlLlBVQkxJQyxcbiAgICAgICAgfSxcbiAgICAgIF0sXG4gICAgfSk7XG5cbiAgICAvLyAzLiBHcnVwbyBkZSBTZWd1cmlkYWQgKFNlY3VyaXR5IEdyb3VwKVxuICAgIGNvbnN0IGVjMlNlY3VyaXR5R3JvdXAgPSBuZXcgZWMyLlNlY3VyaXR5R3JvdXAodGhpcywgJ1JlY2ljbGFqZUVjMlNnJywge1xuICAgICAgdnBjLFxuICAgICAgZGVzY3JpcHRpb246ICdQZXJtaXRpciB0cmFmaWNvIEhUVFAsIEhUVFBTIHkgU1NIIGhhY2lhIGxhIGFwcCBSZWNpY2xhamUgTGl0b3JhbCcsXG4gICAgICBhbGxvd0FsbE91dGJvdW5kOiB0cnVlLFxuICAgIH0pO1xuXG4gICAgZWMyU2VjdXJpdHlHcm91cC5hZGRJbmdyZXNzUnVsZShlYzIuUGVlci5hbnlJcHY0KCksIGVjMi5Qb3J0LnRjcCg4MCksICdQZXJtaXRpciB0cmFmaWNvIFdlYiBIVFRQJyk7XG4gICAgZWMyU2VjdXJpdHlHcm91cC5hZGRJbmdyZXNzUnVsZShlYzIuUGVlci5hbnlJcHY0KCksIGVjMi5Qb3J0LnRjcCg0NDMpLCAnUGVybWl0aXIgdHJhZmljbyBXZWIgSFRUUFMnKTtcbiAgICBlYzJTZWN1cml0eUdyb3VwLmFkZEluZ3Jlc3NSdWxlKGVjMi5QZWVyLmFueUlwdjQoKSwgZWMyLlBvcnQudGNwKDIyKSwgJ1Blcm1pdGlyIGFjY2VzbyBTU0gnKTtcblxuICAgIC8vIDQuIFJvbCBkZSBJQU0gcGFyYSBsYSBJbnN0YW5jaWEgRUMyIGNvbiBQZXJtaXNvcyBoYWNpYSBlbCBCdWNrZXQgUzNcbiAgICBjb25zdCBlYzJSb2xlID0gbmV3IGlhbS5Sb2xlKHRoaXMsICdSZWNpY2xhamVFYzJSb2xlJywge1xuICAgICAgYXNzdW1lZEJ5OiBuZXcgaWFtLlNlcnZpY2VQcmluY2lwYWwoJ2VjMi5hbWF6b25hd3MuY29tJyksXG4gICAgICBkZXNjcmlwdGlvbjogJ1JvbCBkZSBFQzIgY29uIGFjY2VzbyBkZSBsZWN0dXJhL2VzY3JpdHVyYSBhbCBidWNrZXQgZGUgZm90b3MgUzMnLFxuICAgIH0pO1xuXG4gICAgZm90b3NCdWNrZXQuZ3JhbnRSZWFkV3JpdGUoZWMyUm9sZSk7XG5cbiAgICAvLyA1LiBTY3JpcHQgZGUgSW5pY2lhbGl6YWNpw7NuIChVc2VyRGF0YSk6IEluc3RhbGEgRG9ja2VyLCBEb2NrZXIgQ29tcG9zZSB5IGxldmFudGEgZWwgc3RhY2tcbiAgICBjb25zdCB1c2VyRGF0YSA9IGVjMi5Vc2VyRGF0YS5mb3JMaW51eCgpO1xuICAgIHVzZXJEYXRhLmFkZENvbW1hbmRzKFxuICAgICAgJ3N1ZG8gZG5mIHVwZGF0ZSAteScsXG4gICAgICAnc3VkbyBkbmYgaW5zdGFsbCAteSBkb2NrZXIgZ2l0IGRvY2tlci1jb21wb3NlLXBsdWdpbicsXG4gICAgICAnc3VkbyBzeXN0ZW1jdGwgZW5hYmxlIC0tbm93IGRvY2tlcicsXG4gICAgICAnc3VkbyB1c2VybW9kIC1hRyBkb2NrZXIgZWMyLXVzZXInLFxuICAgICAgJ21rZGlyIC1wIC9ob21lL2VjMi11c2VyL2FwcCcsXG4gICAgICAnY2QgL2hvbWUvZWMyLXVzZXIvYXBwJyxcbiAgICAgICdlY2hvIFwiU2Vydmlkb3IgZGUgUmVjaWNsYWplIExpdG9yYWwgbGlzdG8gcGFyYSBkZXNwbGVnYXIgY29uIGRvY2tlciBjb21wb3NlIHVwIC1kIC0tYnVpbGRcIidcbiAgICApO1xuXG4gICAgLy8gNi4gSW5zdGFuY2lhIEVDMiBHcmF2aXRvbiAodDRnLnNtYWxsOiAyIHZDUFUgQVJNNjQsIDIgR0IgUkFNIC0gfiQ2LSQ4IFVTRC9tZXMpXG4gICAgY29uc3QgZWMySW5zdGFuY2UgPSBuZXcgZWMyLkluc3RhbmNlKHRoaXMsICdSZWNpY2xhamVFYzJJbnN0YW5jZScsIHtcbiAgICAgIHZwYyxcbiAgICAgIGluc3RhbmNlVHlwZTogZWMyLkluc3RhbmNlVHlwZS5vZihlYzIuSW5zdGFuY2VDbGFzcy5UNEcsIGVjMi5JbnN0YW5jZVNpemUuU01BTEwpLFxuICAgICAgbWFjaGluZUltYWdlOiBlYzIuTWFjaGluZUltYWdlLmxhdGVzdEFtYXpvbkxpbnV4MjAyMyh7XG4gICAgICAgIGNwdVR5cGU6IGVjMi5BbWF6b25MaW51eENwdVR5cGUuQVJNXzY0LFxuICAgICAgfSksXG4gICAgICBzZWN1cml0eUdyb3VwOiBlYzJTZWN1cml0eUdyb3VwLFxuICAgICAgcm9sZTogZWMyUm9sZSxcbiAgICAgIHVzZXJEYXRhOiB1c2VyRGF0YSxcbiAgICAgIHZwY1N1Ym5ldHM6IHsgc3VibmV0VHlwZTogZWMyLlN1Ym5ldFR5cGUuUFVCTElDIH0sXG4gICAgfSk7XG5cbiAgICAvLyA3LiBEaXJlY2Npw7NuIElQIEVsw6FzdGljYSAoRWxhc3RpYyBJUCkgZmlqYVxuICAgIGNvbnN0IGVpcCA9IG5ldyBlYzIuQ2ZuRUlQKHRoaXMsICdSZWNpY2xhamVFbGFzdGljSVAnLCB7XG4gICAgICBpbnN0YW5jZUlkOiBlYzJJbnN0YW5jZS5pbnN0YW5jZUlkLFxuICAgIH0pO1xuXG4gICAgLy8gT3V0cHV0IE91dHB1dHMgZGUgbGEgSW5mcmFlc3RydWN0dXJhXG4gICAgbmV3IGNkay5DZm5PdXRwdXQodGhpcywgJ0J1Y2tldE5hbWVPdXRwdXQnLCB7XG4gICAgICB2YWx1ZTogZm90b3NCdWNrZXQuYnVja2V0TmFtZSxcbiAgICAgIGRlc2NyaXB0aW9uOiAnTm9tYnJlIGRlbCBCdWNrZXQgUzMgcGFyYSBGb3RvcyBkZSBJbnNwZWNjacOzbicsXG4gICAgfSk7XG5cbiAgICBuZXcgY2RrLkNmbk91dHB1dCh0aGlzLCAnUHVibGljSXBPdXRwdXQnLCB7XG4gICAgICB2YWx1ZTogZWlwLnJlZixcbiAgICAgIGRlc2NyaXB0aW9uOiAnSVAgUHVibGljYSBFbMOhc3RpY2EgZGUgbGEgSW5zdGFuY2lhIEVDMiBHcmF2aXRvbicsXG4gICAgfSk7XG5cbiAgICBuZXcgY2RrLkNmbk91dHB1dCh0aGlzLCAnQXBwVXJsT3V0cHV0Jywge1xuICAgICAgdmFsdWU6IGBodHRwOi8vJHtlaXAucmVmfWAsXG4gICAgICBkZXNjcmlwdGlvbjogJ1VSTCBkZSBhY2Nlc28gZGlyZWN0byBhIGxhIEFwbGljYWNpb24gV2ViIGRlIFJlY2ljbGFqZSBMaXRvcmFsJyxcbiAgICB9KTtcbiAgfVxufVxuIl19