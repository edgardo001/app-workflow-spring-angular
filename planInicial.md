app-workflow-net-angular — Aplicación desarrollada con Spring Boot v4.1 con OpenJDK v17 y Angular v22, MongoDB v8.3 y Apache Kafka v4.3.0

- La idea de esta aplicación será crear un flujo en el cual subo un documento a una plataforma y agrego correos secuenciales de destinatarios, cada destinatario deberá aceptar por correo el documento, pasando de estado en estado.
- El documento solo será guardado temporalmente mientras se ejecuta el flujo completo, para poderlo enviar como adjunto en cada correo.
- Una vez que el último usuario lo acepte, se almacenará el hash y metadata del documento y se eliminará del almacenamiento.
- Deberá tener una UI/UX similar a una plataforma de correos en donde adjuntaré un archivo y será enviado a cada usuario del flujo uno por uno.
- Al subir un documento, se les enviará un correo a todos los destinatarios avisando del flujo, luego se le enviará un correo al primer usuario para que acepte el documento, el correo tendrá un token único de aceptación, luego se le enviará al segundo usuario y al tercero hasta terminar el flujo, cuando termine, se enviará un último correo con el archivo adjunto a todos los destinatarios, indicando que se cerró el flujo.
- Debe tener un sistema de login usando GitHub OAuth.
- El administrador o creador del flujo tendrá una pantalla con dashboards indicativos KPI y una pantalla para subir documento y/o crear un flujo, el flujo se subirá adjuntará uno o más documentos en donde podrá agregar una breve descripción y una fecha límite indicada.
- Los documentos y estados tendrán UUID únicos para diferenciarlos.
- El administrador o creador del flujo debe tener una grilla maestra con búsqueda, ordenamiento, filtros, paginación y los estados de la grilla deben verse o almacenarse en la misma URL de consumo.
- Se usará Docker para desplegar los servicios en un VPS.
- La url base de los servicios debe poder configurarse vía un archivo .env centralizado.
- Debe tener un script start-dev.bat para iniciar los servicios en local para desarrollo.
- Los usuarios que aceptan los documentos deben hacer login para luego entrar en la plataforma y poder ver una grilla maestra con búsqueda, ordenamiento, filtros, paginación y los estados de la grilla deben verse o almacenarse en la misma URL de consumo. En esta grilla se podrá aceptar o rechazar los documentos.
- Cuando un documento es rechazado, se debe marcar y eliminar el documento del almacén, solo quedará la metadata en la base de datos. Se enviará un correo a todos los participantes, indicando el motivo del rechazo del documento.
- Se usará Tailwind CSS para el UX/UI.
- El sistema está pensado para que gestione todos los flujos mediante eventos con Kafka, se deberá usar el patrón SAGA.
- Los proyectos deberán quedar en /src/backend /src/frontend y en /src/docker (para los Docker Compose).
- Docker Compose deberá usar un .env alojado en la raíz del proyecto.
- Debes generar el README.md y AGENTS.md, podrás usar diagramas explicativos de la arquitectura usando Mermaid.
- Para el desarrollo y la planificación de spec, usaremos speckit de GitHub para prácticas SDD, deberás hacer el init y dejar registro de cómo se realizó la operación, para poder replicarlos en otros proyectos.
- Se podrán usar múltiples agentes enfocados a cada funcionalidad: Arquitecto, desarrollador, QA, Seguridad, Product Owner, Diseñador UX/UI.
- Cada funcionalidad debe tener sus test unitarios, enfocándonos en prácticas como TDD.
- Los documentos se almacenarán en carpetas temporales.
- Si un documento excede la fecha límite +3 días (configurables), el sistema debe rechazarlo y avisar a los participantes que el documento expiró.
- Los documentos no pueden pesar más de 2 MB y no pueden cargarse más de 5 documentos por flujo.
- En cada funcionalidad o modificación de la misma, se deberá respetar primeramente los test unitarios, si los test unitarios pasan, se procede a realizar el cambio o la nueva funcionalidad, no antes.
- Cada funcionalidad debe ser planificada mediante speckit de GitHub, entre todos los AGENTES disponibles.
- Se debe habilitar la UI/UX de OpenAPI en producción, para poder verificar funcionamientos y disponibilidad de endpoints.
- Se debe contar con servicios de monitoreo como Grafana y Prometheus, para ellos se deberán crear endpoints de "is alive" y "healthcheck", en Grafana se podrán ver los KPI.
- Grafana debe desplegarse con usuario y contraseña indicada en el .env.
- Usaremos Traefik para disponibilizar los servicios a internet.
- Usaremos el modelo de despliegue y configuración de Traefik de este proyecto https://github.com/edgardo001/app-base-net-react en el cual se usarán DNS del dominio de Cloudflare.
- Las contraseñas de MongoDB se deben indicar en el .env de la raíz del proyecto.
- Si no sabes cómo resolver algún problema de configuración, usaremos este proyecto como guía de apoyo https://github.com/edgardo001/app-base-net-react.
- No se debe publicar ninguna credencial de producción a Git, para eso usaremos el .env que no será cargado al repo, pero se deberá crear un .env.example con las explicaciones necesarias.
- Usaremos Gradle para gestionar las dependencias de Spring.
- El administrador podrá rechazar o relanzar un flujo existente, indicando un detalle de motivo.
- La arquitectura usada será Vertical Slice para el backend, apoyada de Clean Architecture, Arquitectura Orientada a Eventos, código limpio y principios SOLID. Deberás registrar las decisiones, pros y contras de cada decisión y el por qué es una buena opción usarlas en conjunto, qué problema se resuelve.
- Se usará SOLID solo cuando sea necesario, esto para evitar escribir demasiado código boilerplate.
- Registrar o crear un flujo de reparación cuando un proceso de eventos se corrompió y se necesita retomar en la arquitectura.
- Los AGENTES deberán debatir siempre las soluciones antes de aceptarlas.
- Se deberá registrar qué modelos LLM fueron usados para cada proceso de desarrollo.
- Se deberá trabajar en ramas de desarrollo de Git y/o worktrees, nunca en la rama main, para eso usarán diversos Pull Requests al repositorio GitHub.
- Usaremos GitHub Actions para compilar los proyectos y desplegar las imágenes en Docker Hub, para luego usar dichas imágenes en el Docker Compose final que será usado para desplegar en los servidores VPS Linux Ubuntu. Deberás dar indicaciones de cómo configurar GitHub para desplegar imágenes en Docker Hub.
- Usaremos la skill previamente instalada "npx skills add https://github.com/vercel-labs/skills --skill find-skills" para buscar nuevas habilidades necesarias.
- Los usuarios que hayan iniciado sesión deberán quedar registrados en la base de datos, indicando qué acción realizaron, qué aceptaron, su último login, etc. Esto para una trazabilidad de las acciones realizadas.
- Las credenciales del administrador se indicarán en el mismo .env de la raíz del proyecto.
- Usaremos la skill previamente instalada "npx skills add https://github.com/vercel-labs/skills --skill find-skills" para buscar nuevas habilidades necesarias.
- Los usuarios que hayan iniciado secion, deberan quedar registrados en la base de datos, indicando que accion realizaron, que aceptaron, su ultimo login, etc. Esto para una trazabilidad de las acciones realizadas.
- La credenciales del administrador se indicaran en el mismo .env de la raiz de proyecto
- Agrega turnstile de cloudflare al login, no puede hacer login si no acepta el recaptcha.
- Agrega un aviso en todas la paginas cuando el backend no esta disponible, un tipo de alerta, un la luz que indique el estado.
- En el login se agrega "Al continuar, aceptas los términos de uso y la política de privacidad.", implementa las paginas correspondientes.


1. Automatización de Fechas Límite y Limpieza
Índices TTL (Time-To-Live) de MongoDB: Para los tokens de correo o flujos abandonados, usa los índices TTL de Mongo. Puedes configurar una colección tokens donde los documentos se auto-eliminen pasadas 24 o 48 horas.

Spring Boot Scheduler + Kafka: Para las fechas límite de los flujos, no satures la base de datos con consultas constantes. Crea un Job diario (usando @Scheduled en Spring) que busque los flujos cuya fecha límite sea menor o igual a la fecha actual, y emita un evento FlujoExpiradoEvent hacia Kafka para que el patrón SAGA se encargue de cancelar y notificar.

2. Seguridad: El Puente de Identidad (Token + OAuth)
Tokens de Acción Estáticos (Stateless JWS): El enlace del correo no debe llevar un id numérico simple. Genera un JWS (JSON Web Signature) firmado por tu backend que contenga en su payload el flowId, el emailDestinatario y el stepNumber.

Flujo de Validación Desacoplado: Cuando el usuario hace clic, Angular lee el JWS y lo envía al backend. El backend valida la firma del token.

Validación de Sesión con GitHub: 1. Si el usuario ya inició sesión con GitHub, el backend verifica: ¿El email invitado coincide con el email de GitHub?
3. Si no coinciden, la app debe mostrar una pantalla intermedia: "Estás logueado como github_user, pero este documento fue enviado a correo_empresa. ¿Deseas firmar de todas formas a nombre de esta identidad?" o simplemente registrar el ID de GitHub en el log de auditoría como el ejecutor real de la acción.

4. Idempotencia y SAGA con Kafka
Implementa la estrategia de Consumidor Idempotente: En redes y sistemas distribuidos, Kafka puede entregar un mensaje más de una vez. Para evitar procesar dos veces la misma aprobación:

Cada evento de Kafka debe incluir un eventId único (UUID) o una clave de negocio única (ej: approval-{flowId}-{step}).

Antes de procesar el evento, el consumidor de Spring Boot intenta insertar esa clave en una colección de control en MongoDB con un índice Unique. Si la inserción falla por duplicado, el backend ignora el mensaje de forma segura (Idempotent Consumer).

Usa SAGA Orquestado: Crea un servicio específico (FlowOrchestratorService). Es mucho más fácil mapear y depurar un flujo secuencial si un solo componente central sabe exactamente en qué paso va el documento y qué evento de Kafka debe disparar a continuación.

5. Trazabilidad y No Repudio (Auditoría)
Colección de Eventos (Append-Only): Crea una colección en MongoDB llamada flow_audit_log. Esta colección nunca debe recibir un UPDATE ni un DELETE. Cada acción genera un nuevo registro.

Estructura del Log: Cada documento de auditoría debe almacenar:

JSON


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
6. Resiliencia en el Envío de Correos
Patrón de Reintentos no Bloqueantes en Kafka: Si el servidor de correos se cae, no bloquees el hilo principal de procesamiento. Configura contenedores de reintento en Spring Kafka (@RetryableTopic). Si el envío falla, Kafka reintenta de forma automática con un retraso exponencial (ej: a los 5 min, luego a los 15 min).

Dead Letter Queue (DLQ): Si tras 3 o 5 intentos el correo sigue fallando, el mensaje se envía a un tópico de Kafka llamado email-sending-dlq. Esto evitará que el flujo se quede en el limbo y te permitirá alertar al administrador a través del Dashboard KPI.
