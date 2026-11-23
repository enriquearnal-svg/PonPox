# PonPox

Octava versión del interfaz de gestión de recursos humanos y producción para personal de eventos, congresos y acciones de imagen de marca.

## Ejecutar en VSCode

1. Abre la carpeta `PonPox` en Visual Studio Code.
2. Abre `src/PonPox.java`.
3. Pulsa el botón **Run** que aparece sobre el método `main`.

La ventana inicial incorpora el logotipo de la empresa y dos accesos claramente visibles:

- **RR.HH.**: abre una ventana independiente con tres pestañas: `Personal` (listado, búsqueda y ordenación), `Ficha / alta` (creación y modificación) e `Historial de eventos`.
- **Producción**: eventos, necesidades del cliente, puestos, equipos, incidencias y cierre de servicios.

Cada ficha se guarda en la base local `ponpox.db`, creada automáticamente junto al proyecto la primera vez que se pulsa **Guardar ficha**. La tabla se puede ordenar pulsando sobre sus cabeceras, buscar y abrir para modificar registros existentes.

Desde la pestaña `Personal`, marca la casilla de la columna `Informe` en una persona o en el grupo que quieras exportar. Después se pueden generar informes en formato Excel (`.xls`) y PDF (`.pdf`). Antes de exportar, PonPox pregunta qué campos deben aparecer. Los datos especialmente sensibles —DNI/NIE, número de Seguridad Social e IBAN— quedan desmarcados por defecto. Cada profesional se presenta como una ficha vertical, en líneas de campo y valor: el PDF incluye el logotipo de PonPox en cada ficha y el Excel incorpora la cabecera corporativa.

La ficha de persona contiene nombre completo, DNI/NIE, número de Seguridad Social, fecha de nacimiento, dirección completa, IBAN, teléfono, altura, tallas de vestuario, calzado, idiomas certificados, observaciones y categorías de experiencia: Eventos, Imagen, Congresos, Promoción, Feria y Carga/descarga.

En la pestaña `Personal`, el botón `Importar Excel` permite cargar el listado de la hoja `AZAF+IBAN@6 NOV` del cuadro comercial de PonPox. Crea fichas para las personas nuevas e incorpora IBAN, ciudad y observaciones cuando estos datos aún estén vacíos. Si encuentra valores distintos en una ficha existente, muestra la divergencia y permite conservar los datos de la ficha, usar los del Excel o detener la importación para revisar la ficha manualmente. El resto de columnas del Excel se omite.

Producción dispone de las pestañas `N.º presupuesto`, `Evento`, `Fechas del servicio` y `Personal`. Esta última carga el personal existente desde RR.HH. para poder marcar quién participará en cada servicio. Al guardar una producción, el evento y sus fechas quedan vinculados automáticamente al historial laboral de las personas asignadas.

Como contiene datos personales sensibles, no se debe compartir el archivo `ponpox.db` ni subirlo a un repositorio público. En una fase posterior añadiremos perfiles de acceso y copias de seguridad.
# PonPox

## Versión Producción V10

La pantalla principal contiene tres áreas: **Trabajadores**, **Clientes** y **Producción**.

- **Clientes** carga automáticamente la base inicial incluida en `Clientes-2026-11-23.csv` cuando aún no exista una base de clientes. Para actualizarla después, abre Clientes y pulsa `Importar clientes CSV`.
- En **Producción**, guarda primero la cabecera del presupuesto. Después, en `Producción del presupuesto`, elige el presupuesto y pulsa `Cargar / modificar líneas`: podrás revisar, añadir, editar o borrar líneas antes de guardar los cambios. Cada importe se calcula con `cantidad × precio sin IVA − descuento`.
- El botón `Importar presupuesto PDF` interpreta las fechas de servicio abreviadas —por ejemplo, `(23,24/1)`— como los días 23 y 24 de enero del año del presupuesto. Confirma siempre la fecha antes de guardar.
- El análisis económico se registra por **trabajador y día**. Tras guardar una jornada, la ficha queda preparada para registrar el siguiente trabajador o el mismo trabajador en otro día.
- `Previsualizar producción` muestra todas las jornadas guardadas del presupuesto seleccionado, con salario neto, otros gastos sin PSS y los totales del presupuesto. `Cuadro de nóminas` muestra esas mismas jornadas y una única fila TOTAL por presupuesto, exportable a XLS o PDF.

La base de datos ya no depende de la carpeta del proyecto. En Mac se guarda de forma permanente en `~/Library/Application Support/PonPox/ponpox.db`; la primera vez que abras esta versión copia allí la antigua base que tuvieras en la carpeta del proyecto. Cada guardado o importación crea una copia fechada en `~/Library/Application Support/PonPox/copias de seguridad`. Las actualizaciones de PonPox no reemplazan esa base.

El historial de cada trabajador se actualiza automáticamente al guardar su análisis económico: queda asociado al presupuesto, evento y día. En su pestaña `Historial de eventos` puedes pulsar `Actualizar historial`. Desde Clientes, selecciona un cliente y pulsa `Presupuestos del cliente` para consultar los presupuestos producidos por periodo y exportarlos a PDF o Excel.
