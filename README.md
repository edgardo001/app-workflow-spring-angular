# Workflow Net Angular

Sistema avanzado de flujo de aprobación secuencial de documentos con Arquitectura Orientada a Eventos y SAGA. 

Basado en Spring Boot v4.1, Angular v22, MongoDB v8.3 y Apache Kafka v4.3.0.

---

## 🚀 Arquitectura y Tecnologías

| Capa | Tecnología |
|---|---|
| **Backend** | Spring Boot v4.1, JDK 17 (Vertical Slice + Clean Architecture) |
| **Frontend** | Angular v22, Tailwind CSS |
| **Base de Datos** | MongoDB v8.3 (TTL Indexes, Unique Identifiers) |
| **Mensajería** | Apache Kafka v4.3.0 (Event-Driven, Patrón SAGA Orquestado) |
| **Despliegue** | Docker, Docker Compose, Traefik, GitHub Actions |
| **Monitoreo** | Grafana, Prometheus (Endpoints Healthcheck / KPI) |

### Patrones de Diseño Implementados
- **Event-Driven Architecture:** Todo el flujo del documento se maneja a través de tópicos en Kafka.
- **SAGA Orquestado (`FlowOrchestratorService`):** Un componente central mapea el progreso secuencial y emite los eventos hacia Kafka para el siguiente aprobador.
- **Consumidor Idempotente:** Controlado mediante IDs de evento (UUID) en MongoDB con índices únicos, evitando así el procesamiento de aprobaciones duplicadas si Kafka reintenta mensajes.
- **Trazabilidad (Append-Only):** Log inmutable de auditoría (`flow_audit_log`) que almacena metadatos y evidencia (hash SHA-256) sin permitir `UPDATE` ni `DELETE`.

---

## 🔐 Seguridad y Autenticación (BFF)

La plataforma utiliza el protocolo **OAuth2 con GitHub** empleando el patrón de seguridad más estricto para aplicaciones de tipo SPA (Single Page Applications): **BFF (Backend-For-Frontend)**.

- **Flujo BFF:** Al autorizar la aplicación en GitHub, la redirección es capturada exclusivamente por el Backend de Spring Boot. El Backend canjea el código secreto por un JWT Token.
- **HttpOnly Cookies:** El Backend envía al navegador el token sellado en una Cookie `HttpOnly` y `Secure`, evitando que el frontend (JavaScript) acceda al token. Esto mitiga vulnerabilidades como Cross-Site Scripting (XSS).
- **Tokens Stateless (JWS) en Correos:** Los enlaces de aprobación por correo envían un token de un solo uso en formato JWS que permite identificar flujo, etapa y correo sin necesidad de mantener sesiones abiertas innecesarias.

---

## 🔄 Funcionalidad Principal

1. **Gestión Documental:** 
   - Límite de carga: 5 documentos por flujo, 2MB máximo por documento.
   - El documento real solo se guarda de forma temporal (en `./temp-documents`) para adjuntarlo por correo.
   - Una vez finalizado el ciclo, se elimina el archivo físico y solo se preserva el Hash y metadata.
2. **Aprobación Secuencial:** 
   - El flujo viaja de participante en participante de manera ordenada.
   - Se generan notificaciones automatizadas vía Email al iniciar, avanzar, aprobar o rechazar el flujo completo.
3. **Control Automático (Expiración):** 
   - Un Scheduler diario de Spring (`@Scheduled`) revisa límites de gracia (+3 días) y emite un evento a Kafka (`FlujoExpiradoEvent`) cerrando y notificando flujos abandonados.
   - Apoyo de **Índices TTL en MongoDB** para limpieza autónoma de tokens temporales e IDs expirados.
4. **Resiliencia de Envío de Correos:** 
   - Sistema de reintentos asíncronos (`@RetryableTopic`) con *Backoff Exponencial*.
   - **DLQ (Dead Letter Queue):** Los correos que fallen reiteradamente irán a un tópico especial de revisión (`email-sending-dlq`) para evitar bloquear los procesos principales.
5. **Dashboard Maestro:**
   - Panel KPI y métricas para Administradores (vía integración Grafana y consultas MongoDB).
   - Tablas interactivas con paginación, filtros y ordenamientos sincronizados con la URL (`queryParams`).

---

## 🛠️ Entorno de Desarrollo Local

### Pre-requisitos
- Docker & Docker Compose
- JDK 17 o superior
- Node.js v22+
- GitHub OAuth App (URL de Callback: `http://localhost:4200/api/auth/github/callback`)

### Instalación Rápida

1. Renombra o copia `.env.example` a `.env` en la raíz del proyecto.
2. Llena las credenciales de Github, MongoDB y Mail SMTP. **No subas este archivo a Git**.
3. Ejecuta el script de inicio global:
   ```cmd
   .\start-dev.bat
   ```

Este script automatiza:
- Levanta contenedores Docker (Kafka y MongoDB).
- Inyecta variables locales del `.env` en memoria.
- Ejecuta el servidor Backend (Gradle, Puerto 8080).
- Ejecuta el servidor Frontend Vite (npm, Puerto 4200 con Proxy Inverso hacia el 8080).

---

## 📋 Flujo de Trabajo y SDD (Spec-Driven Development)

Este proyecto está regido estrictamente por SpecKit y TDD:
- Nunca trabajar directo en rama `main`. Usa ramas aisladas (worktrees) y crea **Pull Requests**.
- Todos los cambios deben estar respaldados por **Test Unitarios (TDD)** ejecutados y pasando antes de programar funcionalidad.
- Debate arquitectónico entre Agentes AI antes de escribir código permanente.
- Registro estricto de las decisiones tomadas en la bitácora de arquitectura.

---

## 📜 Licencia

Este proyecto está bajo la Licencia MIT - mira el archivo [LICENSE](LICENSE) para más detalles.
