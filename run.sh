#!/bin/bash

CURRENT_IP=$(curl -s ifconfig.me)

LAST_IP_FILE=".last_ip"

if [ -f "$LAST_IP_FILE" ] && [ "$(cat $LAST_IP_FILE)" == "$CURRENT_IP" ]; then

    echo "La IP no ha cambiado ($CURRENT_IP). No es necesario actualizar el firewall."

else

    echo "Actualizando firewall con la IP actual: $CURRENT_IP"

    az sql server firewall-rule create \
        --resource-group business-sales-rg \
        --server joshuaroot \
        --name MiPC \
        --start-ip-address "$CURRENT_IP" \
        --end-ip-address "$CURRENT_IP"

    echo "$CURRENT_IP" > "$LAST_IP_FILE"

fi

docker build -t business-sales-core .

docker run -it \
    --env-file .env \
    -v "$(pwd)/.env:/app/.env:ro" \
    -v "$(pwd)/exports:/app/exports" \
    business-sales-core