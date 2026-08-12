# Infraestructura en AWS con AWS CDK (TypeScript) - Reciclaje Litoral

Este directorio contiene la definición declarativa de infraestructura como código (**IaC**) mediante **AWS CDK (v2)** para aprovisionar automáticamente todos los recursos de **AWS** requeridos por la aplicación de **Reciclaje Litoral**.

---

## 1. Recursos Aprovisionados por el Stack de CDK

* **S3 Bucket (`reciclaje-litoral-fotos-<account>-<region>`)**: Almacenamiento optimizado de imágenes con políticas CORS configuradas para subida y lectura de fotos de inspección.
* **VPC Privada**: Diseñada para costo cero en redes (Subredes públicas sin cobros por NAT Gateway).
* **Security Group**: Firewall configurado para tráfico HTTP (Puerto `80`), HTTPS (Puerto `443`) y SSH (Puerto `22`).
* **IAM Role & Instance Profile**: Otorga permisos nativos a la instancia EC2 para interactuar con S3 sin almacenar credenciales en el código.
* **Instancia EC2 Graviton (`t4g.small`)**: Instancia de arquitectura ARM64 con Amazon Linux 2023 (~$6-$8 USD/mes).
* **Elastic IP (EIP)**: Dirección IP pública estática reservada para el servidor.

---

## 2. Requisitos Previos

1. Tener configurado **AWS CLI** con tus credenciales:
   ```bash
   aws configure
   ```
2. Tener instalado **Node.js 18+** y **npm**.
3. Instalar **AWS CDK CLI** globalmente:
   ```bash
   npm install -g aws-cdk
   ```

---

## 3. Instrucciones Paso a Paso para Desplegar

### Paso 1: Instalar Dependencias del Proyecto CDK
Navega a la carpeta `reciclaje-cdk` e instala las dependencias:

```bash
cd /Volumes/Mac-Storage/Documents/personal-nfx/reciclaje-app/reciclaje-cdk
npm install
```

### Paso 2: Inicializar la Cuenta de AWS (CDK Bootstrap)
Si es la primera vez que usas AWS CDK en tu cuenta o región de AWS, ejecuta:

```bash
cdk bootstrap
```

### Paso 3: Sintetizar la Plantilla de CloudFormation (Opcional)
Verifica que la plantilla CloudFormation generada sea válida:

```bash
cdk synth
```

### Paso 4: Desplegar la Infraestructura en AWS
Despliega todo el stack en tu cuenta de AWS con un solo comando:

```bash
cdk deploy
```

CDK te mostrará un resumen de las políticas de IAM y recursos a crear. Presiona `y` para confirmar.

 Al finalizar el despliegue, la terminal te entregará los **Outputs**:
* `PublicIpOutput`: La IP pública estática de tu instancia.
* `BucketNameOutput`: El nombre del Bucket S3 creado.
* `AppUrlOutput`: La URL pública para acceder a tu aplicación (`http://<IP-ELASTICA>`).

---

## 4. Destruir la Infraestructura (Eliminar Costos)

Si deseas eliminar todos los recursos creados en AWS para evitar cobros futuros:

```bash
cdk destroy
```
