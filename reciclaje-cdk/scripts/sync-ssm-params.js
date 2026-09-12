#!/usr/bin/env node

/**
 * Script de Sincronización y Consulta de Parámetros en AWS SSM Parameter Store
 * 
 * Uso:
 *   node scripts/sync-ssm-params.js --action=push   (Sube/actualiza parámetros a SSM con cifrado KMS)
 *   node scripts/sync-ssm-params.js --action=list   (Lista los parámetros actuales en AWS SSM)
 * 
 * Opciones:
 *   --env=prod        (Entorno destino: prod | qa | dev, por defecto prod)
 *   --region=us-east-1 (Región de AWS, por defecto us-east-1)
 */

const { execSync, execFileSync } = require('child_process');
const path = require('path');
const fs = require('fs');

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

const args = process.argv.slice(2);
let action = 'push';
let envName = 'prod';
let region = process.env.AWS_REGION || process.env.CDK_DEFAULT_REGION || 'us-east-1';
let stackName = 'ReciclajeLitoralStack';

let envFile = path.resolve(__dirname, '../../.env');

for (const arg of args) {
  if (arg.startsWith('--action=')) action = arg.split('=')[1].toLowerCase();
  else if (arg.startsWith('--env=')) envName = arg.split('=')[1].toLowerCase();
  else if (arg.startsWith('--region=')) region = arg.split('=')[1];
  else if (arg.startsWith('--stack=')) stackName = arg.split('=')[1];
  else if (arg.startsWith('--file=')) envFile = path.resolve(process.cwd(), arg.split('=')[1]);
}

function parseEnvFile(filePath) {
  if (!fs.existsSync(filePath)) return {};
  const content = fs.readFileSync(filePath, 'utf8');
  const env = {};
  for (const line of content.split('\n')) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;
    const eqIdx = trimmed.indexOf('=');
    if (eqIdx === -1) continue;
    const key = trimmed.slice(0, eqIdx).trim();
    let val = trimmed.slice(eqIdx + 1).trim();
    if ((val.startsWith('"') && val.endsWith('"')) || (val.startsWith("'") && val.endsWith("'"))) {
      val = val.slice(1, -1);
    }
    env[key] = val;
  }
  return env;
}

const prefix = `/reciclaje-app/${envName}`;

log('========================================================================', colors.cyan);
log(`   🔐 GESTOR DE PARÁMETROS AWS SSM PARAMETER STORE (KMS / ZERO-COST)   `, colors.bright + colors.cyan);
log('========================================================================', colors.cyan);
log(`Acción     : ${colors.bright}${action.toUpperCase()}${colors.reset}`);
log(`Prefijo    : ${colors.green}${prefix}${colors.reset}`);
log(`Región AWS : ${region}\n`);

// Obtener nombre dinámico del bucket S3 desde CloudFormation
let bucketName = `reciclaje-litoral-fotos-953630283432-${region}`;
try {
  const cfnRaw = execSync(
    `aws cloudformation describe-stacks --stack-name "${stackName}" --region "${region}" --query "Stacks[0].Outputs" --output json`,
    { encoding: 'utf8', stdio: ['pipe', 'pipe', 'ignore'] }
  );
  const outputs = JSON.parse(cfnRaw);
  const bOut = outputs.find(o => o.OutputKey === 'BucketNameOutput');
  if (bOut?.OutputValue) bucketName = bOut.OutputValue;
} catch (e) {
  // Mantener fallback si no está disponible
}

if (action === 'list') {
  log(`Consultando parámetros bajo la ruta "${prefix}/"...`, colors.dim);
  try {
    const raw = execSync(
      `aws ssm get-parameters-by-path --path "${prefix}/" --region "${region}" --query "Parameters[*].[Name,Type,Version,LastModifiedDate]" --output json`,
      { encoding: 'utf8' }
    );
    const params = JSON.parse(raw);

    if (params.length === 0) {
      log(`⚠️ No se encontraron parámetros bajo ${prefix}/`, colors.yellow);
    } else {
      console.log('\n' + 'NOMBRE DEL PARÁMETRO'.padEnd(46) + 'TIPO (KMS)'.padEnd(20) + 'VERSIÓN'.padEnd(10) + 'ÚLTIMA MODIFICACIÓN');
      console.log('-'.repeat(105));
      for (const [name, type, ver, date] of params) {
        const typeBadge = type === 'SecureString' ? `${colors.green}🔒 SecureString${colors.reset}` : `${colors.cyan}📄 String      ${colors.reset}`;
        console.log(`${colors.bright}${name.padEnd(46)}${colors.reset} ${typeBadge}   ${String('v' + ver).padEnd(8)} ${date}`);
      }
      console.log('\n');
    }
  } catch (e) {
    log(`❌ Error al listar parámetros: ${e.message}`, colors.red);
    process.exit(1);
  }
} else if (action === 'push') {
  const localEnv = parseEnvFile(envFile);
  if (fs.existsSync(envFile)) {
    log(`📄 Leyendo variables desde archivo local: ${colors.cyan}${envFile}${colors.reset}\n`, colors.yellow);
  } else {
    log(`ℹ️ Archivo local no encontrado en: ${envFile}. Usando valores por defecto seguros.\n`, colors.dim);
  }

  const paramsTemplate = [
    { name: 'POSTGRES_DB', defaultVal: 'reciclaje_db', type: 'String', desc: 'Nombre de base de datos PostgreSQL' },
    { name: 'POSTGRES_USER', defaultVal: 'reciclaje_user', type: 'String', desc: 'Usuario de base de datos PostgreSQL' },
    { name: 'POSTGRES_PASSWORD', defaultVal: 'reciclaje_pass_2026_prod_secure!', type: 'SecureString', desc: 'Contraseña de base de datos (Cifrado KMS)' },
    { name: 'JWT_SECRET', defaultVal: 'ReciclajeLitoralSuperSecretKey2026WithEnoughBitLengthForHMACSHA256Signature!', type: 'SecureString', desc: 'Clave HMAC-SHA256 para tokens JWT' },
    { name: 'JWT_EXPIRATION_MS', defaultVal: '604800000', type: 'String', desc: 'Tiempo de expiración JWT (7 días)' },
    { name: 'ADMIN_INITIAL_EMAIL', defaultVal: 'admin@reciclajelitoral.cl', type: 'String', desc: 'Email del Administrador General inicial' },
    { name: 'ADMIN_INITIAL_NAME', defaultVal: 'Administrador General', type: 'String', desc: 'Nombre del Administrador General' },
    { name: 'ADMIN_INITIAL_PASSWORD', defaultVal: 'JetPack1977!', type: 'SecureString', desc: 'Contraseña del Administrador General inicial (Cifrado KMS)' },
    { name: 'AWS_REGION', defaultVal: region, type: 'String', desc: 'Región de AWS' },
    { name: 'AWS_S3_BUCKET', defaultVal: bucketName, type: 'String', desc: 'Nombre del Bucket S3 para fotos' },
    { name: 'SPRING_PROFILES_ACTIVE', defaultVal: 'prod', type: 'String', desc: 'Perfil activo de Spring Boot' },
    { name: 'VITE_SHOW_TEST_CREDENTIALS', defaultVal: 'false', type: 'String', desc: 'Ocultar credenciales de prueba en frontend' },
    { name: 'SERVER_PORT', defaultVal: '8080', type: 'String', desc: 'Puerto de la aplicación backend' },
  ];

  const paramsToSync = paramsTemplate.map(p => {
    const hasLocal = localEnv[p.name] !== undefined && localEnv[p.name] !== '';
    return {
      name: p.name,
      value: hasLocal ? localEnv[p.name] : p.defaultVal,
      type: p.type,
      desc: p.desc,
      source: hasLocal ? 'archivo .env' : 'predeterminado',
    };
  });

  log(`Sincronizando ${paramsToSync.length} parámetros hacia AWS SSM Parameter Store...`, colors.dim);

  let count = 0;
  for (const p of paramsToSync) {
    const fullName = `${prefix}/${p.name}`;
    try {
      execFileSync(
        'aws',
        [
          'ssm', 'put-parameter',
          '--name', fullName,
          '--value', p.value,
          '--type', p.type,
          '--description', p.desc,
          '--overwrite',
          '--region', region,
        ],
        { stdio: ['pipe', 'pipe', 'pipe'] }
      );
      const badge = p.type === 'SecureString' ? `${colors.green}[🔒 SecureString/KMS]${colors.reset}` : `${colors.cyan}[📄 String]${colors.reset}`;
      const srcBadge = p.source === 'archivo .env' ? `${colors.yellow}(desde .env)${colors.reset}` : `${colors.dim}(por defecto)${colors.reset}`;
      console.log(`  ✅ Guardado: ${colors.bright}${fullName}${colors.reset} ${badge} ${srcBadge}`);
      count++;
    } catch (e) {
      log(`  ❌ Error guardando ${fullName}: ${e.message}`, colors.red);
    }
  }

  log(`\n🎉 Sincronización completada exitosamente! (${count}/${paramsToSync.length} parámetros actualizados en AWS)\n`, colors.bright + colors.green);
}
