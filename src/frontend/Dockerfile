# =============================================================================
# Frontend Dockerfile — Angular Multi-Stage Build
# =============================================================================
# Multi-stage build: compila Angular en Node, sirve con Nginx.
# Nginx es mucho más ligero y rápido que un servidor Node para servir archivos estáticos.
# =============================================================================

# --- Stage 1: Build (compilación Angular) ---
FROM node:22-alpine AS build
WORKDIR /app

# Copiamos package*.json PRIMERO para el cache de dependencias.
# Si no cambian las dependencias (package-lock.json), npm ci se reutiliza.
# Sin esto, cambiar cualquier archivo TypeScript re-instalaría todo (~30s).
COPY package*.json ./

# npm ci en vez de npm install:
#   - npm ci borra node_modules y reinstala desde package-lock.json
#   - Más rápido (~10s menos) y siempre reproducible
#   - En CI es la práctica estándar (npm install puede modificar package-lock.json)
RUN npm ci

# Copiamos TODO el código fuente y compilamos.
COPY . .

# ng build --configuration production:
#   - Optimización: minifica JS/CSS, elimina código muerto
#   - Hash en archivos: cache busting para browsers
#   - Output en dist/workflow-net/browser/
#   - Las fuentes NO se inlinean (fonts: false en angular.json) porque
#     Docker no tiene internet para descargar Google Fonts durante el build.
#     Las fuentes se cargan en runtime via <link> en index.html.
RUN npm run build

# --- Stage 2: Runtime (Nginx) ---
# Nginx alpine: ~5MB, sirve archivos estáticos con mejor rendimiento que Node.
FROM nginx:alpine

# Copiamos los archivos compilados del stage anterior.
COPY --from=build /app/dist /usr/share/nginx/html

# Nginx escucha en puerto 80 (HTTP).
# En producción, Traefik se encarga del TLS (HTTPS) y redirige a este puerto.
EXPOSE 80

# nginx con "daemon off" para que corra en foreground dentro del contenedor.
# Docker necesita que el proceso principal corra en foreground, sino el contenedor se detiene.
CMD ["nginx", "-g", "daemon off;"]
