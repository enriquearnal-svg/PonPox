#!/bin/bash
set -eu
cd "$(dirname "$0")"
kit="$PWD"
app="$kit/PonPox.app"
[[ -d "$app" ]] || app="/Applications/PonPox.app"
[[ -d "$app" ]] || { echo "No encuentro PonPox.app en esta carpeta ni en Aplicaciones."; exit 1; }
envio="$(mktemp -d "$kit/Envios-PonPox-XXXXXX")"
datos="$envio/Datos-oficina.ppx"
"$app/Contents/MacOS/PonPox" exportar "$datos"
[[ -s "$datos" ]] || { echo "Cancelado: no se ha preparado ningún envío."; exit 1; }
for plataforma in Mac Windows; do
  nombre="PonPox-Oficina-$plataforma.zip"
  cp "$kit/Modelos/$nombre" "$envio/$nombre"
  (cd "$envio" && /usr/bin/zip -q -0 "$nombre" Datos-oficina.ppx && /usr/bin/unzip -tq "$nombre")
done
open "$envio"
echo "Listos: envía solo el ZIP correspondiente a cada persona. La contraseña va por otro canal."
