# PonPox

## Versión Producción V24

La pestaña **Informes** se ha reconstruido. Ahora presenta tres accesos claros:

- **Informe por presupuesto**
- **Informe por trabajador**
- **Informe por cliente**

Cada acceso abre su propio configurador. Permite seleccionar un presupuesto, trabajador o cliente concreto —o `Todos`—, elegir uno o varios meses del año, asignar un nombre al informe, elegir la ordenación y marcar libremente cualquiera de los campos disponibles. Entre ellos están evento, venue, vestuario, fechas y horario, trabajador, IBAN, salario neto, costes, márgenes y base imponible. El resultado se previsualiza en pantalla antes de exportarlo a Excel o PDF.

Se ha eliminado el error de exportación que utilizaba la descripción interna de un campo de pantalla como si fuera el nombre de archivo. A partir de esta versión el archivo se crea usando exclusivamente el título escrito para el informe.

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

## Versión Producción V23

Al guardar una ficha de presupuesto, la lista de clientes ya no sustituye la selección actual por el primer cliente disponible. Esto evita que un segundo guardado automático al cerrar cambie el cliente de la referencia abierta.

Al generar un informe de un **número de presupuesto concreto**, ese número tiene prioridad sobre el filtro de clientes: se recuperan sus jornadas ya registradas aunque el cliente hubiera quedado seleccionado de forma incorrecta en una sesión anterior. Los clientes que aparezcan por primera vez al actualizar filtros se seleccionan por defecto, evitando que el informe quede sin resultados por una selección vacía.

## Versión Producción V22

Corrige la apertura de presupuestos desde **Producción presupuestaria**: al seleccionar una fila, PonPox carga la cabecera completa de esa misma referencia —incluido su cliente— y sus líneas, sin arrastrar el cliente de otro presupuesto.

Los informes ya no requieren que exista una jornada de análisis económico para mostrar un presupuesto. Un presupuesto importado y guardado aparece en el informe del año y mes de su servicio; sus columnas de trabajador, IBAN, salario o costes permanecen vacías hasta que se registren jornadas. Cuando sí existen jornadas, se generan sus filas por trabajador y día.

## Versión Producción V21

La ventana principal de Producción queda reducida a dos pestañas: **Producción presupuestaria** e **Informes**. La primera presenta una tabla persistente de presupuestos ordenados por número, con sus datos principales y total sin IVA. Selecciona una línea para abrirla y modificarla o para acceder a su **Análisis económico**.

La importación ya guarda inmediatamente el presupuesto y sus líneas; asimismo, al cerrar el editor de un presupuesto con número y evento, PonPox guarda sus cambios de manera automática. Por ello, al volver a abrir Producción, la tabla muestra lo ya guardado.

## Versión Producción V20

El botón **Generar informe** se muestra ahora directamente al final de los filtros, antes de las opciones de exportación. El orden de uso es: definir filtros, marcar columnas, generar y comprobar las filas en pantalla y, por último, exportar a XLS o PDF.

## Versión Producción V19

La pestaña **Informes** incorpora filtros encadenados para trabajar con muchos registros: selecciona un año y uno o varios meses; entonces Clientes muestra únicamente quienes tienen producción registrada en ese período y Presupuesto únicamente las referencias de esos clientes. Ambos conservan la opción **Todos** y permiten excluir clientes concretos antes de generar el informe. También puede ordenarse por presupuesto, cliente o trabajador.

Los campos seleccionables cubren la información disponible de presupuesto, cliente, evento, venue, vestuario, trabajador y análisis económico. Así se puede generar, por ejemplo, un informe de un trabajador con solo `Evento`, `Venue`, `Día trabajado`, `Hora desde`, `Hora hasta` y `Vestuario`.

Al importar un presupuesto PDF, su cabecera y sus líneas se guardan de inmediato. En Análisis económico se puede eliminar la jornada seleccionada con confirmación; la producción y el historial de experiencia se actualizan en consecuencia.

## Versión Producción V18

El generador de informes corrige la consulta de jornadas y permite asignar a cada informe un título propio. El título se aplica al PDF y al nombre sugerido para el archivo exportado; las columnas continúan siendo totalmente seleccionables y las filas se ordenan por presupuesto, día y trabajador.

## Versión Producción V17

Esta actualización limpia los avisos de mantenimiento detectados por VS Code: se han retirado importaciones y código obsoleto, y el listado de Clientes muestra ahora dirección, país, tipo, referencia y moneda. No cambia la estructura ni el contenido de la base de datos.

## Versión Producción V16

La pantalla principal contiene tres áreas: **Trabajadores**, **Clientes** y **Producción**.

- **Clientes** carga automáticamente la base inicial incluida en `Clientes-2026-11-23.csv` cuando aún no exista una base de clientes. Para actualizarla después, abre Clientes y pulsa `Importar clientes CSV`.
- En **Producción**, guarda primero la cabecera del presupuesto. Después, en `Producción del presupuesto`, elige el presupuesto y pulsa `Cargar / modificar líneas`: podrás revisar, añadir, editar o borrar líneas antes de guardar los cambios. Cada importe se calcula con `cantidad × precio sin IVA − descuento`.
- El botón `Importar presupuesto PDF` interpreta las fechas de servicio abreviadas —por ejemplo, `(23,24/1)`— como los días 23 y 24 de enero del año del presupuesto, e identifica el cliente que aparece como destinatario. Si no existía, lo incorpora a Clientes. Confirma siempre la fecha antes de guardar.
- También puedes crear un presupuesto desde cero con `Nuevo presupuesto` y exportarlo con `Exportar presupuesto PDF`, con cabecera de PonPox, destinatario, datos del servicio, líneas e importes.
- El análisis económico se registra por **trabajador y día**. Las jornadas guardadas se muestran bajo la ficha del presupuesto y se pueden pulsar para corregirlas sin perder el resto de líneas.
- `Previsualizar producción` muestra todas las jornadas guardadas del presupuesto seleccionado, con salario neto, otros gastos sin PSS y los totales del presupuesto. `Comprobar con presupuesto` compara la base producida —con márgenes— con las líneas presupuestadas y señala posibles causas de diferencia.
- En `Informes` se genera un cuadro de nóminas nuevo en cada consulta. Puedes filtrar por intervalo de fechas, número de presupuesto y uno o varios clientes (separados por comas), y después exportar exclusivamente ese resultado a XLS o PDF.
- La pestaña `Informes` es ahora un generador personalizable: en `Filtros` puedes delimitar cliente(s), presupuesto, mes y rango de días; en `Campos a incluir` marcas las columnas que quieres obtener. Incluye los datos de jornada, trabajador, márgenes, costes, PSS y base imponible.
- En `Producción del presupuesto` hay búsqueda por cliente y por número de presupuesto para trabajar cómodamente con una base extensa.
- La edición de líneas muestra siempre el **total del presupuesto sin IVA**. La previsualización y la comprobación comparan expresamente ese total con la base producida.
- En análisis económico, `Costes cotizables = salario neto × (1 + margen bruto laboral)`. El campo `Total costes` es `costes cotizables + total de otros costes`.

La base de datos ya no depende de la carpeta del proyecto. En Mac se guarda de forma permanente en `~/Library/Application Support/PonPox/ponpox.db`; la primera vez que abras esta versión copia allí la antigua base que tuvieras en la carpeta del proyecto. Cada guardado o importación crea una copia fechada en `~/Library/Application Support/PonPox/copias de seguridad`. Las actualizaciones de PonPox no reemplazan esa base.

El historial de cada trabajador se actualiza automáticamente al guardar su análisis económico: queda asociado al presupuesto, evento y día. En su pestaña `Historial de eventos` puedes pulsar `Actualizar historial`. Desde Clientes, selecciona un cliente y pulsa `Presupuestos del cliente` para consultar los presupuestos producidos por periodo y exportarlos a PDF o Excel.
