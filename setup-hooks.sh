#!/usr/bin/env bash
# =============================================
#  Configurar Git Hooks del proyecto
#  Ejecutar una vez despues de clonar el repositorio
# =============================================

echo "Configurando Git Hooks..."
echo ""

git config core.hooksPath .githooks

if [ $? -eq 0 ]; then
  echo "[OK] Git Hooks configurados correctamente."
  echo "    Ruta: .githooks/"
  echo ""
  echo "Hooks activos:"
  echo "  - pre-commit:  Bloquea commits en main/master"
  echo "  - commit-msg:  Valida Conventional Commits"
  echo "  - pre-push:    Bloquea push a ramas protegidas"
else
  echo "[ERROR] No se pudieron configurar los hooks."
  echo "Asegurate de estar en la raiz del repositorio."
fi
