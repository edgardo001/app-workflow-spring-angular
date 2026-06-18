# WorkflowNet

Sistema de aprobación secuencial de documentos con **Arquitectura Orientada a Eventos** y **SAGA Orquestado**.

Construido con Spring Boot, Angular, MongoDB y Apache Kafka.

---

## Arquitectura General

```mermaid
graph TB
    subgraph Frontend["Frontend - Angular"]
        UI[SPA UI]
        PROXY[Proxy Inverso :4200 → :8080]
    end

    subgraph Backend["Backend - Spring Boot"]
        REST[REST API]
        ORCH[FlowOrchestratorService]
        EMAIL[EmailSenderService]
        AUDIT[AuditService]
        SCHEDULER[Scheduler Expiración]
    end

    subgraph Infra["Infraestructura"]
        KAFKA[Apache Kafka]
        MONGO[(MongoDB)]
        SMTP[Servidor SMTP]
        GRAFANA[Grafana + Prometheus]
    end

    UI --> PROXY --> REST
    REST --> ORCH
    ORCH --> KAFKA
    KAFKA --> EMAIL
    EMAIL --> SMTP
    ORCH --> MONGO
    AUDIT --> MONGO
    SCHEDULER --> KAFKA
    MONGO --> GRAFANA
```

---

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| **Backend** | Spring Boot v4.1, JDK 17 |
| **Frontend** | Angular v22, Tailwind CSS |
| **Base de Datos** | MongoDB v8.3 |
| **Mensajería** | Apache Kafka v4.3.0 |
| **Despliegue** | Docker, Docker Compose, Traefik |
| **Monitoreo** | Grafana, Prometheus |

### Compatibilidad de Node.js

Angular 22 requiere una de las siguientes versiones de Node.js:

| Node.js | Estado | Compatibilidad |
|---------|--------|----------------|
| **22.22.3+** | LTS | ✅ Soportado |
| **24.15.0+** | LTS | ✅ Soportado |
| **26.0.0+** | LTS | ✅ Soportado |
| 25.x | No LTS | ❌ No soportado |
| 21.x o inferior | EOL | ❌ No soportado |

> ⚠️ **Importante:** Node.js 25.x es una versión impar (no LTS) y **no es compatible** con Angular 22. Usa siempre versiones pares (22, 24, 26).

```bash
# Verificar versión actual
node --version

# Cambiar versión (si usas nvm-windows)
nvm use 22  # o 24, o 26

# Cambiar versión (si usas fnm)
fnm use 22  # o 24, o 26
```

### Gestores de Versiones de Node.js

| Gestor | Plataforma | Instalación |
|--------|------------|-------------|
| **nvm-windows** | Windows | https://github.com/coreybutler/nvm-windows/releases |
| **fnm** | Windows/Mac/Linux | `winget install Schniz.fnm` o https://github.com/Schniz/fnm |
| **nvm** | Mac/Linux | https://github.com/nvm-sh/nvm |

```bash
# nvm-windows: instalar desde .exe en GitHub Releases
# Después de instalar:
nvm install 26
nvm use 26

# fnm: más rápido que nvm
fnm install 26
fnm use 26
```

---

## Arquitectura Vertical Slices

Cada feature es una Slice independiente con su propio dominio, infraestructura y presentación:

```mermaid
graph LR
    subgraph flow["flow"]
        FD[domain/model]
        FS[application/service]
        FI[infrastructure/messaging]
        FP[presentation]
    end

    subgraph auth["auth"]
        AS[AuthService]
        GH[GitHubOAuth]
        JWT[JwtTokenService]
        FILTER[JwtAuthFilter]
    end

    subgraph document["document"]
        DS[DocumentService]
        DC[DocumentController]
        DD[domain]
    end

    subgraph audit["audit"]
        AUS[AuditService]
        AUL[FlowAuditLog]
        AUR[AuditLogRepository]
    end

    subgraph shared["shared"]
        EVT[Eventos Comunes]
        CONFIG[Configuración]
    end

    flow --> shared
    auth --> shared
    document --> shared
    audit --> shared
```

---

## Flujo de Aprobación Secuencial (SAGA)

```mermaid
sequenceDiagram
    participant U as Usuario
    participant BE as Backend
    participant K as Kafka
    participant E as Email Service
    participant M as MongoDB
    participant R as Receptor

    U->>BE: Crear flujo con documentos
    BE->>M: Guardar Flow (DRAFT)
    BE->>K: FlowCreatedEvent
    BE->>M: Registrar en AuditLog

    U->>BE: Iniciar flujo
    BE->>M: Flow → ACTIVE
    BE->>K: FlowStartedEvent
    BE->>E: EmailSendEvent
    E->>R: Email con enlace JWS

    R->>BE: Aprobar (token JWS)
    BE->>M: Verificar idempotencia
    BE->>M: Avanzar currentStep
    BE->>K: DocumentApprovedEvent
    BE->>E: Siguiente aprobador

    alt Último aprobador
        BE->>M: Flow → COMPLETED
        BE->>K: FlowCompletedEvent
        BE->>E: Email de finalización
    end
```

---

## Event-Driven Architecture

```mermaid
graph TB
    subgraph Producer["Productores"]
        FS[FlowService]
        FOS[FlowOrchestratorService]
        SCHED[Scheduler Expiración]
    end

    subgraph KafkaTopics["Kafka Topics"]
        T1[flow.created]
        T2[flow.started]
        T3[flow.document.approved]
        T4[flow.document.rejected]
        T5[flow.completed]
        T6[flow.expired]
        T7[email.send]
        T8[email.failed]
    end

    subgraph Consumers["Consumidores"]
        FEC[FlowEventConsumer]
        ESC[EmailSenderService]
        DLQ[DLQ Handler]
    end

    FS --> T1
    FOS --> T2
    FOS --> T3
    FOS --> T4
    FOS --> T5
    SCHED --> T6

    T1 --> FEC
    T3 --> FEC
    T4 --> FEC
    T6 --> FEC
    T7 --> ESC
    T8 --> DLQ
```

---

## Autenticación BFF (Backend-For-Frontend)

```mermaid
sequenceDiagram
    participant U as Usuario
    participant FE as Frontend
    participant BE as Backend
    participant GH as GitHub OAuth

    U->>FE: Click "Login con GitHub"
    FE->>GH: Redirigir a GitHub
    GH->>U: Autorizar aplicación
    GH->>BE: Callback con código (HTTP Only)

    BE->>GH: Canjear código por token
    GH-->>BE: access_token

    BE->>BE: Crear JWT interno
    BE->>U: Set-Cookie: HttpOnly + Secure

    U->>FE: Request autenticado
    FE->>BE: Cookie automática
    BE->>BE: Validar JWT de cookie
    BE-->>FE: 200 OK + datos
```

---

## Modelo de Dominio (Flow)

```mermaid
stateDiagram-v2
    [*] --> DRAFT
    DRAFT --> ACTIVE : start()
    ACTIVE --> PENDING_APPROVAL : approve() [no último]
    ACTIVE --> COMPLETED : approve() [último]
    ACTIVE --> REJECTED : reject()
    PENDING_APPROVAL --> ACTIVE : approve() [no último]
    PENDING_APPROVAL --> COMPLETED : approve() [último]
    PENDING_APPROVAL --> REJECTED : reject()
    ACTIVE --> EXPIRED : expire()
    PENDING_APPROVAL --> EXPIRED : expire()
    DRAFT --> CANCELLED : cancel()
    ACTIVE --> CANCELLED : cancel()
    PENDING_APPROVAL --> CANCELLED : cancel()
    COMPLETED --> [*]
    REJECTED --> [*]
    EXPIRED --> [*]
    CANCELLED --> [*]
```

---

## Funcionalidad Principal

### Gestión Documental
- **Límite:** 5 documentos por flujo, 2MB máximo por documento
- Los documentos se guardan temporalmente en `./temp-documents` para adjuntar por correo
- Al finalizar, se elimina el archivo físico y solo se preserva el Hash SHA-256 + metadata

### Aprobación Secuencial
- El flujo viaja de participante en participante de manera ordenada
- Notificaciones automatizadas vía email al iniciar, avanzar, aprobar o rechazar

### Control de Expiración
- Scheduler diario (`@Scheduled`) revisa límites de gracia (+3 días)
- Emite `FlowExpiredEvent` a Kafka para cerrar flujos abandonados
- Índices TTL en MongoDB para limpieza autónoma de tokens expirados

### Resiliencia de Email
- Reintentos asíncronos (`@RetryableTopic`) con backoff exponencial
- **DLQ (Dead Letter Queue):** Correos fallidos van a `email-sending-dlq` para revisión

### Dashboard Maestro
- Panel KPI y métricas para administradores
- Tablas interactivas con paginación, filtros y ordenamientos sincronizados con la URL

---

## Seguridad

| Mecanismo | Descripción |
|---|---|
| **BFF Pattern** | Backend canjea código OAuth por JWT, frontend nunca maneja tokens |
| **HttpOnly Cookies** | Token en cookie `HttpOnly` + `Secure`, mitiga XSS |
| **JWS Tokens** | Tokens stateless en enlaces de correo para aprobación |
| **Idempotencia** | UUIDs únicos en MongoDB previenen procesamiento duplicado |
| **Audit Log** | Log inmutable (`flow_audit_log`) con hash SHA-256 |

---

## Desarrollo Local

### Requisitos

- Docker & Docker Compose
- JDK 17+
- **Node.js 22.x, 24.x o 26.x** (versiones LTS) — ver [compatibilidad](#compatibilidad-de-nodejs)
- GitHub OAuth App (Callback: `http://localhost:8080/api/auth/github/callback`)

### Inicio rápido

```bash
# 1. Clonar y configurar variables de entorno
cp .env.example .env
# Editar .env con valores reales

# 2. Iniciar todo (MongoDB, Kafka + Backend + Frontend)
.\start-dev.bat
```

El script levanta:
- **Docker:** MongoDB (:27017) + Kafka (:9092)
- **Backend:** Gradle en puerto 8080
- **Frontend:** Vite en puerto 4200 (proxy inverso → 8080)

### Detener todo

```bash
# Detener todos los procesos y contenedores
.\kill-dev.bat
```

El script detiene:
- Procesos Java (Backend / Gradle)
- Procesos Node.js (Frontend / Angular)
- Procesos en puertos 8080 y 4200 (conexiones huérfanas)
- Contenedores Docker (MongoDB + Kafka)

### Matar procesos por puerto (Windows)

Si necesitas liberar un puerto específico manualmente:

```bash
# Ver qué procesos están usando un puerto
netstat -ano | findstr :8080

# Matar el proceso por PID
taskkill /F /PID <PID>

# O buscar y matar todos los java
taskkill /F /IM java.exe
```

### Comandos Individuales

```bash
# Docker (MongoDB + Kafka)
docker compose -f src/docker/docker-compose.yml --env-file .env up -d mongodb kafka

# Backend
cd src/backend && ./gradlew bootRun

# Frontend (con proxy inverso hacia backend)
cd src/frontend && ng serve --proxy-config proxy.conf.json
```

### Proxy Inverso (Desarrollo)

El archivo `src/frontend/proxy.conf.json` redirige las peticiones `/api` del frontend al backend:

```json
{
  "/api": {
    "target": "http://127.0.0.1:8080",
    "secure": false
  }
}
```

| Puerto | Servicio | Descripción |
|--------|----------|-------------|
| 4200 | Frontend | Angular DevServer |
| 8080 | Backend | Spring Boot API |

Las llamadas a `http://localhost:4200/api/*` se proxean automáticamente a `http://localhost:8080/api/*`.

**Swagger UI (desarrollo):** `http://localhost:8080/swagger-ui.html`

**Swagger UI (producción):** `https://api.tu-dominio.com/swagger-ui.html`

### Variables de entorno (Desarrollo)

| Variable | Descripción | Requerida |
|----------|-------------|-----------|
| `MONGO_ROOT_USER` | Usuario root de MongoDB | Sí |
| `MONGO_ROOT_PASSWORD` | Contraseña root de MongoDB | Sí |
| `GITHUB_CLIENT_ID` | Client ID de GitHub OAuth | Sí |
| `GITHUB_CLIENT_SECRET` | Client Secret de GitHub OAuth | Sí |
| `GITHUB_REDIRECT_URI` | URI de callback (desarrollo: `http://localhost:8080/api/auth/github/callback`) | Sí |
| `JWT_SECRET` | Clave JWT (mínimo 64 caracteres) | Sí |
| `MAIL_HOST` | Servidor SMTP | Sí |
| `MAIL_USERNAME` | Usuario SMTP | Sí |
| `MAIL_PASSWORD` | Contraseña SMTP | Sí |

### Diagnóstico y Resolución de Problemas (Troubleshooting)

#### Error: `Command failed with error 13 (Unauthorized): 'Command find requires authentication'`
* **Causa:** Existencia de una clase `MongoConfig.java` que extiende de `AbstractMongoClientConfiguration` en `com.workflowspring.config` que anula la autoconfiguración nativa de Spring Boot e intenta conectarse sin credenciales a `localhost:27017`.
* **Solución:** Eliminar el archivo `MongoConfig.java`. Esto forzará a Spring Boot a usar su autoconfiguración nativa leyendo la propiedad `spring.data.mongodb.uri` de `application.yml`.

#### Error: `java.lang.IllegalArgumentException: state should be: databaseName does not contain ' '`
* **Causa:** Espacios en blanco al final de las variables de entorno en el script por lotes de Windows (`cmd.exe`), causados por espaciado incorrecto antes de los encadenadores `&&`. Ejemplo: `set VAR=VAL && set VAR2=VAL2` inyecta un espacio al final de `VAR`.
* **Solución:** Escapar las comillas de asignación y eliminar los espacios previos a `&&` en los scripts `.bat`:
  ```bat
  set "MONGO_DATABASE=%MONGO_DATABASE%"&& set "VAR=%VAR%"
  ```

#### Error: Cambios en credenciales del `.env` no se aplican en MongoDB
* **Causa:** Las credenciales root de MongoDB se configuran únicamente durante la creación inicial del volumen de datos del contenedor Docker.
* **Solución:** Recrear el contenedor limpiando los volúmenes de desarrollo antiguos:
  ```bash
  # Detener contenedores y limpiar volúmenes del compose
  docker compose -f src/docker/docker-compose.yml --env-file .env down -v
  
  # Limpieza general de volúmenes huérfanos locales si es necesario
  docker volume prune -f
  ```

---

## Flujo de Trabajo

Este proyecto utiliza **SpecKit SDD** (Spec-Driven Development) y **TDD**:

- Nunca trabajar directo en `main` — usar ramas aisladas y Pull Requests
- Todos los cambios deben tener tests unitarios pasando
- Debate arquitectónico antes de código permanente
- Decisiones registradas en la bitácora de arquitectura

### Proceso para Crear un Pull Request (PR)

Para contribuir de forma manual y seguir la metodología del proyecto, sigue los siguientes pasos:

#### 1. Crear una Rama de Desarrollo
Nunca trabajes en la rama `main`. Crea una rama con nombre descriptivo y prefijo de tipo de cambio (ej. `fix/`, `feat/`, `chore/`, `docs/`):
```bash
git checkout -b <tipo-de-rama>/<nombre-descriptivo>
# Ejemplo: git checkout -b fix/mongo-auth-and-startup
```

#### 2. Confirmar los Cambios en Commits Atómicos
Agrupa tus cambios de forma lógica y realiza commits pequeños con mensajes claros:
```bash
# Registrar archivos para el commit
git add <archivo-modificado>

# Crear el commit
git commit -m "<tipo-de-commit>(<alcance>): <descripcion corta en minuscula>"
# Ejemplo: git commit -m "fix(backend): enable native mongo autoconfiguration with credentials"
```

#### 3. Subir la Rama al Repositorio Remoto
Envía tu rama local a GitHub:
```bash
git push origin <nombre-de-la-rama>
# Ejemplo: git push origin fix/mongo-auth-and-startup
```

#### 4. Crear el Pull Request usando GitHub CLI (`gh`)
Si usas `gh cli`, puedes gestionar todo el flujo desde la terminal:

* **Iniciar sesión (si no estás autenticado):**
  ```bash
  gh auth login
  ```
* **Crear el Pull Request de forma no interactiva (pasando título y cuerpo):**
  ```bash
  gh pr create --title "<titulo-del-pr>" --body "<descripcion-del-cambio>"
  ```
* **Crear el Pull Request de forma interactiva (asistida en consola):**
  ```bash
  gh pr create
  ```
* **Revisar Pull Requests abiertos en el repositorio:**
  ```bash
  gh pr list
  ```
* **Ver el estado de tus Pull Requests actuales:**
  ```bash
  gh pr status
  ```

#### 5. Integrar (Merge) el Pull Request
Una vez revisado y validado el PR, se debe integrar a la rama principal `main`. Puedes hacerlo de dos formas:

##### A. Desde la Terminal con GitHub CLI (`gh`)
* **Hacer merge interactivo (te preguntará el tipo de merge y si deseas borrar la rama local y remota):**
  ```bash
  gh pr merge
  ```
* **Hacer merge directo (estilo Squash, que consolida todos los commits y limpia la rama):**
  ```bash
  gh pr merge --squash --delete-branch
  ```
* **Hacer merge tradicional (crea un commit de fusión):**
  ```bash
  gh pr merge --merge --delete-branch
  ```

##### B. Desde la Interfaz Web de GitHub
1. Ve a la pestaña **Pull Requests** en tu repositorio en GitHub.
2. Selecciona tu PR abierto.
3. Al final de la página de discusión, haz clic en **Merge pull request** (o elige *Squash and merge* en la flecha desplegable).
4. Haz clic en **Confirm merge**.
5. Presiona **Delete branch** para eliminar la rama de desarrollo ya integrada y mantener el repositorio limpio.

---

## Despliegue en Producción

El archivo `src/docker/docker-compose.yml` levanta:
1. **Traefik** — Proxy reverso con TLS automático (Let's Encrypt + Cloudflare)
2. **MongoDB 8** — Base de datos
3. **Kafka + Zookeeper** — Mensajería event-driven
4. **Backend** — Spring Boot API
5. **Frontend** — Angular SPA (Nginx)
6. **Prometheus** — Métricas
7. **Grafana** — Dashboards KPI

### Comandos Principales

```bash
# Construir todas las imágenes
docker compose -f src/docker/docker-compose.yml --env-file .env build

# Desplegar la aplicación
docker compose -f src/docker/docker-compose.yml --env-file .env up -d

# Construir y desplegar en un solo paso
docker compose -f src/docker/docker-compose.yml --env-file .env up -d --build

# Detener todos los servicios
docker compose -f src/docker/docker-compose.yml --env-file .env down

# Detener + borrar volúmenes y redes (BD incluida)
docker compose -f src/docker/docker-compose.yml --env-file .env down --volumes
```

### Verificar Servicios

```bash
# Ver estado de contenedores
docker compose -f src/docker/docker-compose.yml ps

# Ver logs en tiempo real
docker compose -f src/docker/docker-compose.yml logs -f

# Ver logs de un servicio específico
docker compose -f src/docker/docker-compose.yml logs -f backend
docker compose -f src/docker/docker-compose.yml logs -f traefik
```

### Servicios desplegados

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| Traefik | 80, 443 | Reverse proxy con TLS automático (Let's Encrypt) |
| Backend | 8080 | Spring Boot API |
| Frontend | 80 | Angular SPA (Nginx) |
| MongoDB | 27017 | Base de datos |
| Kafka | 9092 | Mensajería event-driven |
| Prometheus | 9090 | Métricas |
| Grafana | 3000 | Dashboards KPI |

### Redes Docker

- `mvp-network` — Comunicación interna entre servicios
- `traefik-net` — Integración con Traefik para descubrimiento automático

### VPS con 1vCPU / 1GB RAM

Compilar imágenes Docker en un VPS de 1GB RAM puede agotar la memoria. Se recomienda agregar swap:

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
# Persistente al reinicio:
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

Para verificar: `swapon --show` o `free -h`.

---

## Configurar Cloudflare + Let's Encrypt (Traefik TLS)

### 1. Crear API Token en Cloudflare

1. Ir a [Cloudflare Dashboard → API Tokens](https://dash.cloudflare.com/profile/api-tokens)
2. Click **"Create Token"**
3. Seleccionar **"Edit zone DNS"** (template)
4. Configurar permisos:
   - **Permissions**: `Zone → DNS → Edit`
   - **Resources**: `Include → Specific zone → tu-dominio.com`
5. Click **"Continue to summary"** → **"Create Token"**
6. Copiar el token → asignar a `CF_DNS_API_TOKEN` en `.env`

### 2. Configurar Registros DNS en Cloudflare

Para cada dominio, crear registros DNS apuntando al servidor:

| Tipo | Nombre | Contenido | Proxy |
|------|--------|-----------|-------|
| A | `app` | `IP_DEL_SERVIDOR` | DNS only (gray cloud) |
| A | `api` | `IP_DEL_SERVIDOR` | DNS only (gray cloud) |

> **Importante**: Desactivar el proxy (gray cloud) para que Traefik pueda validar los certificados.

### 3. Generar Hash para Dashboard Traefik

```bash
# Con contenedor Docker (recomendado)
docker run --rm httpd:alpine htpasswd -nb admin tu-contrasena

# Resultado: admin:$apr1$xyz$...
# IMPORTANTE: Duplicar los '$' en el .env:
# TRAEFIK_PASS_HASH=admin:$$apr1$$xyz$$...
```

### 4. Variables en `.env`

```bash
# Cloudflare
CF_DNS_API_TOKEN=tu-token-api-cloudflare

# Traefik
ACME_EMAIL=admin@tu-dominio.com
TRAEFIK_PASS_HASH=admin:$$apr1$$xyz$$...
TRAEFIK_DOMAIN=api.tu-dominio.com

# Dominios
BACKEND_DOMAIN=api.tu-dominio.com
FRONTEND_DOMAIN=app.tu-dominio.com
```

### 5. Verificar Certificados

Una vez desplegado, Traefik automáticamente:
1. Detecta los dominios configurados en las labels Docker
2. Valida con Cloudflare DNS challenge
3. Emite certificados Let's Encrypt
4. Los renueva automáticamente antes de expirar

Para verificar:
```bash
# Ver logs de Traefik
docker logs workflow-traefik

# Verificar certificado
curl -v https://api.tu-dominio.com 2>&1 | grep -A5 "SSL certificate"
```

---

## Licencia

MIT — ver [LICENSE](LICENSE)
