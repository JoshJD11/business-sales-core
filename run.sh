#!/bin/bash

CURRENT_IP=$(curl -s ifconfig.me)

echo "IP pública actual: $CURRENT_IP"

AZURE_IP=$(az sql server firewall-rule show \
    --resource-group business-sales-rg \
    --server joshuaroot \
    --name MiPC \
    --query startIpAddress \
    --output tsv 2>/dev/null)

if [ "$AZURE_IP" = "$CURRENT_IP" ]; then

    echo "El firewall ya tiene la IP correcta: $AZURE_IP"

else

    echo "La IP del firewall no coincide."
    echo "IP en Azure: $AZURE_IP"
    echo "IP actual:   $CURRENT_IP"
    echo "Actualizando firewall..."

    az sql server firewall-rule update \
        --resource-group business-sales-rg \
        --server joshuaroot \
        --name MiPC \
        --start-ip-address "$CURRENT_IP" \
        --end-ip-address "$CURRENT_IP"

    if [ $? -ne 0 ]; then
        echo "ERROR: No se pudo actualizar el firewall."
        exit 1
    fi

    echo "Firewall actualizado correctamente."

fi

docker build -t business-sales-core .

mkdir -p exports

docker run -it \
    --env-file .env \
    -v "$(pwd)/.env:/app/.env:ro" \
    -v "$(pwd)/exports:/app/exports" \
    business-sales-core