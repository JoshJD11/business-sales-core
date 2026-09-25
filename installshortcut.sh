#!/bin/bash
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DESKTOP_DIR="$HOME/Desktop"
DESKTOP_FILE="$DESKTOP_DIR/BusinessSalesCore.desktop"
ICON_SOURCE="$DIR/src/main/resources/app-icon.png"

mkdir -p "$DESKTOP_DIR"

cat > "$DESKTOP_FILE" <<EOF
[Desktop Entry]
Type=Application
Name=Business Sales Core
Comment=Ejecuta mi proyecto Java
Exec=$DIR/run.sh
Icon=$ICON_SOURCE
Terminal=false
Categories=Development;
EOF

chmod +x "$DESKTOP_FILE"

if command -v gio &> /dev/null; then
    gio set "$DESKTOP_FILE" metadata::trusted true 2>/dev/null || echo "⚠️  No se pudo marcar como confiable automáticamente (puede que no sea necesario en tu sistema)"
fi

echo "✅ Ícono creado en: $DESKTOP_FILE"