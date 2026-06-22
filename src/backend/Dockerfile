# =============================================================================
# Backend Dockerfile — Spring Boot Multi-Stage Build
# =============================================================================
# Multi-stage build: separa el proceso de compilación del runtime final.
# Esto reduce el tamaño de la imagen de ~1GB (con JDK + Gradle) a ~150MB (solo JRE + JAR).
# =============================================================================

# --- Stage 1: Build (compilación) ---
# Usamos JDK 17 porque Spring Boot 4.x lo requiere como mínimo.
# Alpine porque es ligera (~5MB vs ~70MB de Debian), reduce tiempo de pull.
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copiamos Gradle wrapper y configuración de dependencias PRIMERO.
# Docker construye por capas: si estos archivos no cambian,
# la capa de descarga de dependencias se reutiliza (cache hit).
# Sin esto, cambiar UNA línea de código re-descargaria todas las dependencias (~60s).
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

# Descargamos dependencias ANTES de copiar el código fuente.
# Esto es clave para el cache de Docker Buildx en CI:
#   - Si solo cambia src/ → la capa de dependencias NO se reconstruye
#   - Si cambia build.gradle.kts → SÍ se reconstruye (y eso es correcto)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# Ahora copiamos el código fuente y compilamos el JAR.
# --no-daemon: evita que Gradle ejecute un daemon en background dentro del contenedor.
# En Docker no hay proceso padre que recoja el daemon, y causaría que el build no termine.
COPY src src
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

# --- Stage 2: Runtime (ejecución) ---
# JRE en vez de JDK: no necesitamos compilar en producción, solo ejecutar.
# Alpine: imagen mínima (~50MB) para reducir attack surface y tamaño de imagen.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Solo copiamos el JAR resultante del stage anterior.
# --from=build indica que viene del stage "build", no del host.
# El wildcard *.jar funciona porque solo hay un JAR en build/libs/.
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# ENTRYPOINT vs CMD:
#   - ENTRYPOINT: el ejecutable principal (no se puede sobreescribir con args)
#   - CMD: argumentos por defecto (se pueden sobreescribir)
# Usamos ENTRYPOINT porque java -jar es siempre el mismo comando.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
