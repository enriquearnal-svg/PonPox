# Instalador de PonPox para Windows x64

El paquete se compila en Windows con un JDK Temurin 25 x64 e Inno Setup 6.3 o posterior. La edición de Windows requiere como mínimo la compilación 17763 de Windows 10. El instalador es por usuario e incorpora su propio runtime. Es compatible con el esquema de carpetas que usaba PonPox en Windows.

## Preparar los datos en el Mac

Cierra PonPox y ejecuta desde la raíz del proyecto. La carpeta de salida debe ser nueva, privada y estar fuera del repositorio. Se genera un respaldo SQLite y se adaptan las rutas de fotos únicamente en la copia de distribución.

```sh
python3 windows/preparar-datos.py \
  --base "$HOME/Library/Application Support/PonPox/ponpox.db" \
  --informes Informes \
  --informes "$HOME/Documents/PonPox/Informes" \
  --documentos "../PonPox documentos" \
  --salida "/ruta/privada/entrega"
```

## Compilar en Windows

Transfiere por un canal privado `src/`, `lib/`, `plantillas/`, el logotipo y `windows/`, conservando la jerarquía del proyecto. Transfiere por separado `entrega/datos/`. Conserva `entrega/respaldo/` en el Mac. Configura `JAVA_HOME` con la ubicación del JDK de compilación.

```powershell
.\windows\compilar.ps1 -DatosPrivados 'D:\Privado\datos' -Salida 'D:\Privado\publicacion'
```

El resultado será `D:\Privado\publicacion\PonPox_Setup.exe`. El script calcula su SHA-256. Si dispones de certificado Authenticode de la empresa, firma el instalador y calcula el SHA-256 definitivo después de firmar.

GitHub Actions en Windows puede empaquetar una compilación de prueba con datos sintéticos. La compilación con datos reales y sus pruebas deben ejecutarse en un equipo Windows privado. Los paquetes que incorporan datos reales se conservan y distribuyen fuera de GitHub.

## Verificar antes de distribuir

1. En una cuenta de prueba limpia y sin Java instalado, ejecuta el instalador en Windows 10 x64 y Windows 11 x64. Comprueba que el asistente está en castellano, la instalación es por usuario y los dos accesos directos y el manual funcionan.
2. Abre una ficha con foto, consulta un presupuesto importado y abre los archivos desde **Documentos > Abrir carpeta de informes**. Revisa los recuentos iniciales. Guarda un cambio de prueba y genera un informe PDF. Cierra PonPox y confirma que los cambios siguen visibles al volver a abrirlo.
3. Con PonPox cerrado, registra los hashes de `%USERPROFILE%\.ponpox\ponpox.db` y del informe de prueba. Reinstala con PonPox cerrado y compara ambos hashes antes de abrirlo. Comprueba también que los archivos de datos sobreviven a la desinstalación.
4. Si todas las comprobaciones pasan, comparte el EXE mediante un enlace privado restringido a los destinatarios autorizados. El EXE inicial contiene los datos de trabajo. Remite el SHA-256 definitivo por un canal autenticado.
