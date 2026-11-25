#!/bin/bash
# Copia unidireccional. Compatible con Bash 3.2 del Mac.
set -e
umask 077
base=/Users/enrique
origen="$base/PonPox"
cloud="$base/Library/CloudStorage"
fallar() { echo "No se ha completado la copia: $*" >&2; exit 1; }
[[ -d "$origen" && ! -L "$origen" ]] || fallar "No encuentro la carpeta original: $origen"
google="$cloud/GoogleDrive-enrique.arnal@gmail.com/Mi unidad"
[[ -d "$google" ]] || google="$cloud/GoogleDrive-enrique.arnal@gmail.com/My Drive"
icloud="$base/Library/Mobile Documents/com~apple~CloudDocs"
one="$cloud/OneDrive-Personal(2)"
[[ -d "$one" ]] || one="$cloud/OneDrive-Personal"
destinos=("$google" "$icloud" "$one")
nombres=("Google Drive" "iCloud Drive" "OneDrive")
for i in 0 1 2; do
  raiz=${destinos[$i]}
  [[ -d "$raiz" && -w "$raiz" ]] || fallar "${nombres[$i]} no está disponible en $raiz. Abre el servicio en Finder y comprueba la ruta. No se ha iniciado la copia."
  [[ ! -L "$raiz/PonPox" ]] || fallar "El destino $raiz/PonPox es un enlace."
  [[ ! -e "$raiz/PonPox" || -d "$raiz/PonPox" ]] || fallar "Ya existe un archivo llamado PonPox en $raiz."
  printf '%s → %s/PonPox\n' "${nombres[$i]}" "$raiz"
done
printf '\nOrigen: %s\n' "$origen"
echo 'Cierra PonPox y guarda los cambios de VS Code antes de continuar.'
echo 'Confirma que los tres destinos son privados. Se incluyen archivos ocultos y datos personales si están en el origen.'
echo 'Las versiones del Mac sustituyen archivos del mismo nombre. No se eliminan archivos sobrantes.'
read -r -p 'Para iniciar escribe COPIAR: ' respuesta
[[ "$respuesta" == COPIAR ]] || { echo 'Cancelado. No se ha copiado nada.'; exit 0; }
estado=0
for i in 0 1 2; do
  destino="${destinos[$i]}/PonPox"
  echo "Actualizando ${nombres[$i]}…"
  if mkdir -p "$destino" && /usr/bin/rsync -rltp --checksum --stats "$origen/" "$destino/"; then
    echo "Copia local completada: $destino"
  else
    echo "Copia incompleta en ${nombres[$i]}. Revisa los mensajes anteriores." >&2
    estado=1
  fi
done
echo 'Comprueba en Finder que cada servicio ha terminado de subir los archivos.'
exit "$estado"
