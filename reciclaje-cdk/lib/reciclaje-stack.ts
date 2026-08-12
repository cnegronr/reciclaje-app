import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as s3 from 'aws-cdk-lib/aws-s3';
import * as iam from 'aws-cdk-lib/aws-iam';
import { Construct } from 'constructs';

export class ReciclajeStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
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
    userData.addCommands(
      'sudo dnf update -y',
      'sudo dnf install -y docker git docker-compose-plugin',
      'sudo systemctl enable --now docker',
      'sudo usermod -aG docker ec2-user',
      'mkdir -p /home/ec2-user/app',
      'cd /home/ec2-user/app',
      'echo "Servidor de Reciclaje Litoral listo para desplegar con docker compose up -d --build"'
    );

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
