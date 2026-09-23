#!/bin/bash

set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

MODE="${1:-gui}"

if [[ "$MODE" != "gui" && "$MODE" != "console" ]]; then
    echo "Uso: ./run.sh [gui|console]"
    exit 2
fi

CURRENT_IP=$(curl -s ifconfig.me)
LAST_IP_FILE=".last_ip"

echo "IP pública actual: $CURRENT_IP"

if [[ -f "$LAST_IP_FILE" ]]; then
    LAST_IP=$(cat "$LAST_IP_FILE")
else
    LAST_IP=""
fi

if [[ "$LAST_IP" == "$CURRENT_IP" ]]; then
    echo "La IP no ha cambiado ($CURRENT_IP). No es necesario actualizar el firewall."
else
    echo "La IP ha cambiado."

    if [[ -n "$LAST_IP" ]]; then
        echo "IP anterior: $LAST_IP"
    else
        echo "No existe una IP guardada anteriormente."
    fi

    echo "Actualizando la regla de firewall MiPC..."

    az sql server firewall-rule update \
        --resource-group business-sales-rg \
        --server joshuaroot \
        --name MiPC \
        --start-ip-address "$CURRENT_IP" \
        --end-ip-address "$CURRENT_IP"

    echo "Firewall actualizado correctamente: $CURRENT_IP"

    echo "$CURRENT_IP" > "$LAST_IP_FILE"

    echo "IP guardada en $LAST_IP_FILE"
fi

if [[ "$MODE" == "gui" ]]; then
    echo "Iniciando la interfaz JavaFX local..."

    LOG_FILE="/tmp/business-sales-core.log"
    ./mvnw clean javafx:run > "$LOG_FILE" 2>&1
    EXIT_CODE=$?

    if [[ $EXIT_CODE -ne 0 ]]; then
        echo "La app terminó con error. Revisa: $LOG_FILE"
    fi

    exit $EXIT_CODE
fi

docker build -t business-sales-core .

mkdir -p exports

docker run -it \
    --env-file .env \
    -v "$(pwd)/.env:/app/.env:ro" \
    -v "$(pwd)/exports:/app/exports" \
    business-sales-core --console