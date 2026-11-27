#!/usr/bin/env python3
"""Copia archivos autorizados de PonPox a una carpeta nueva, sin usar Git."""

import argparse
from pathlib import Path
import shutil

ARCHIVOS = (
    ".gitignore",
    ".vscode/settings.json",
    "Logotipo PonPox.png",
    "lib/itext-5.0.6.jar",
    "lib/slf4j-api-2.0.12.jar",
    "lib/slf4j-simple-2.0.12.jar",
    "lib/sqlite-jdbc-3.45.1.0.jar",
    "plantillas/Reporte.pdf",
    "plantillas/Setcards.pdf",
    "plantillas/Setcards.pptx",
    "src/Carpetas.java",
    "src/DocumentosPonPox.java",
    "src/Oficina.java",
    "src/PonPox.java",
    "herramientas/exportar-codigo.py",
    "windows/COMPILAR.md",
    "windows/PonPox.iss",
    "windows/RecursosInstalador.java",
    "windows/compilar.ps1",
    "windows/manual.txt",
    "windows/preparar-datos.py",
)

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("destino", type=Path, help="Carpeta nueva fuera del proyecto")
args = parser.parse_args()
raiz = Path(__file__).resolve().parents[1]
destino = args.destino.resolve()
if destino.is_relative_to(raiz) or destino.exists():
    parser.error("El destino debe ser una carpeta nueva fuera del proyecto.")

for nombre in ARCHIVOS:
    origen = raiz / nombre
    if origen.resolve() != origen or not origen.is_file():
        parser.error(f"Falta un archivo autorizado o hay un enlace simbólico: {nombre}")

destino.mkdir(mode=0o700)
for nombre in ARCHIVOS:
    copia = destino / nombre
    copia.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(raiz / nombre, copia)

print(f"{len(ARCHIVOS)} archivos copiados en {destino}. Revisa su contenido antes de publicarlos.")
