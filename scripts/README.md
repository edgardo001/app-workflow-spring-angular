# scripts/

Este directorio contiene scripts de utilidad para pruebas y automatización.

## Prueba de Integración de Extremo a Extremo (`e2e_test.py`)

El script `e2e_test.py` es una prueba de integración de extremo a extremo (E2E) basada en Python, diseñada para validar el flujo secuencial de aprobación de documentos sin necesidad de abrir un navegador web. Ejecuta solicitudes HTTP contra la instancia local de la aplicación para probar el ciclo de vida completo de un flujo de trabajo.

### Prerrequisitos

Necesitas tener instalado Python 3. Instala las librerías necesarias con pip:

```bash
pip install requests pyjwt
```

El script detecta y carga automáticamente las variables de entorno del archivo `.env` en la raíz del proyecto para obtener las credenciales de la base de datos, URLs y la clave `JWT_SECRET` para firmar los tokens.

### Ejecución de la Prueba

1. Asegúrate de que el entorno de desarrollo esté corriendo (MongoDB, Kafka y el backend de Spring Boot en el puerto 8080).
2. Ejecuta el script desde la raíz del proyecto:

```bash
python scripts/e2e_test.py
```

### Qué Valida

La prueba se divide en 5 fases secuenciales:

1. **Fase 1: Carga de Documentos**
   - Sube un archivo PDF de prueba a `/api/documents/upload`.
   - Valida que el backend almacene el archivo en `temp-documents/` y devuelva los metadatos correctos con un ID generado y el hash del archivo.

2. **Fase 2: Creación de Flujo**
   - Envía una nueva solicitud de flujo secuencial utilizando el ID del documento cargado y dos correos de participantes simulados (`approver1@example.com` y `approver2@example.com`).
   - Verifica que el flujo comience en estado `ACTIVE` en el paso 0.

3. **Fase 3: Verificación JWS**
   - Genera un token de aprobación JWS sin estado para `approver1` usando el `JWT_SECRET`.
   - Envía una consulta al endpoint público `GET /api/flows/verify?token=...` para confirmar que los detalles del flujo se pueden cargar sin una sesión activa.

4. **Fase 4: Aprobación mediante Token JWS y Autenticación**
   - Intenta realizar la aprobación del paso con un usuario autenticado incorrecto (`owner@example.com`), confirmando que el backend rechaza la acción con un error 403 (mismatch).
   - Realiza la solicitud con las credenciales correctas (`approver1@example.com`), validando que el flujo cambia a `PENDING_APPROVAL` y avanza de paso.

5. **Fase 5: Aprobación desde Dashboard (Autenticado sin Token)**
   - Simula una sesión de usuario logueado para `approver2@example.com` firmando una cookie de autenticación de prueba.
   - Obtiene la lista de pendientes del usuario para confirmar que el flujo aparece bajo el endpoint `/api/flows/pending` y que el indicador `isMyTurn` está habilitado.
   - Envía una solicitud POST a `/api/flows/{flowId}/approve` *sin* ningún token, permitiendo que el backend valide las credenciales contra el contexto de seguridad del usuario autenticado de la sesión.
   - Valida que el estado final del flujo sea `COMPLETED` y que ambos participantes se marquen como `approved`.
