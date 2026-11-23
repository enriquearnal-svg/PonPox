# PonPox

Séptima versión del interfaz de gestión de recursos humanos y producción para personal de eventos, congresos y acciones de imagen de marca.

## Ejecutar en VSCode

1. Abre la carpeta `PonPox` en Visual Studio Code.
2. Abre `src/PonPox.java`.
3. Pulsa el botón **Run** que aparece sobre el método `main`.

La ventana inicial incorpora el logotipo de la empresa y dos accesos claramente visibles:

- **RR.HH.**: abre una ventana independiente con tres pestañas: `Personal` (listado, búsqueda y ordenación), `Ficha / alta` (creación y modificación) e `Historial de eventos`.
- **Producción**: eventos, necesidades del cliente, puestos, equipos, incidencias y cierre de servicios.

Cada ficha se guarda en la base local `ponpox.db`, creada automáticamente junto al proyecto la primera vez que se pulsa **Guardar ficha**. La tabla se puede ordenar pulsando sobre sus cabeceras, buscar y abrir para modificar registros existentes.

Desde la pestaña `Personal`, marca la casilla de la columna `Informe` en una persona o en el grupo que quieras exportar. Después se pueden generar informes en formato Excel (`.xls`) y PDF (`.pdf`). Antes de exportar, PonPox pregunta qué campos deben aparecer. Los datos especialmente sensibles —DNI/NIE, número de Seguridad Social e IBAN— quedan desmarcados por defecto. Cada profesional se presenta como una ficha vertical, en líneas de campo y valor: el PDF incluye el logotipo de PonPox en cada ficha y el Excel incorpora la cabecera corporativa.

La ficha de persona contiene nombre, dos apellidos, DNI/NIE, número de Seguridad Social, fecha de nacimiento, dirección completa, IBAN, teléfono, altura, tallas de vestuario, calzado, idiomas certificados y categorías de experiencia: Eventos, Imagen, Congresos, Promoción, Feria y Carga/descarga.

Producción dispone de las pestañas `N.º presupuesto`, `Evento`, `Fechas del servicio` y `Personal`. Esta última carga el personal existente desde RR.HH. para poder marcar quién participará en cada servicio. Al guardar una producción, el evento y sus fechas quedan vinculados automáticamente al historial laboral de las personas asignadas.

Como contiene datos personales sensibles, no se debe compartir el archivo `ponpox.db` ni subirlo a un repositorio público. En una fase posterior añadiremos perfiles de acceso y copias de seguridad.
