#!/usr/bin/env python3
"""Prepara una copia privada. Cierra PonPox antes de ejecutarlo."""
import argparse
import hashlib
import json
import os
import shutil
import sqlite3
import unicodedata
from contextlib import closing
from pathlib import Path

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--base", type=Path, required=True)
parser.add_argument("--informes", type=Path, action="append", required=True)
parser.add_argument("--documentos", type=Path)
parser.add_argument("--salida", type=Path, required=True)
args = parser.parse_args()
repo = Path(__file__).resolve().parents[1]
output = args.salida.resolve()
if output.is_relative_to(repo):
    parser.error("Guarda la copia privada fuera del repositorio.")
base = args.base.resolve(strict=True)
os.umask(0o077)
output.mkdir(mode=0o700)
backup = output / "respaldo"
seed = output / "datos"
backup.mkdir()
seed.mkdir()

with closing(sqlite3.connect(base.as_uri() + "?mode=ro", uri=True)) as original:
    with closing(sqlite3.connect(backup / "ponpox.db")) as snapshot:
        original.backup(snapshot)
        if snapshot.execute("PRAGMA quick_check").fetchone() != ("ok",):
            raise ValueError("La base de origen no supera la comprobación de integridad.")

shutil.copy2(backup / "ponpox.db", seed / "ponpox.db")
photos = seed / "fotos trabajadores"
photos.mkdir()
with closing(sqlite3.connect(seed / "ponpox.db")) as conn, conn:
    records = conn.execute(
        "SELECT personal_id, posicion, ruta FROM personal_fotos WHERE coalesce(ruta, '') <> ''"
    ).fetchall()
    for person_id, slot, path in records:
        source = Path(path)
        if not source.is_absolute():
            source = base.parent / source
        extension = source.suffix.lower()
        if not extension.removeprefix(".").isascii() or not extension[1:].isalnum():
            extension = ".img"
        filename = f"trabajador_{person_id}_foto_{slot + 1}{extension}"
        shutil.copy2(source, photos / filename)
        conn.execute(
            "UPDATE personal_fotos SET ruta = ? WHERE personal_id = ? AND posicion = ?",
            (f"fotos trabajadores/{filename}", person_id, slot),
        )
    counts = {
        table: conn.execute(f"SELECT COUNT(*) FROM {table}").fetchone()[0]
        for table in ("personal", "clientes", "presupuestos", "pdf_presupuesto")
    }

shutil.copytree(photos, backup / photos.name)
reports = seed / "Informes"
for area in ("Trabajadores", "Clientes", "Producción"):
    (reports / area).mkdir(parents=True)

# Windows compara nombres sin distinguir mayúsculas. Conserva las dos versiones
# de un archivo cuando coinciden las rutas y el contenido es distinto.
folders = {}
files = {}


def copy_tree(source, destination, include_archives=True):
    def walk(src, dest):
        dest.mkdir(parents=True, exist_ok=True)
        for item in sorted(src.iterdir()):
            if item.is_symlink() or item.name == ".DS_Store":
                continue
            name = unicodedata.normalize("NFC", item.name)
            if (
                any(char in name for char in '<>:"/\\|?*')
                or name.endswith((" ", "."))
                or name.split(".")[0].upper() in {
                    "CON", "PRN", "AUX", "NUL",
                    *(f"{prefix}{i}" for prefix in ("COM", "LPT") for i in range(1, 10)),
                }
            ):
                raise ValueError(f"Nombre incompatible con Windows: {item}")
            candidate = dest / name
            if item.is_dir():
                walk(item, folders.setdefault(str(candidate).casefold(), candidate))
            elif item.is_file() and (include_archives or item.suffix.lower() != ".zip"):
                with item.open("rb") as stream:
                    digest = hashlib.sha256(stream.read()).digest()
                index = 2
                while str(candidate).casefold() in files:
                    if files[str(candidate).casefold()] == digest:
                        break
                    candidate = dest / f"{Path(name).stem} ({index}){Path(name).suffix}"
                    index += 1
                else:
                    shutil.copy2(item, candidate)
                    files[str(candidate).casefold()] = digest

    walk(source.resolve(strict=True), destination)


for root in args.informes:
    copy_tree(root, reports)
if args.documentos:
    copy_tree(args.documentos, reports / "Documentos existentes", include_archives=False)
shutil.copytree(reports, backup / reports.name)
(seed / "instalacion-datos.txt").write_text("PonPox Windows\n", encoding="utf-8")
print(json.dumps({**counts, "fotos": len(records), "archivos": len(files)}, ensure_ascii=False))
