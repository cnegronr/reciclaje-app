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

#### 5.- Desplegar cambios parciales en AWS via ssh
```bash
# 1. Conectarse por SSH a la instancia EC2
ssh -i "tu-llave.pem" ubuntu@<IP-PUBLICA-EC2>
# 2. Descargar los últimos cambios
cd /var/www/reciclaje-app
git pull origin main
# 3. Reconstruir y reiniciar contenedores
docker compose build --no-cache
docker compose up -d --remove-orphans
```
## 🌐 URLs de Acceso y Puertos

| Servicio | URL de Acceso | Descripción |
| :--- | :--- | :--- |
| **Aplicación Web (Docker/Nginx)** | `http://localhost` | Interfaz de usuario servida vía Nginx Proxy |
| **Aplicación Web (Vite Dev)** | `http://localhost:5173` | Servidor de desarrollo local |
| **API REST Backend (Spring Boot)** | `http://localhost:8080/api` | Endpoints REST de autenticación e inspecciones |
| **Base de Datos PostgreSQL** | `localhost:5432` | BD `reciclaje_db` (Usuario: `postgres` / Clave: `postgres`) |

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
   * Los usuarios inactivos pueden ser **eliminados definitivamente de la base de datos** (`hard-delete`), desvinculando de forma segura las claves foráneas en historiales sin alterar o perder los registros de inspección, kilos recolectados o fotografías históricas.

6. **Dashboard de Métricas Simplificado & Filtros:**
   * Interfaz simplificada y amigable con filtros por periodo (Esta Semana, Semana Anterior, Hoy, Este Mes, Histórico), inspector activo y comuna.
   * Omite la visualización de usuarios inactivos en todos los selectores de filtro del sistema.

