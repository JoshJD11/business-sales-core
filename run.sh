#!/bin/bash
set -euo pipefail

MODE="${1:-gui}"

if [[ "$MODE" != "gui" && "$MODE" != "console" ]]; then
    echo "Uso: ./run.sh [gui|console]"
    exit 2
fi

CURRENT_IP=$(curl -s ifconfig.me)

echo "IP pública actual: $CURRENT_IP"

AZURE_IP=$(az sql server firewall-rule show \
    --resource-group business-sales-rg \
    --server joshuaroot \
    --name MiPC \
    --query startIpAddress \
    --output tsv 2>/dev/null)

if [[ "$AZURE_IP" == "$CURRENT_IP" ]]; then
    echo "El firewall ya tiene la IP correcta: $AZURE_IP"
else
    echo "La IP del firewall cambió."
    echo "IP en Azure: $AZURE_IP"
    echo "IP actual:   $CURRENT_IP"
    echo "Actualizando la regla de firewall MiPC..."

    az sql server firewall-rule update \
        --resource-group business-sales-rg \
        --server joshuaroot \
        --name MiPC \
        --start-ip-address "$CURRENT_IP" \
        --end-ip-address "$CURRENT_IP"

    echo "Firewall actualizado correctamente: $CURRENT_IP"
fi

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