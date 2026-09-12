# ♻️ Reciclaje Litoral Application

Aplicación web y microservicio REST para el registro, georreferenciación, inspección semanal y seguimiento fotografico de puntos limpios y campanas de reciclaje en el Litoral Central.

---

## 🛠️ Arquitectura y Tecnologías

* **Backend:** Java 21, Spring Boot 3.2.3, Spring Security (JWT Stateless Authentication), Spring Data JPA, Hibernate.
* **Base de Datos & Migraciones:** PostgreSQL 16 con Flyway para control de versiones de BD (`db/migration/`) e inicialización automática vía `init-data.sql`.
* **Organización de Contenedores:** Clasificación por comuna y agrupamiento por `sector` (Algarrobo: 43 contenedores georreferenciados en 5 sectores; El Quisco: 67 contenedores en 7 sectores).
* **Almacenamiento de Fotos:** Amazon S3 con URLs Firmadas Presignadas (`S3Presigner` HMAC-SHA256 7 días) y compresión automática de imágenes (JPEG 75% max 1280px). Modo de simulación local Base64 cuando no se proveen credenciales de AWS.
* **Frontend:** React 18, Vite 8, CSS3 Vanilla (Diseño adaptable glassmorphic).
* **Contenedorización & Orquestación:** Docker, Docker Compose, Nginx (Proxy inverso).
* **Cobertura de Pruebas:** JaCoCo con umbral estricto del **100% de cobertura de instrucciones y ramas** (`<minimum>1.0</minimum>`).

---

## 🔑 Credenciales de Prueba (Entorno Inicial)

Al iniciar la base de datos por primera vez mediante `init-data.sql`, el sistema cuenta con usuarios de prueba precargados.

> 💡 **Ocultar Credenciales en AWS/Producción**: Por defecto las credenciales de prueba se ocultan en compilaciones de producción. Para controlar su visibilidad en el login, configura la variable de entorno `VITE_SHOW_TEST_CREDENTIALS`:
> - `VITE_SHOW_TEST_CREDENTIALS=true`: Muestra el cuadro de credenciales y botones de auto-llenado (Entorno de Desarrollo).
> - `VITE_SHOW_TEST_CREDENTIALS=false`: Oculta el cuadro de credenciales y deja vacíos los campos de login (Entorno AWS / Producción).

| Rol            | Email / Usuario | Contraseña |
|:---------------| :--- | :--- |
| **ADMIN**      | `admin@reciclajelitoral.cl` | `<YourSecurePassword>` |
| **REPORTERIA** | `reporteria@reciclajelitoral.cl` | `<YourSecurePassword>` |
| **INSPECTOR**  | `inspector@reciclajelitoral.cl` | `<YourSecurePassword>` |
| **CHOFER 1**   | `chofer@reciclajelitoral.cl` | `<YourSecurePassword>` |
| **CHOFER 2**   | `chofer2@reciclajelitoral.cl` | `<YourSecurePassword>` |

---

## 🚀 Guía de Comandos y Operación

### 🐳 Comandos Docker & Docker Compose (Raíz del proyecto `/reciclaje-app`)

#### 1. Reconstrucción Limpia y Reinicio de Base de Datos (Reset Completo)
Elimina volúmenes persistentes de PostgreSQL, reconstruye imágenes y levanta el stack completo en segundo plano:
```bash
docker compose down -v && docker compose up -d --build
```

#### 2. Reconstruir y Levantar Manteniendo Datos Existentes
Reconstruye las imágenes de frontend y backend sin borrar la base de datos PostgreSQL:
```bash
docker compose up -d --build
```

#### 3. Detener Contenedores
Detiene los contenedores en ejecución sin eliminar volúmenes:
```bash
docker compose down
```

#### 4. Ver Logs en Tiempo Real
Ver logs consolidados de todos los contenedores:
```bash
docker compose logs -f
```

Ver logs únicamente del microservicio backend:
```bash
docker compose logs -f backend
```

Ver logs únicamente del servidor web frontend Nginx:
```bash
docker compose logs -f frontend
```

#### 5. Verificar Estado de los Servicios
Muestra el estado de los contenedores (`reciclaje-backend`, `reciclaje-frontend`, `reciclaje-postgres`):
```bash
docker compose ps
```

---

### ☕ Comandos Backend Maven (`/reciclaje-app/reciclaje-backend`)

#### 1. Ejecutar Pruebas Unitarias
Ejecuta la suite completa de pruebas de controladores, servicios y repositorio:
```bash
cd reciclaje-backend
mvn clean test
```

#### 2. Ejecutar Pruebas con Verificación de Cobertura JaCoCo 100%
Ejecuta las pruebas y verifica que la cobertura alcanzada sea del **100% en instrucciones y ramas**:
```bash
cd reciclaje-backend
mvn clean verify
```

#### 3. Compilar Archivo JAR (Omitiendo Pruebas)
Genera el paquete ejecutable `target/reciclaje-backend-1.0.0.jar`:
```bash
cd reciclaje-backend
mvn clean package -DskipTests
```

#### 4. Ejecutar Backend Localmente (Sin Docker)
Levanta el servidor Spring Boot directamente en el puerto `8080`:
```bash
cd reciclaje-backend
mvn spring-boot:run
```

---

### ⚡ Comandos Frontend Node / Vite (`/reciclaje-app/reciclaje-web-poc`)

#### 1. Instalar Dependencias
```bash
cd reciclaje-web-poc
npm install
```

#### 2. Iniciar Servidor de Desarrollo Vite
Levanta el servidor dev local con hot-reload en `http://localhost:5173`:
```bash
cd reciclaje-web-poc
npm run dev
```

#### 3. Compilar Bundle de Producción
Compila y optimiza el código React en el directorio `dist/`:
```bash
cd reciclaje-web-poc
npm run build
```

#### 4. Previsualizar Build de Producción Localmente
```bash
cd reciclaje-web-poc
npm run preview
```

---

### ☁️ Comandos de Despliegue en AWS (AWS CDK + AWS SSM - Zero-SSH)

Desde la carpeta `/reciclaje-app/reciclaje-cdk`:

#### 1. Gestión de Variables de Entorno y Secretos en AWS SSM Parameter Store (Costo $0.00)
* **Sincronizar parámetros hacia AWS con cifrado KMS (Lee tu `.env` local):**
  Lee automáticamente tu archivo `.env` local de la raíz del proyecto (o cualquier archivo especificado con `-- --file=ruta/.env`), toma los valores definidos y los sube a AWS Systems Manager Parameter Store bajo `/reciclaje-app/prod/`, aplicando cifrado KMS (`SecureString`) a contraseñas y claves JWT:
  ```bash
  cd reciclaje-cdk
  npm run ssm:push
  # Opcional: especificar un archivo .env personalizado
  npm run ssm:push -- --file=../.env.production
  ```
* **Listar y auditar parámetros actuales en AWS SSM:**
  Muestra una tabla con los nombres, tipos de parámetro (`[🔒 SecureString/KMS]` o `[📄 String]`), versiones y fechas de modificación:
  ```bash
  cd reciclaje-cdk
  npm run ssm:list
  ```

#### 2. Despliegue de Actualizaciones (Modo Recomendado)
Descarga el código actualizado de GitHub (`main`), sincroniza el `.env` dinámicamente desde AWS SSM, recompila los contenedores y ejecuta migraciones de Flyway sin tocar las fotos en S3 ni la base de datos PostgreSQL:
```bash
cd reciclaje-cdk
npm run deploy:update
```

#### 3. Instalación Limpia con Respaldo Preventivo de S3 (Reset Total)
Descarga automáticamente todas las fotos de S3 en `docs/s3/backup_<timestamp>`, vacía el bucket S3 en AWS, descarga el `.env` actualizado desde AWS SSM, elimina los volúmenes de PostgreSQL y reconstruye todo limpio desde cero:
```bash
cd reciclaje-cdk
npm run deploy:clean
```

#### 4. Aprovisionamiento Inicial de Infraestructura con CDK
Si la infraestructura no existe, crea la VPC, Bucket S3, Security Groups, Rol IAM con permisos SSM/KMS e instancia EC2 Graviton:
```bash
cd reciclaje-cdk
npm install
cdk deploy -c keyName=mi-llave-ssh
```

#### 5. Destrucción de Infraestructura (Eliminar Costos)
Elimina todos los recursos aprovisionados en AWS:
```bash
cd reciclaje-cdk
cdk destroy
```

---

## 🌐 URLs de Acceso y Puertos

| Servicio | URL de Acceso | Descripción |
| :--- | :--- | :--- |
| **Aplicación Web (Producción AWS)** | `http://<IP-PUBLICA-EC2>` | Servida vía Nginx Proxy en el puerto 80 (Instancia Graviton) |
| **Aplicación Web (Docker Local)** | `http://localhost` | Interfaz de usuario servida vía Nginx Proxy local |
| **Aplicación Web (Vite Dev)** | `http://localhost:5173` | Servidor de desarrollo local con hot-reload |
| **API REST Backend (Spring Boot)** | `http://localhost:8080/api` | Endpoints REST de autenticación, administración e inspecciones |
| **Base de Datos PostgreSQL** | `localhost:5432` | BD `reciclaje_db` (Usuario: `postgres` / Clave: `postgres`) |

---

## 🌟 Mejoras Recientes e Instrucciones de Operación

A continuación se detallan las mejoras arquitectónicas, de seguridad, usabilidad y despliegue incorporadas al sistema:

### 1. Despliegue Automatizado en AWS sin SSH (Zero-SSH vía AWS Systems Manager)
* **Eliminación de SSH Manual:** Se reemplazó el acceso manual por terminal SSH por comandos remotos autenticados mediante **AWS Systems Manager (SSM Run Command)** utilizando el rol IAM nativo `AmazonSSMManagedInstanceCore`.
* **Dos Modos de Operación Explicitos:**
  * **`deploy:update`:** Ideal para el día a día. Preserva todas las fotos en S3 y todos los registros en PostgreSQL. Ejecuta `git pull`, reconstruye imágenes de backend y frontend, y permite que Flyway aplique automáticamente migraciones pendientes (ej. `V7`).
  * **`deploy:clean`:** Ideal para reinicios completos. Antes de cualquier borrado, **ejecuta un respaldo preventivo descargando todas las fotos del bucket S3** en `docs/s3/backup_<timestamp>/`. Tras verificar el respaldo, vacía el bucket en AWS, elimina los volúmenes de Docker (`down -v`) y levanta todo limpio.
* **Gestión Segura de Secretos con AWS SSM Parameter Store (Costo $0.00):**
  * **Ningún secreto en Git:** El archivo `.env` está en `.gitignore` y **nunca se sube a GitHub**.
  * **Cifrado KMS Nativo:** Variables altamente confidenciales (`POSTGRES_PASSWORD`, `JWT_SECRET`, `ADMIN_INITIAL_PASSWORD`) se almacenan como tipo `SecureString` cifradas automáticamente con AWS KMS.
  * **Aprovisionamiento Dinámico en EC2:** En el arranque de la instancia (`UserData`) y en cada despliegue (`deploy:update` / `deploy:clean`), la instancia EC2 consulta directamente a AWS SSM mediante su rol IAM, genera `/home/ec2-user/reciclaje-app/.env` y restringe sus permisos a `chmod 600` (lectura exclusiva de root/ec2-user).
  * **Comandos CLI Dedicados:** Scripts `npm run ssm:push` y `npm run ssm:list` para actualizar y auditar parámetros sin necesidad de iniciar sesión en la consola web de AWS ni tocar archivos en el servidor.

### 2. Alternador de Visibilidad de Contraseña (Ver / Ocultar)
* **Botón Interactivo:** Incorporación de toggle interactivo con iconos `👁️` (mostrar) y `🙈` (ocultar) en:
  * Pantalla de Login ([`LoginScreen.jsx`](file:///Volumes/Mac-Storage/Documents/personal-nfx/reciclaje-app/reciclaje-web-poc/src/components/LoginScreen.jsx)).
  * Modal de Gestión de Usuarios ([`UserManagementTab.jsx`](file:///Volumes/Mac-Storage/Documents/personal-nfx/reciclaje-app/reciclaje-web-poc/src/components/admin/UserManagementTab.jsx)).
* **Reseteo Automático:** Al abrir o cerrar el modal de usuario, o al enviar el formulario, el campo resetea su estado a oculto por defecto para proteger la privacidad del operador.

### 3. Manejo Seguro de Excepciones Backend & Feedback Moderno Frontend
* **Sanitización de Errores en Backend (`GlobalExceptionHandler.java`):**
  * Los errores 500 no controlados ya no exponen mensajes de error técnicos ni trazas internas de la base de datos al cliente; devuelven un mensaje genérico seguro y registran la traza completa internamente con SLF4J.
  * Manejadores dedicados para `DataIntegrityViolationException` (HTTP 409), `AccessDeniedException` (HTTP 403) y `NoSuchElementException` (HTTP 404).
* **Sistema Global de Feedback (`FeedbackContext.jsx`):**
  * Se eliminaron todas las ventanas nativas emergentes (`alert()` y `confirm()`).
  * Notificaciones Toasts no bloqueantes (`showSuccess`, `showError`, `showWarning`, `showInfo`) con temporizador de desvanecimiento automático.
  * Modales asíncronos de Confirmación con diseño glassmorphic y promesas nativas.

### 4. Gestión Avanzada de Usuarios y Desvinculación de Comunas
* **Desvinculación Automática al Desactivar:** Al marcar a un usuario como inactivo (`activo = false`), se eliminan automáticamente sus asignaciones en `asignaciones_inspector`, liberando las comunas para que figuren como `Sin Asignar`.
* **Reactivación Manual:** Al reactivar a un inspector desactivado, sus comunas previas no se restauran automáticamente; deben ser asignadas de forma explícita y manual.
* **Integridad Histórica:** Se mantienen intactos todos los registros de inspecciones previas, fotos, porcentajes y kilos calculados para auditoría y trazabilidad.

### 5. Soft Delete Obligatorio, Auditoría Histórica y Bloqueo de Hard Delete
* **Tabla de Auditoría Flyway V7 (`historial_asignaciones_comuna`):** Registra cada asignación, desasignación o desactivación con autoría del administrador, fecha/hora inmutable y motivo.
* **Protección de Hard Delete:** Se bloquea el borrado físico (`hardDeleteUser`) si el usuario posee cualquier registro histórico en inspecciones, fotos o detalles, obligando a mantenerlo en estado *inactivo* para preservar la integridad relacional.

### 6. Invalidación Inmediata de Sesión y Detección 401/403
* **Cierre Inmediato de Sesión:** Si un administrador desactiva a un usuario, su sesión activa es invalidada en el backend.
* **Interceptor Reactivo:** El frontend detecta inmediatamente cualquier respuesta HTTP `401` o `403` a través de un interceptor global y redirige al login mostrando un banner explicativo persistente.

### 7. Optimización de Concurrencia y Uso de Recursos en EC2 (Zero-Cost Scaling)
Para maximizar el aprovechamiento de la instancia EC2 `t4g.small` (2 vCPUs ARM64 Graviton, 2.0 GB RAM) bajo ráfagas de peticiones concurrentes sin requerir autoescalado ni incurrir en costos adicionales ($0.00), se implementó una suite integral de optimizaciones en backend, frontend, servidor web y base de datos:

#### A. Backend: Java 21 Virtual Threads (Project Loom) y Calibración de HikariCP
* **Virtual Threads (`spring.threads.virtual.enabled: true`):**
  * *Fundamento Técnico:* Tradicionalmente, cada petición en Tomcat consume un hilo del sistema operativo (OS thread, ~1 MB de stack). Con 2 vCPUs, decenas de peticiones concurrentes compiten por ciclos de CPU en cambios de contexto (*context switching*). Los **Virtual Threads** son hilos livianos administrados directamente por la JVM en memoria de usuario (~kilobytes). Cuando una petición espera I/O (ej. consulta SQL o respuesta de S3), el hilo virtual se desmonta del hilo portador (*carrier thread*), permitiendo que los 2 vCPUs continúen procesando otras solicitudes sin bloquearse.
* **Calibración del Pool de Conexiones HikariCP:**
  * *Fundamento Técnico:* Aunque los hilos virtuales permiten miles de peticiones concurrentes, la base de datos PostgreSQL tiene límites físicos de concurrencia en disco y CPU. Si se permitiera un pool ilimitado, PostgreSQL sufriría contención masiva de bloqueos (*lock contention*) y thrashing de disco. Se dimensionó el pool según la fórmula óptima de PostgreSQL `(vCPUs * 2) + disco` en `maximum-pool-size: 15` con `connection-timeout: 10000ms` (10s), garantizando respuesta ágil o descarte controlado en lugar de colapso por inanición de recursos.
* **Compresión HTTP Nativa (`server.compression.enabled: true`):**
  * *Fundamento Técnico:* Comprime automáticamente con Gzip respuestas JSON mayores a 1024 bytes, reduciendo drásticamente el ancho de banda transferido y la latencia percibida por clientes móviles.

#### B. Gestión de Memoria JVM y Prevención del Linux OOM Killer
* **Configuración del Heap y Algoritmo de Garbage Collection:**
  * Parámetros aplicados: `JAVA_OPTS="-Xms256m -Xmx650m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:MaxRAMPercentage=75.0"`.
  * *Fundamento Técnico:* En una instancia de 2.0 GB compartida entre el SO, Docker, PostgreSQL, Nginx y Java, dejar que la JVM asigne memoria sin tope provocaría la ejecución del *Linux Out-Of-Memory (OOM) Killer*, matando el proceso del backend. Se limitó el heap máximo a 650 MB utilizando **G1GC** (óptimo para baja latencia en arquitecturas multi-núcleo ARM64) y `StringDeduplication` para reducir la memoria consumida por cadenas repetitivas (JSON keys, tokens, URIs).
  * A nivel de Docker Compose, se asignaron techos estrictos: `backend: 850m`, `postgres: 384m`, `frontend: 128m`.

#### C. Servidor Web Nginx: Keepalive Upstream, Gzip y Caché Inmutable
* **Pool de Conexiones Persistentes (`upstream backend_api` con `keepalive 32`):**
  * *Fundamento Técnico:* Por defecto, Nginx abre y cierra una conexión TCP (handshake SYN/ACK de 3 vías) hacia el backend por cada petición entrante. Con el pool *keepalive*, se reutilizan hasta 32 conexiones HTTP/1.1 persistentes, eliminando el coste de establecimiento de sockets y reduciendo la latencia de cada request en 10-30 ms.
* **Caché Inmutable para Bundles Estáticos de Vite:**
  * Directiva: `Cache-Control: public, max-age=31536000, immutable` para `.js`, `.css`, imágenes y fuentes.
  * *Fundamento Técnico:* Los artefactos generados por Vite incluyen un hash de contenido único en el nombre (ej. `index-B8llZd9j.js`). Esta directiva instruye a los navegadores a almacenar el archivo localmente por 1 año sin volver a consultar al servidor mientras el hash no cambie.
  * Por el contrario, `index.html` utiliza `Cache-Control: no-cache, no-store, must-revalidate`, garantizando que cualquier nuevo despliegue sea adoptado al instante.
* **Escudo Defensivo de Tasa de Peticiones (Rate Limiting):**
  * Directiva: `limit_req_zone $binary_remote_addr zone=api_limit:10m rate=30r/s burst=50 nodelay;`.
  * *Fundamento Técnico:* Previene la degradación del servicio ante bucles anormales de clientes o scripts desmedidos, absorbiendo picos de hasta 50 peticiones concurrentes y mitigando abusos sin afectar a usuarios legítimos.

#### D. Frontend SPA: Deduplicación de Peticiones y Caché en Memoria
* **Deduplicación en Vuelo y Caché TTL (`comunaService.js`):**
  * *Fundamento Técnico:* Si múltiples vistas o componentes solicitan simultáneamente el listado de comunas/contenedores, la promesa en curso se comparte entre todos los invocadores (`inFlightRequests`), disparando **una sola petición HTTP** al backend. Adicionalmente, el resultado se almacena en memoria durante 30 segundos (`CACHE_TTL_MS = 30000`), reduciendo la carga de lectura en la API en más de un 80% durante la navegación interactiva.

#### E. Base de Datos: Afinamiento de PostgreSQL 16
* **Parámetros de Ejecución:**
  * Comando: `postgres -c max_connections=50 -c shared_buffers=128MB -c work_mem=4MB`.
  * *Fundamento Técnico:* Limitar `max_connections` a 50 evita que ráfagas descontroladas consuman toda la memoria de PostgreSQL (cada conexión en Postgres reserva memoria para procesos backend). `shared_buffers=128MB` garantiza que las tablas y consultas frecuentes residan en la memoria RAM compartida sin ahogar a los demás contenedores.

#### F. Resultados Verificados en Producción (AWS EC2)
Tras desplegar los cambios en la instancia en vivo (`54.166.102.4`), se ejecutó un benchmark de 30 peticiones concurrentes simultáneas:
* **Tasa de éxito:** 30/30 (100% código HTTP 200).
* **Latencia promedio:** 1.25 segundos (sobre Internet público).
* **Consumo de memoria consolidado:**
  * Backend Spring Boot: **407 MiB** (47.9% del límite asignado).
  * Base de datos PostgreSQL: **40.6 MiB** (10.6% del límite asignado).
  * Servidor web Frontend: **3.9 MiB** (3.1% del límite asignado).
  * **Uso total del stack:** **~452 MiB** (~22% de la RAM total de la máquina), garantizando alta resiliencia y estabilidad continua.

---

## 📋 Reglas de Negocio Integradas

1. **Asignación Única de Inspectores por Comuna:**
   * Cada comuna posee a lo sumo **un único inspector asignado** (`CONSTRAINT unique_comuna_inspector UNIQUE (comuna_id)`).
   * Los inspectores acceden únicamente a sus comunas asignadas (ej. Carlos Negrón en *El Quisco* y *Algarrobo*).
   * Los administradores y choferes tienen acceso global a todas las comunas y contenedores.

2. **Categorías de Contenedores:**
   * `EMPRESA`: Capacidad máxima de 500 kg. Cálculo: `(porcentaje / 100) * 500.0`.
   * `MUNICIPAL`: Capacidad máxima de 1000 kg. Cálculo: `(porcentaje / 100) * 1000.0`.

3. **Georreferenciación & Navegación GPS:**
   * En la ficha de cada contenedor y en el **Mapa de Georreferenciación**, el botón **`🚘 Manejar hacia ubicación`** abre Google Maps Directions (`/maps/dir/?api=1&destination={lat},{lng}&travelmode=driving`), calculando la ruta en tiempo real desde la ubicación GPS del dispositivo.

4. **Traspaso de Visitas y Limpieza de Semana:**
   * Solamente los usuarios con **`ROL = ADMIN`** pueden ejecutar y previsualizar el traspaso de visitas previas a la semana actual.
   * Los Inspectores disponen de las funciones de limpieza de semana actual y de la acción de reversión inmediata (**`⏪ Deshacer ÚLTIMA Acción`**).

5. **Gestión de Usuarios y Borrado Definitivo (Hard Delete):**
   * En la vista de administración de usuarios, la reasignación de comunas presenta distintivos y advertencias de confirmación antes de transferir comunas de un inspector a otro.
   * La eliminación definitiva está restringida únicamente a usuarios sin registros de inspección previos; usuarios con historial solo admiten desactivación (soft delete).

6. **Dashboard de Métricas Simplificado & Filtros:**
   * Interfaz simplificada y amigable con filtros por periodo (Esta Semana, Semana Anterior, Hoy, Este Mes, Histórico), inspector activo y comuna.
   * Omite la visualización de usuarios inactivos en todos los selectores de filtro del sistema.


