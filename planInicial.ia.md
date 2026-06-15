# Plan Inicial — app-workflow-net-angular

## Stack Tecnológico

| Capa | Tecnología | Versión |
|---|---|---|
| Backend | Spring Boot | v4.1 |
| JDK | OpenJDK | v17 |
| Frontend | Angular | v22 |
| Base de datos | MongoDB | v8.3 |
| Mensajería | Apache Kafka | v4.3.0 |
| CSS/UI | Tailwind CSS | — |
| Contenedores | Docker + Docker Compose | — |
| Proxy reverso | Traefik | — |
| Monitoreo | Grafana + Prometheus | — |
| Gestión de dependencias (Spring) | Gradle | — |

---

## Funcionalidad Principal — Flujo de Documentos

- La idea de la aplicación es crear un flujo en el cual un usuario sube un documento a la plataforma y agrega correos secuenciales de destinatarios. Cada destinatario deberá aceptar por correo el documento, pasando de estado en estado.

- El documento solo será guardado temporalmente mientras se ejecuta el flujo completo, para poderlo enviar como adjunto en cada correo.

- Una vez que el último usuario lo acepte, se almacenará el hash y la metadata del documento y se eliminará del almacenamiento temporal.

- Al subir un documento, se les enviará un correo a todos los destinatarios avisando del inicio del flujo. Luego se le enviará un correo al primer usuario para que acepte el documento. El correo contendrá un token único de aceptación. Luego se le enviará al segundo usuario, luego al tercero, y así sucesivamente hasta terminar el flujo. Cuando termine, se enviará un último correo con el archivo adjunto a todos los destinatarios, indicando que se cerró el flujo.

- Cuando un documento es rechazado, se debe marcar y eliminar el documento del almacén. Solo quedará la metadata en la base de datos. Se enviará un correo a todos los participantes indicando el motivo del rechazo.

- Si un documento excede la fecha límite +3 días (configurables), el sistema debe rechazarlo automáticamente y avisar a los participantes que el documento expiró.

- Los documentos no pueden pesar más de 2 MB y no pueden cargarse más de 5 documentos por flujo.

- Los documentos se almacenarán en carpetas temporales.

- Los documentos y estados tendrán UUID únicos para diferenciarlos.

- El administrador podrá rechazar o relanzar un flujo existente, indicando un detalle de motivo.

---

## Autenticación y Seguridad

- Debe tener un sistema de login usando GitHub OAuth.

- **Tokens de Acción Estáticos (Stateless JWS):** El enlace del correo no debe llevar un ID numérico simple. Genera un JWS (JSON Web Signature) firmado por el backend que contenga en su payload el `flowId`, el `emailDestinatario` y el `stepNumber`.

- **Flujo de Validación Desacoplado:** Cuando el usuario hace clic, Angular lee el JWS y lo envía al backend. El backend valida la firma del token.

- **Validación de Sesión con GitHub:**
  1. Si el usuario ya inició sesión con GitHub, el backend verifica: ¿el email invitado coincide con el email de GitHub?
  2. Si no coinciden, la app debe mostrar una pantalla intermedia: "Estás logueado como `github_user`, pero este documento fue enviado a `correo_empresa`. ¿Deseas firmar de todas formas a nombre de esta identidad?" o simplemente registrar el ID de GitHub en el log de auditoría como el ejecutor real de la acción.

- No se debe publicar ninguna credencial de producción a Git. Para eso se usará el `.env` que no será cargado al repositorio, pero se deberá crear un `.env.example` con las explicaciones necesarias.

- Se debe habilitar la UI/UX de OpenAPI en producción para poder verificar funcionamientos y disponibilidad de endpoints.

- Las credenciales del administrador se indicarán en el mismo `.env` de la raíz del proyecto.

---

## Administrador / Creador del Flujo

- El administrador o creador del flujo tendrá una pantalla con dashboards indicativos KPI y una pantalla para subir documentos y/o crear un flujo.

- En el flujo se adjuntarán uno o más documentos. Se podrá agregar una breve descripción y una fecha límite indicada.

- El administrador o creador del flujo debe tener una grilla maestra con:
  - Búsqueda
  - Ordenamiento
  - Filtros
  - Paginación
  - Los estados de la grilla deben verse o almacenarse en la misma URL de consumo.

---

## Usuarios Destinatarios

- Los usuarios que aceptan los documentos deben hacer login para luego entrar en la plataforma y poder ver una grilla maestra con:
  - Búsqueda
  - Ordenamiento
  - Filtros
  - Paginación
  - Los estados de la grilla deben verse o almacenarse en la misma URL de consumo.
  - En esta grilla se podrá aceptar o rechazar los documentos.

---

## Arquitectura y Patrones de Diseño

### Arquitectura General

| Componente | Enfoque |
|---|---|
| Backend | Vertical Slice + Clean Architecture |
| Comunicación | Arquitectura Orientada a Eventos (Event-Driven) |
| Flujo transaccional | Patrón SAGA (orquestado) |
| Código | Código limpio y principios SOLID (solo cuando sea necesario, para evitar escribir demasiado código boilerplate) |

**Decisiones arquitectónicas registradas:**
- Deberás registrar las decisiones, pros y contras de cada decisión y el por qué es una buena opción usarlas en conjunto, qué problema se resuelve.

### SAGA Orquestado

- Se usará SAGA Orquestado: crear un servicio específico (`FlowOrchestratorService`). Es mucho más fácil mapear y depurar un flujo secuencial si un solo componente central sabe exactamente en qué paso va el documento y qué evento de Kafka debe disparar a continuación.

### Reparación de Flujos

- Registrar o crear un flujo de reparación cuando un proceso de eventos se corrompió y se necesita retomar en la arquitectura.

### Idempotencia (Consumidor Idempotente)

- En redes y sistemas distribuidos, Kafka puede entregar un mensaje más de una vez. Para evitar procesar dos veces la misma aprobación:
  - Cada evento de Kafka debe incluir un `eventId` único (UUID) o una clave de negocio única (ej: `approval-{flowId}-{step}`).
  - Antes de procesar el evento, el consumidor de Spring Boot intenta insertar esa clave en una colección de control en MongoDB con un índice Unique. Si la inserción falla por duplicado, el backend ignora el mensaje de forma segura (*Idempotent Consumer*).

---

## Trazabilidad y Auditoría (No Repudio)

- **Colección de Eventos (Append-Only):** Crear una colección en MongoDB llamada `flow_audit_log`. Esta colección nunca debe recibir un UPDATE ni un DELETE. Cada acción genera un nuevo registro.

- **Estructura del Log:** Cada documento de auditoría debe almacenar:

```json
{
  "flowId": "uuid-del-flujo",
  "action": "DOCUMENT_APPROVED",
  "userId": "github-oauth-id-12345",
  "userEmail": "usuario@email.com",
  "timestamp": "2026-06-15T12:00:00Z",
  "documentHash": "sha256-del-archivo...",
  "metadata": {
    "ip": "192.168.1.50",
    "userAgent": "Mozilla/5.0..."
  }
}
```

---

### Registro de Usuarios

- Los usuarios que hayan iniciado sesión deberán quedar registrados en la base de datos, indicando qué acción realizaron, qué aceptaron, su último login, etc. Esto para una trazabilidad completa de las acciones realizadas.

---

## Automatización de Fechas Límite y Limpieza

### Índices TTL de MongoDB

Para los tokens de correo o flujos abandonados, usar los índices TTL de MongoDB. Configurar una colección `tokens` donde los documentos se auto-eliminen pasadas 24 o 48 horas.

### Spring Boot Scheduler + Kafka

Para las fechas límite de los flujos, no saturar la base de datos con consultas constantes. Crear un Job diario (usando `@Scheduled` en Spring) que busque los flujos cuya fecha límite sea menor o igual a la fecha actual, y emita un evento `FlujoExpiradoEvent` hacia Kafka para que el patrón SAGA se encargue de cancelar y notificar.

---

## Envío de Correos — Resiliencia

### Patrón de Reintentos no Bloqueantes en Kafka

Si el servidor de correos se cae, no bloquear el hilo principal de procesamiento. Configurar contenedores de reintento en Spring Kafka (`@RetryableTopic`). Si el envío falla, Kafka reintenta de forma automática con un retraso exponencial (ej: a los 5 min, luego a los 15 min).

### Dead Letter Queue (DLQ)

Si tras 3 o 5 intentos el correo sigue fallando, el mensaje se envía a un tópico de Kafka llamado `email-sending-dlq`. Esto evitará que el flujo se quede en el limbo y permitirá alertar al administrador a través del Dashboard KPI.

---

## UI/UX

- Deberá tener una UI/UX similar a una plataforma de correos en donde el usuario adjunte un archivo y sea enviado a cada usuario del flujo uno por uno.

- Se usará Tailwind CSS para el UX/UI.

---

## Monitoreo y Observabilidad

- Se debe contar con servicios de monitoreo como Grafana y Prometheus.

- Se deberán crear endpoints de "is alive" y "healthcheck".

- En Grafana se podrán ver los KPI.

- Grafana debe desplegarse con usuario y contraseña indicada en el `.env`.

---

## Infraestructura y Despliegue

### Estructura de directorios

```
/src/
├── backend/
├── frontend/
└── docker/         (para los Docker Compose)
```

### Docker Compose

- Docker Compose deberá usar un `.env` alojado en la raíz del proyecto.
- La URL base de los servicios debe poder configurarse vía un archivo `.env` centralizado.
- Las contraseñas de MongoDB se deben indicar en el `.env` de la raíz del proyecto.
- Se usará Docker para desplegar los servicios en un VPS.

### Traefik

- Usaremos Traefik para disponibilizar los servicios a internet.
- Usaremos el modelo de despliegue y configuración de Traefik del proyecto [app-base-net-react](https://github.com/edgardo001/app-base-net-react), en el cual se usarán DNS del dominio de Cloudflare.

### GitHub Actions + Docker Hub

- Usaremos GitHub Actions para compilar los proyectos y desplegar las imágenes en Docker Hub.
- Luego usar dichas imágenes en el Docker Compose final que será usado para desplegar en los servidores VPS Linux Ubuntu.
- Deberás dar indicaciones de cómo configurar GitHub para desplegar imágenes en Docker Hub.

### Desarrollo local

- Debe tener un script `start-dev.bat` para iniciar los servicios en local para desarrollo.

### Referencia

- Si no sabes cómo resolver algún problema de configuración, usar el proyecto [app-base-net-react](https://github.com/edgardo001/app-base-net-react) como guía de apoyo.

---

## Desarrollo y Flujo de Trabajo

### Ramas y Pull Requests

- Se deberá trabajar en ramas de desarrollo de Git y/o worktrees, nunca en la rama main.
- Para eso se usarán diversos Pull Requests al repositorio GitHub.

### Prácticas de Desarrollo

- **TDD:** Cada funcionalidad debe tener sus test unitarios, enfocándonos en prácticas como TDD.
- En cada funcionalidad o modificación de la misma, se deberá respetar primeramente los test unitarios. Si los test unitarios pasan, se procede a realizar el cambio o la nueva funcionalidad, no antes.

### Agentes de Desarrollo

- Se podrán usar múltiples agentes enfocados a cada funcionalidad: Arquitecto, desarrollador, QA, Seguridad, Product Owner, Diseñador UX/UI.
- Los AGENTES deberán debatir siempre las soluciones antes de aceptarlas.
- Cada funcionalidad debe ser planificada mediante speckit de GitHub, entre todos los AGENTES disponibles.
- Se deberá registrar qué modelos LLM fueron usados para cada proceso de desarrollo.

### SpecKit (SDD)

- Para el desarrollo y la planificación de spec, usaremos speckit de GitHub para prácticas SDD (Spec-Driven Development).
- Deberás hacer el init y dejar registro de cómo se realizó la operación, para poder replicarlos en otros proyectos.

### Skills

- Usaremos la skill previamente instalada `npx skills add https://github.com/vercel-labs/skills --skill find-skills` para buscar nuevas habilidades necesarias.

### Archivos de proyecto

- Debes generar el `README.md` y `AGENTS.md`.
- Podrás usar diagramas explicativos de la arquitectura usando Mermaid.

---

## Resumen de Requisitos Técnicos Clave

| ID | Requisito |
|---|---|
| R01 | Flujo secuencial de aprobación por correo con tokens JWS |
| R02 | Almacenamiento temporal de documentos; solo hash + metadata al finalizar |
| R03 | Logging append-only en `flow_audit_log` con estructura definida |
| R04 | Login con GitHub OAuth |
| R05 | Grilla maestra con búsqueda, ordenamiento, filtros, paginación y estado en URL |
| R06 | Dashboard KPI para administrador |
| R07 | Patrón SAGA orquestado con Kafka |
| R08 | Consumidor idempotente con UUID + índice único en MongoDB |
| R09 | Límite: 2 MB por documento, 5 documentos por flujo |
| R10 | Fecha límite configurable + 3 días de gracia; expiración automática |
| R11 | Reintentos con backoff exponencial y DLQ para envío de correos |
| R12 | Índices TTL para limpieza de tokens y flujos abandonados |
| R13 | Vertical Slice + Clean Architecture + Event-Driven + SOLID (solo cuando sea necesario) |
| R14 | Traefik como proxy reverso con DNS de Cloudflare |
| R15 | Monitoreo con Grafana + Prometheus (endpoints healthcheck) |
| R16 | GitHub Actions + Docker Hub + VPS Linux Ubuntu |
| R17 | `.env` centralizado en la raíz; `.env.example` sin credenciales |
| R18 | OpenAPI habilitado en producción |
| R19 | Reparación de flujos corruptos |
| R20 | TDD obligatorio antes de cualquier cambio |
