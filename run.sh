#!/bin/bash
set -euo pipefail

MODE="${1:-gui}"

if [[ "$MODE" != "gui" && "$MODE" != "console" ]]; then
    echo "Uso: ./run.sh [gui|console]"
    exit 2
fi

CURRENT_IP=$(curl -s ifconfig.me)

echo "IP pública actual: $CURRENT_IP"

echo "Actualizando la regla de firewall MiPC..."
az sql server firewall-rule update \
    --resource-group business-sales-rg \
    --server joshuaroot \
    --name MiPC \
    --start-ip-address "$CURRENT_IP" \
    --end-ip-address "$CURRENT_IP"

echo "Firewall actualizado correctamente: $CURRENT_IP"

if [[ "$MODE" == "gui" ]]; then
    echo "Iniciando la interfaz JavaFX local..."
    mvn clean javafx:run
    exit $?
fi

docker build -t business-sales-core .

mkdir -p exports

docker run -it \
    --env-file .env \
    -v "$(pwd)/.env:/app/.env:ro" \
    -v "$(pwd)/exports:/app/exports" \
    business-sales-core --console