#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../backend"
if [[ ! -f target/lexmovil-api-0.1.0.jar ]]; then
  mvn package
fi
exec java -jar target/lexmovil-api-0.1.0.jar
