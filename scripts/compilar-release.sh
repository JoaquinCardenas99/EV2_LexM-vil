#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../Ev2_DesarrolloAPL"
if [[ ! -f keystore.properties ]]; then
  echo "Falta keystore.properties. Consulta la sección Firma del README."
  exit 1
fi
# URL de la API propia: predeterminada para el emulador Android Studio.
api_url="${LEXMOVIL_API_URL:-http://10.0.2.2:8080/api/}"
http_local=false
if [[ "$api_url" == http://* ]]; then http_local=true; fi
bash gradlew testDebugUnitTest assembleRelease   -Pkotlin.compiler.execution.strategy=in-process   -PLEXMOVIL_API_URL="$api_url" -PLEXMOVIL_ALLOW_HTTP="$http_local"
