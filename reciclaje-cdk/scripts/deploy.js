#!/usr/bin/env node

/**
 * Script de Despliegue Automatizado en AWS (Zero-SSH) vía AWS Systems Manager (SSM)
 * 
 * Modos disponibles:
 *   --mode=clean   : Respalda S3 a docs/s3/backup_<timestamp>, vacía S3, elimina volúmenes de PostgreSQL y reconstruye todo limpio.
 *   --mode=update  : Conserva S3 y PostgreSQL, descarga últimos cambios de Git y recompila contenedores backend y frontend.
 * 
 * Opciones adicionales:
 *   --region=<region> : Región de AWS (por defecto: us-east-1)
 *   --stack=<nombre>  : Nombre del stack de CloudFormation (por defecto: ReciclajeLitoralStack)
 */

const { execSync, spawnSync, execFileSync } = require('child_process');
const fs = require('fs');
const path = require('path');

// Colores para consola
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  dim: '\x1b[2m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  magenta: '\x1b[35m',
  cyan: '\x1b[36m',
  red: '\x1b[31m',
};

function log(msg, color = colors.reset) {
  console.log(`${color}${msg}${colors.reset}`);
}

function logStep(step, total, msg) {
  console.log(`\n${colors.bright}${colors.cyan}[${step}/${total}] ${msg}${colors.reset}`);
}

// 1. Parsear argumentos
const args = process.argv.slice(2);
let mode = 'update';
let region = process.env.AWS_REGION || process.env.CDK_DEFAULT_REGION || 'us-east-1';
let stackName = 'ReciclajeLitoralStack';

for (const arg of args) {
  if (arg.startsWith('--mode=')) {
    mode = arg.split('=')[1].toLowerCase();
  } else if (arg.startsWith('--region=')) {
    region = arg.split('=')[1];
  } else if (arg.startsWith('--stack=')) {
    stackName = arg.split('=')[1];
  }
}

if (mode !== 'clean' && mode !== 'update') {
  log(`⚠️ Modo desconocido: "${mode}". Opciones válidas: --mode=clean o --mode=update`, colors.red);
  process.exit(1);
}

log('========================================================================', colors.magenta);
log('   🚀 DESPLIEGUE AUTOMATIZADO RECICLAJE LITORAL (AWS SSM / ZERO-SSH)   ', colors.bright + colors.magenta);
log('========================================================================', colors.magenta);
log(`Modo seleccionado : ${colors.bright}${mode.toUpperCase()}${colors.reset}`);
log(`Región AWS        : ${region}`);
log(`Stack CDK         : ${stackName}\n`);

// 2. Obtener información de la infraestructura desde CloudFormation
log('🔍 Obteniendo parámetros del stack CloudFormation...', colors.dim);
let bucketName = '';
let publicIp = '';
let instanceId = '';

try {
  const cfnRaw = execSync(
    `aws cloudformation describe-stacks --stack-name "${stackName}" --region "${region}" --query "Stacks[0].Outputs" --output json`,
    { encoding: 'utf8' }
  );
  const outputs = JSON.parse(cfnRaw);

  for (const out of outputs) {
    if (out.OutputKey === 'BucketNameOutput') bucketName = out.OutputValue;
    if (out.OutputKey === 'PublicIpOutput') publicIp = out.OutputValue;
    if (out.OutputKey === 'InstanceIdOutput') instanceId = out.OutputValue;
  }
} catch (e) {
  log(`❌ Error al consultar CloudFormation para el stack ${stackName}: ${e.message}`, colors.red);
  process.exit(1);
}

if (!bucketName) {
  log('❌ No se encontró BucketNameOutput en el stack.', colors.red);
  process.exit(1);
}

// Si InstanceIdOutput no está en CloudFormation aún, buscar la instancia por la IP elástica
if (!instanceId && publicIp) {
  log('ℹ️ Buscando InstanceId mediante IP Elástica...', colors.dim);
  try {
    instanceId = execSync(
      `aws ec2 describe-instances --region "${region}" --filters "Name=ip-address,Values=${publicIp}" --query "Reservations[0].Instances[0].InstanceId" --output text`,
      { encoding: 'utf8' }
    ).trim();
  } catch (e) {
    log(`❌ No se pudo resolver el InstanceId: ${e.message}`, colors.red);
    process.exit(1);
  }
}

log(`✅ Bucket S3     : ${colors.green}${bucketName}${colors.reset}`);
log(`✅ IP Pública     : ${colors.green}${publicIp}${colors.reset}`);
log(`✅ Instancia EC2 : ${colors.green}${instanceId}${colors.reset}`);

// 3. Ejecutar según el modo
const totalSteps = mode === 'clean' ? 4 : 2;

if (mode === 'clean') {
  // PASO 1: Respaldo preventivo automático de S3
  const now = new Date();
  const pad = (n) => String(n).padStart(2, '0');
  const timestamp = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}_${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`;
  const repoRoot = path.resolve(__dirname, '../..');
  const backupDir = path.join(repoRoot, 'docs', 's3', `backup_${timestamp}`);

  logStep(1, totalSteps, `Respaldo preventivo automático de S3`);
  log(`Destino local: ${colors.cyan}${backupDir}${colors.reset}`);

  if (!fs.existsSync(backupDir)) {
    fs.mkdirSync(backupDir, { recursive: true });
  }

  try {
    log('Descargando objetos de S3...', colors.dim);
    execSync(`aws s3 sync "s3://${bucketName}" "${backupDir}" --region "${region}"`, {
      stdio: 'inherit',
    });
    log(`✅ Respaldo de S3 completado exitosamente en: docs/s3/backup_${timestamp}`, colors.green);
  } catch (e) {
    log(`❌ Falló la descarga de respaldo de S3: ${e.message}`, colors.red);
    log('Operación abortada para proteger los datos.', colors.red);
    process.exit(1);
  }

  // PASO 2: Vaciado del Bucket S3
  logStep(2, totalSteps, `Vaciando Bucket S3 (${bucketName})`);
  try {
    execSync(`aws s3 rm "s3://${bucketName}" --recursive --region "${region}"`, {
      stdio: 'inherit',
    });
    log(`✅ Bucket S3 vaciado correctamente.`, colors.green);
  } catch (e) {
    log(`❌ Error al vaciar S3: ${e.message}`, colors.red);
    process.exit(1);
  }

  // PASO 3: Instalación Limpia en EC2 (Docker down -v + build)
  logStep(3, totalSteps, `Ejecutando instalación limpia en EC2 vía AWS SSM`);
  const cleanCmd = [
    'export HOME=/root',
    'git config --system --add safe.directory /home/ec2-user/reciclaje-app',
    'echo "Descargando variables de entorno desde AWS SSM Parameter Store..."',
    `aws ssm get-parameters-by-path --region "${region}" --path "/reciclaje-app/prod/" --with-decryption --query "Parameters[*].[Name,Value]" --output text | while IFS="$(printf '\\t')" read -r name val; do echo "\${name##*/}=\$val"; done > /home/ec2-user/reciclaje-app/.env`,
    'chmod 600 /home/ec2-user/reciclaje-app/.env',
    'chown -R ec2-user:ec2-user /home/ec2-user/reciclaje-app',
    'sudo -u ec2-user -i sh -c "cd /home/ec2-user/reciclaje-app && git fetch origin main && git reset --hard origin/main && docker compose down -v && docker compose up -d --build"',
  ];

  executeSsmCommand(instanceId, region, cleanCmd, 'Instalación Limpia Docker');

  // PASO 4: Verificación final
  logStep(4, totalSteps, `Verificando estado de los contenedores`);
  checkContainersStatus(instanceId, region, publicIp);

} else {
  // MODO UPDATE
  logStep(1, totalSteps, `Conservando S3 y Base de Datos (Modo Actualización)`);
  log(`ℹ️ Las fotos en S3 y los datos en PostgreSQL se mantienen intactos.`, colors.yellow);
  log(`ℹ️ Flyway aplicará automáticamente las nuevas migraciones al iniciar el backend.`, colors.yellow);

  logStep(2, totalSteps, `Descargando código y actualizando contenedores en EC2 vía AWS SSM`);
  const updateCmd = [
    'export HOME=/root',
    'git config --system --add safe.directory /home/ec2-user/reciclaje-app',
    'echo "Actualizando variables de entorno desde AWS SSM Parameter Store..."',
    `aws ssm get-parameters-by-path --region "${region}" --path "/reciclaje-app/prod/" --with-decryption --query "Parameters[*].[Name,Value]" --output text | while IFS="$(printf '\\t')" read -r name val; do echo "\${name##*/}=\$val"; done > /home/ec2-user/reciclaje-app/.env`,
    'chmod 600 /home/ec2-user/reciclaje-app/.env',
    'chown -R ec2-user:ec2-user /home/ec2-user/reciclaje-app',
    'sudo -u ec2-user -i sh -c "cd /home/ec2-user/reciclaje-app && git fetch origin main && git reset --hard origin/main && docker compose up -d --build --no-deps backend frontend"',
  ];

  executeSsmCommand(instanceId, region, updateCmd, 'Actualización Incremental Docker');

  checkContainersStatus(instanceId, region, publicIp);
}

// -----------------------------------------------------------------------------
// Funciones auxiliares para AWS SSM
// -----------------------------------------------------------------------------

function executeSsmCommand(instId, reg, commands, description) {
  log(`Iniciando ejecución remota en ${instId}...`, colors.dim);

  let sendResult;

  try {
    const raw = execFileSync(
      'aws',
      [
        'ssm', 'send-command',
        '--region', reg,
        '--instance-ids', instId,
        '--document-name', 'AWS-RunShellScript',
        '--parameters', JSON.stringify({ commands }),
        '--comment', description,
        '--output', 'json'
      ],
      { encoding: 'utf8' }
    );
    sendResult = JSON.parse(raw);
  } catch (e) {
    log(`❌ Error al enviar comando SSM: ${e.message}`, colors.red);
    process.exit(1);
  }

  const commandId = sendResult.Command?.CommandId;
  log(`Comando SSM enviado: ${colors.cyan}${commandId}${colors.reset}`);
  log('Esperando ejecución y compilación de Docker en EC2 (esto puede tardar 1-3 minutos)...', colors.yellow);

  const startTime = Date.now();
  let status = 'Pending';
  let invocationData = null;

  while (status === 'Pending' || status === 'InProgress' || status === 'Delayed') {
    // Pausa de 3 segundos
    spawnSync('sleep', ['3']);

    const elapsed = Math.round((Date.now() - startTime) / 1000);
    process.stdout.write(`\r⏳ En progreso (${elapsed}s)... `);

    try {
      const invRaw = execFileSync(
        'aws',
        [
          'ssm', 'get-command-invocation',
          '--region', reg,
          '--command-id', commandId,
          '--instance-id', instId,
          '--output', 'json'
        ],
        { encoding: 'utf8', stdio: ['pipe', 'pipe', 'ignore'] }
      );
      invocationData = JSON.parse(invRaw);
      status = invocationData.Status;
    } catch (e) {
      // Puede arrojar InvocationDoesNotExist durante los primeros segundos
    }
  }

  console.log('\n');

  if (status === 'Success') {
    log(`\n🎉 Ejecución remota completada con éxito!`, colors.bright + colors.green);
    if (invocationData?.StandardOutputContent) {
      log('\n--- [Salida de EC2] ---', colors.dim);
      console.log(invocationData.StandardOutputContent);
      log('-----------------------\n', colors.dim);
    }
  } else {
    log(`\n❌ Error en la ejecución remota de SSM. Estado: ${status}`, colors.red);
    if (invocationData?.StandardErrorContent) {
      log('\n--- [Error de EC2] ---', colors.red);
      console.error(invocationData.StandardErrorContent);
      log('-----------------------\n', colors.red);
    }
    if (invocationData?.StandardOutputContent) {
      console.log(invocationData.StandardOutputContent);
    }
    process.exit(1);
  }
}

function checkContainersStatus(instId, reg, ip) {
  try {
    const raw = execSync(
      `aws ssm send-command --region "${reg}" --instance-ids "${instId}" --document-name "AWS-RunShellScript" --parameters 'commands=["sudo -u ec2-user -i sh -c \\"cd /home/ec2-user/reciclaje-app && docker compose ps\\""]' --output json`,
      { encoding: 'utf8' }
    );
    const cmdId = JSON.parse(raw).Command?.CommandId;

    spawnSync('sleep', ['4']);

    const invRaw = execSync(
      `aws ssm get-command-invocation --region "${reg}" --command-id "${cmdId}" --instance-id "${instId}" --output json`,
      { encoding: 'utf8' }
    );
    const out = JSON.parse(invRaw);

    log('📋 Estado de los contenedores Docker en AWS:', colors.cyan);
    console.log(out.StandardOutputContent || 'Sin salida');

    log('========================================================================', colors.green);
    log(`  🌐 Aplicación Web disponible en: ${colors.bright}http://${ip}${colors.reset}`, colors.green);
    log('========================================================================\n', colors.green);
  } catch (e) {
    log(`⚠️ No se pudo verificar el estado final de los contenedores: ${e.message}`, colors.yellow);
  }
}
