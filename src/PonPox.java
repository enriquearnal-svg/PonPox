import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Pantalla inicial de PonPox: gestión de personal para eventos y congresos. */
public class PonPox {
    private static final Color ROSA = new Color(224, 45, 133);
    private static final Color ROSA_OSCURO = new Color(187, 25, 105);
    private static final Color GRAFITO = new Color(45, 45, 48);
    private static final Color GRIS_SUAVE = new Color(246, 246, 248);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            aplicarEstiloAplicacion();
            new VentanaPrincipal().setVisible(true);
        });
    }

    private static void aplicarEstiloAplicacion() {
        try {
            // Evita que macOS sustituya los colores de los controles por los del sistema.
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) { }
    }

    private static class VentanaPrincipal extends JFrame {
        VentanaPrincipal() {
            setTitle("PonPox | Gestión de personal");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setMinimumSize(new Dimension(920, 590));
            setSize(1100, 700);
            setLocationRelativeTo(null);
            setContentPane(crearContenido());
        }

        private JPanel crearContenido() {
            JPanel principal = new JPanel(new BorderLayout());
            principal.setBackground(Color.WHITE);
            principal.add(crearCabecera(), BorderLayout.NORTH);
            principal.add(crearCentro(), BorderLayout.CENTER);
            principal.add(crearPie(), BorderLayout.SOUTH);
            return principal;
        }

        private JComponent crearCabecera() {
            JPanel cabecera = new JPanel(new BorderLayout());
            cabecera.setBackground(GRAFITO);
            cabecera.setBorder(new EmptyBorder(13, 28, 13, 28));
            JLabel titulo = new JLabel("PONPOX");
            titulo.setForeground(Color.WHITE);
            titulo.setFont(new Font("Serif", Font.PLAIN, 24));
            cabecera.add(titulo, BorderLayout.WEST);
            JLabel subtitulo = new JLabel("Personal para eventos, ferias y congresos");
            subtitulo.setForeground(new Color(238, 238, 238));
            subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
            cabecera.add(subtitulo, BorderLayout.EAST);
            return cabecera;
        }

        private JComponent crearCentro() {
            JPanel centro = new JPanel(new GridBagLayout());
            centro.setBackground(Color.WHITE);
            centro.setBorder(new EmptyBorder(38, 48, 38, 48));
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = 0; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
            c.gridx = 0; c.weightx = 1; c.insets = new Insets(0, 0, 0, 24);
            centro.add(crearZonaMarca(), c);
            c.gridx = 1; c.weightx = 1.05; c.insets = new Insets(0, 24, 0, 0);
            centro.add(crearZonaAccesos(), c);
            return centro;
        }

        private JComponent crearZonaMarca() {
            JPanel zona = new JPanel(new GridBagLayout());
            zona.setOpaque(false);
            JLabel logo = new JLabel(cargarLogo());
            logo.setHorizontalAlignment(SwingConstants.CENTER);
            zona.add(logo);
            return zona;
        }

        private Icon cargarLogo() {
            try {
                File archivo = new File("Logotipo PonPox.png");
                if (!archivo.exists()) archivo = new File("../Logotipo PonPox.png");
                Image imagen = ImageIO.read(archivo);
                if (imagen != null) return new ImageIcon(imagen.getScaledInstance(430, 143, Image.SCALE_SMOOTH));
            } catch (Exception ignored) { }
            return new ImageIcon();
        }

        private JComponent crearZonaAccesos() {
            JPanel zona = new JPanel();
            zona.setLayout(new BoxLayout(zona, BoxLayout.Y_AXIS));
            zona.setBackground(GRIS_SUAVE);
            zona.setBorder(new CompoundBorder(new LineBorder(new Color(230, 230, 234), 1, true), new EmptyBorder(34, 38, 34, 38)));
            JLabel bienvenida = new JLabel("Panel de gestión");
            bienvenida.setFont(new Font("SansSerif", Font.BOLD, 28));
            bienvenida.setForeground(GRAFITO); bienvenida.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel descripcion = new JLabel("Selecciona un área para comenzar.");
            descripcion.setFont(new Font("SansSerif", Font.PLAIN, 16));
            descripcion.setForeground(new Color(95, 95, 100)); descripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
            zona.add(bienvenida); zona.add(Box.createVerticalStrut(8)); zona.add(descripcion); zona.add(Box.createVerticalStrut(30));
            zona.add(crearBoton("RR.HH.", "Alta, fichas, disponibilidad e historial", PonPox::abrirRecursosHumanos));
            zona.add(Box.createVerticalStrut(16));
            zona.add(crearBoton("Producción", "Eventos, equipos y operativa", PonPox::abrirProduccion));
            zona.add(Box.createVerticalGlue());
            return zona;
        }

        private JButton crearBoton(String titulo, String detalle, Runnable accion) {
            BotonPonPox boton = new BotonPonPox(titulo, detalle);
            boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
            boton.setPreferredSize(new Dimension(365, 88));
            boton.setAlignmentX(Component.LEFT_ALIGNMENT);
            boton.addActionListener(e -> accion.run());
            return boton;
        }

        private JComponent crearPie() {
            JLabel pie = new JLabel("PonPox · Gestión de RR.HH. y operaciones", SwingConstants.CENTER);
            pie.setOpaque(true); pie.setBackground(GRAFITO); pie.setForeground(new Color(225, 225, 225));
            pie.setBorder(new EmptyBorder(10, 10, 10, 10));
            return pie;
        }
    }

    /** Botón pintado por la aplicación: conserva el rosa corporativo también en macOS. */
    private static class BotonPonPox extends JButton {
        private final String titulo;
        private final String detalle;

        BotonPonPox(String titulo, String detalle) {
            this.titulo = titulo;
            this.detalle = detalle;
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(titulo);
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color = getModel().isPressed() ? ROSA_OSCURO : (getModel().isRollover() ? new Color(237, 65, 150) : ROSA);
            g.setColor(color);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 21));
            g.drawString(titulo, 22, 36);
            g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g.drawString(detalle, 22, 60);
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            g.drawString("›", getWidth() - 30, 52);
            g.dispose();
        }
    }

    private static void abrirRecursosHumanos() {
        JFrame ventana = crearVentanaArea("RR.HH. | Personal");
        GestionPersonal gestion = new GestionPersonal();
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(new Font("SansSerif", Font.BOLD, 14));
        gestion.setPestanas(pestanas);
        pestanas.addTab("Personal", gestion.crearListado());
        pestanas.addTab("Ficha / alta", gestion.crearFicha());
        pestanas.addTab("Historial de eventos", gestion.crearHistorial());
        ventana.setContentPane(pestanas);
        ventana.setVisible(true);
    }

    private static JFrame crearVentanaArea(String titulo) {
        JFrame ventana = new JFrame("PonPox | " + titulo);
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ventana.setMinimumSize(new Dimension(880, 620));
        ventana.setSize(1040, 720);
        ventana.setLocationByPlatform(true);
        return ventana;
    }

    private static class GestionPersonal {
        private final RepositorioPersonal repositorio = new RepositorioPersonal();
        private final DefaultTableModel modelo = new DefaultTableModel(
            new String[] { "Informe", "ID", "Nombre completo", "Teléfono", "E-mail", "Coche" }, 0) {
            @Override public Class<?> getColumnClass(int columna) { return columna == 0 ? Boolean.class : String.class; }
            @Override public boolean isCellEditable(int fila, int columna) { return columna == 0; }
        };
        private JTable tabla;
        private DefaultTableModel modeloHistorial;
        private JTabbedPane pestanas;
        private int idActual = -1;
        private final JTextField nombreCompleto = nuevoCampo();
        private final JTextField dni = nuevoCampo();
        private final JTextField fechaNacimiento = nuevoCampo();
        private final JComboBox<String> tipoVia = new JComboBox<>(new String[] { "Calle", "Avenida", "Plaza", "Paseo", "Otra" });
        private final JTextField via = nuevoCampo();
        private final JTextField numero = nuevoCampo();
        private final JTextField piso = nuevoCampo();
        private final JTextField codigoPostal = nuevoCampo();
        private final JTextField ciudad = nuevoCampo();
        private final JTextField provincia = nuevoCampo();
        private final JTextField pais = nuevoCampo();
        private final JTextField telefono = nuevoCampo();
        private final JTextField email = nuevoCampo();
        private final JTextField seguridadSocial = nuevoCampo();
        private final JTextField iban = nuevoCampo();
        private final JTextField altura = nuevoCampo();
        private final JTextField tallaPantalon = nuevoCampo();
        private final JTextField tallaCamisa = nuevoCampo();
        private final JTextField tallaCamiseta = nuevoCampo();
        private final JTextField tallaCalzado = nuevoCampo();
        private final JTextField idiomas = nuevoCampo();
        private final JTextArea observaciones = nuevaArea();
        private final JCheckBox experienciaEventos = new JCheckBox("E · Eventos");
        private final JCheckBox experienciaImagen = new JCheckBox("I · Imagen");
        private final JCheckBox experienciaCongresos = new JCheckBox("C · Congresos");
        private final JCheckBox experienciaPromocion = new JCheckBox("P · Promoción");
        private final JCheckBox experienciaFerias = new JCheckBox("F · Feria");
        private final JCheckBox experienciaCarga = new JCheckBox("C y DC · Carga y descarga");
        private final JComboBox<String> coche = new JComboBox<>(new String[] { "No", "Sí" });
        private final JLabel estadoFicha = new JLabel("Nueva ficha");

        GestionPersonal() { repositorio.inicializar(); }
        void setPestanas(JTabbedPane pestanas) { this.pestanas = pestanas; }

        JComponent crearListado() {
            JPanel contenido = new JPanel(new BorderLayout(0, 16));
            contenido.setBackground(Color.WHITE);
            contenido.setBorder(new EmptyBorder(25, 32, 25, 32));
            JPanel cabecera = new JPanel(new BorderLayout());
            cabecera.setOpaque(false);
            JPanel textos = new JPanel(); textos.setOpaque(false); textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            JLabel titulo = new JLabel("Base de datos de personal");
            titulo.setFont(new Font("SansSerif", Font.BOLD, 28)); titulo.setForeground(ROSA_OSCURO);
            JLabel ayuda = new JLabel("Marca las casillas de Informe para exportar una persona o un grupo. Haz doble clic sobre una fila para modificar la ficha.");
            ayuda.setForeground(new Color(85, 85, 90)); ayuda.setFont(new Font("SansSerif", Font.PLAIN, 14));
            textos.add(titulo); textos.add(Box.createVerticalStrut(6)); textos.add(ayuda);
            cabecera.add(textos, BorderLayout.WEST);
            JButton importar = botonSecundario("Importar Excel"); importar.addActionListener(e -> importarExcel());
            JButton alta = botonRosa("Nueva persona");
            alta.addActionListener(e -> { limpiarFicha(); pestanas.setSelectedIndex(1); });
            JPanel cabeceraAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); cabeceraAcciones.setOpaque(false); cabeceraAcciones.add(importar); cabeceraAcciones.add(alta);
            cabecera.add(cabeceraAcciones, BorderLayout.EAST);
            contenido.add(cabecera, BorderLayout.NORTH);

            tabla = new JTable(modelo);
            tabla.setAutoCreateRowSorter(true);
            tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tabla.setRowHeight(29);
            tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
            tabla.getTableHeader().setBackground(GRAFITO); tabla.getTableHeader().setForeground(Color.WHITE);
            tabla.getColumnModel().getColumn(0).setPreferredWidth(62); tabla.getColumnModel().getColumn(0).setMaxWidth(72);
            tabla.getColumnModel().getColumn(1).setMinWidth(0); tabla.getColumnModel().getColumn(1).setMaxWidth(0); tabla.getColumnModel().getColumn(1).setWidth(0);
            tabla.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) abrirSeleccionada(); }
            });
            contenido.add(new JScrollPane(tabla), BorderLayout.CENTER);

            JPanel inferior = new JPanel(new BorderLayout()); inferior.setOpaque(false);
            JTextField buscar = new JTextField(); buscar.setPreferredSize(new Dimension(260, 34));
            buscar.setToolTipText("Busca por nombre, apellidos, teléfono o correo");
            buscar.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { cargarListado(buscar.getText()); }
                public void removeUpdate(DocumentEvent e) { cargarListado(buscar.getText()); }
                public void changedUpdate(DocumentEvent e) { cargarListado(buscar.getText()); }
            });
            JPanel buscador = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); buscador.setOpaque(false);
            buscador.add(new JLabel("Buscar: ")); buscador.add(buscar);
            inferior.add(buscador, BorderLayout.WEST);
            JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); acciones.setOpaque(false);
            JButton abrir = botonSecundario("Abrir / modificar"); abrir.addActionListener(e -> abrirSeleccionada());
            JButton borrar = botonSecundario("Eliminar"); borrar.addActionListener(e -> eliminarSeleccionada());
            JButton xls = botonSecundario("Exportar XLS"); xls.addActionListener(e -> exportar("xls"));
            JButton pdf = botonRosa("Exportar PDF"); pdf.addActionListener(e -> exportar("pdf"));
            acciones.add(abrir); acciones.add(borrar); acciones.add(xls); acciones.add(pdf); inferior.add(acciones, BorderLayout.EAST);
            contenido.add(inferior, BorderLayout.SOUTH);
            cargarListado("");
            return contenido;
        }

        JComponent crearFicha() {
            JPanel contenido = new JPanel(new BorderLayout(0, 16));
            contenido.setBackground(Color.WHITE); contenido.setBorder(new EmptyBorder(25, 32, 25, 32));
            JPanel cabecera = new JPanel(); cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS)); cabecera.setOpaque(false);
            JLabel titulo = new JLabel("Ficha de personal"); titulo.setFont(new Font("SansSerif", Font.BOLD, 28)); titulo.setForeground(ROSA_OSCURO);
            estadoFicha.setFont(new Font("SansSerif", Font.BOLD, 14)); estadoFicha.setForeground(new Color(95, 95, 100));
            cabecera.add(titulo); cabecera.add(Box.createVerticalStrut(6)); cabecera.add(estadoFicha); contenido.add(cabecera, BorderLayout.NORTH);
            JPanel formulario = new JPanel(new GridBagLayout()); formulario.setBackground(Color.WHITE);
            GridBagConstraints c = nuevasRestricciones();
            agregarSeccion(formulario, "Datos personales", 0, c);
            agregarCampo(formulario, "Nombre completo *", nombreCompleto, 1, 0, c); agregarCampo(formulario, "DNI / NIE", dni, 1, 1, c);
            agregarCampo(formulario, "Fecha de nacimiento", fechaNacimiento, 2, 0, c);
            agregarSeccion(formulario, "Dirección", 4, c);
            agregarCampo(formulario, "Tipo de vía", tipoVia, 5, 0, c); agregarCampo(formulario, "Calle, avenida o plaza", via, 5, 1, c);
            agregarCampo(formulario, "Número", numero, 6, 0, c); agregarCampo(formulario, "Piso / puerta", piso, 6, 1, c);
            agregarCampo(formulario, "Código postal", codigoPostal, 7, 0, c); agregarCampo(formulario, "Ciudad", ciudad, 7, 1, c);
            agregarCampo(formulario, "Provincia", provincia, 8, 0, c); agregarCampo(formulario, "País", pais, 8, 1, c);
            agregarSeccion(formulario, "Contacto y datos administrativos", 9, c);
            agregarCampo(formulario, "Teléfono", telefono, 10, 0, c); agregarCampo(formulario, "N.º Seguridad Social", seguridadSocial, 10, 1, c);
            agregarCampo(formulario, "Cuenta bancaria / IBAN", iban, 11, 0, c); agregarCampo(formulario, "Coche propio", coche, 11, 1, c);
            agregarSeccion(formulario, "Perfil profesional", 12, c);
            agregarCampo(formulario, "Altura (cm)", altura, 13, 0, c); agregarCampo(formulario, "Nivel de idiomas certificado", idiomas, 13, 1, c);
            agregarCampo(formulario, "Talla pantalón / falda", tallaPantalon, 14, 0, c); agregarCampo(formulario, "Talla camisa", tallaCamisa, 14, 1, c);
            agregarCampo(formulario, "Talla camiseta", tallaCamiseta, 15, 0, c); agregarCampo(formulario, "Talla calzado", tallaCalzado, 15, 1, c);
            agregarExperiencia(formulario, 16, c);
            agregarCampo(formulario, "Observaciones", new JScrollPane(observaciones), 17, 0, c);
            JScrollPane scroll = new JScrollPane(formulario); scroll.setBorder(BorderFactory.createEmptyBorder()); contenido.add(scroll, BorderLayout.CENTER);
            JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); acciones.setOpaque(false);
            JButton nueva = botonSecundario("Nueva ficha"); nueva.addActionListener(e -> limpiarFicha());
            JButton guardar = botonRosa("Guardar ficha"); guardar.addActionListener(e -> guardarFicha());
            acciones.add(nueva); acciones.add(guardar); contenido.add(acciones, BorderLayout.SOUTH);
            return contenido;
        }

        JComponent crearHistorial() {
            JPanel contenido = new JPanel(new BorderLayout(0, 18)); contenido.setBackground(Color.WHITE); contenido.setBorder(new EmptyBorder(25, 32, 25, 32));
            JLabel titulo = new JLabel("Historial de participación en eventos"); titulo.setFont(new Font("SansSerif", Font.BOLD, 27)); titulo.setForeground(ROSA_OSCURO);
            contenido.add(titulo, BorderLayout.NORTH);
            modeloHistorial = new DefaultTableModel(new String[] { "Inicio", "Fin", "Evento", "N.º presupuesto" }, 0) {
                @Override public boolean isCellEditable(int fila, int columna) { return false; }
            };
            JTable tablaHistorial = new JTable(modeloHistorial); tablaHistorial.setRowHeight(30);
            contenido.add(new JScrollPane(tablaHistorial), BorderLayout.CENTER);
            JLabel aviso = new JLabel("El historial se completará al crear los eventos desde Producción y asignarles personal.");
            aviso.setForeground(new Color(90, 90, 96)); contenido.add(aviso, BorderLayout.SOUTH);
            return contenido;
        }

        private void cargarHistorial() {
            if (modeloHistorial == null) return;
            modeloHistorial.setRowCount(0);
            if (idActual < 0) return;
            for (HistorialEvento h : new RepositorioEventos().historialDe(idActual)) modeloHistorial.addRow(new Object[] { h.fechaInicio, h.fechaFin, h.evento, h.presupuesto });
        }

        private void agregarExperiencia(JPanel panel, int fila, GridBagConstraints base) {
            GridBagConstraints c = (GridBagConstraints) base.clone(); c.gridx = 0; c.gridy = fila; c.gridwidth = 2;
            JPanel bloque = new JPanel(new BorderLayout(0, 6)); bloque.setOpaque(false);
            JLabel etiqueta = new JLabel("Experiencia acreditada"); etiqueta.setFont(new Font("SansSerif", Font.BOLD, 13)); etiqueta.setForeground(new Color(65, 65, 70));
            JPanel casillas = new JPanel(new GridLayout(2, 3, 12, 5)); casillas.setOpaque(false);
            casillas.add(experienciaEventos); casillas.add(experienciaImagen); casillas.add(experienciaCongresos); casillas.add(experienciaPromocion); casillas.add(experienciaFerias); casillas.add(experienciaCarga);
            bloque.add(etiqueta, BorderLayout.NORTH); bloque.add(casillas, BorderLayout.CENTER); panel.add(bloque, c);
        }

        private String experienciaSeleccionada() {
            List<String> codigos = new ArrayList<>();
            if (experienciaEventos.isSelected()) codigos.add("E"); if (experienciaImagen.isSelected()) codigos.add("I"); if (experienciaCongresos.isSelected()) codigos.add("C");
            if (experienciaPromocion.isSelected()) codigos.add("P"); if (experienciaFerias.isSelected()) codigos.add("F"); if (experienciaCarga.isSelected()) codigos.add("C y DC");
            return String.join(", ", codigos);
        }

        private void cargarExperiencia(String texto) {
            String valor = texto == null ? "" : texto;
            experienciaEventos.setSelected(valor.equals("E") || valor.startsWith("E,") || valor.contains(", E,") || valor.endsWith(", E"));
            experienciaImagen.setSelected(valor.equals("I") || valor.startsWith("I,") || valor.contains(", I,") || valor.endsWith(", I"));
            experienciaCongresos.setSelected(valor.equals("C") || valor.startsWith("C,") || valor.contains(", C,") || valor.endsWith(", C"));
            experienciaPromocion.setSelected(valor.contains("P")); experienciaFerias.setSelected(valor.contains("F")); experienciaCarga.setSelected(valor.contains("C y DC"));
        }

        private void cargarListado(String filtro) {
            if (tabla == null) return;
            modelo.setRowCount(0);
            for (Persona persona : repositorio.listar(filtro)) {
                modelo.addRow(new Object[] { false, persona.id, nombreCompleto(persona), persona.telefono, persona.email, persona.coche });
            }
        }

        private void abrirSeleccionada() {
            if (tabla.getSelectedRow() < 0) { JOptionPane.showMessageDialog(tabla, "Selecciona primero una persona de la tabla."); return; }
            int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
            Persona persona = repositorio.buscar((Integer) modelo.getValueAt(fila, 1));
            if (persona == null) return;
            idActual = persona.id; nombreCompleto.setText(nombreCompleto(persona));
            dni.setText(persona.dni); cargarDireccion(persona.direccion); telefono.setText(persona.telefono); email.setText(persona.email);
            seguridadSocial.setText(persona.seguridadSocial); iban.setText(persona.iban); coche.setSelectedItem(persona.coche); fechaNacimiento.setText(persona.fechaNacimiento); altura.setText(persona.altura); tallaPantalon.setText(persona.tallaPantalon); tallaCamisa.setText(persona.tallaCamisa); tallaCamiseta.setText(persona.tallaCamiseta); tallaCalzado.setText(persona.tallaCalzado); idiomas.setText(persona.idiomas); observaciones.setText(persona.observaciones); cargarExperiencia(persona.experiencia);
            estadoFicha.setText("Editando ficha n.º " + persona.id + " · " + nombreCompleto(persona));
            cargarHistorial();
            pestanas.setSelectedIndex(1);
        }

        private void eliminarSeleccionada() {
            if (tabla.getSelectedRow() < 0) { JOptionPane.showMessageDialog(tabla, "Selecciona primero una persona de la tabla."); return; }
            int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
            int id = (Integer) modelo.getValueAt(fila, 1);
            String persona = (String) modelo.getValueAt(fila, 2);
            if (JOptionPane.showConfirmDialog(tabla, "¿Eliminar la ficha de " + persona + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                repositorio.eliminar(id); cargarListado(""); limpiarFicha();
            }
        }

        private void guardarFicha() {
            if (nombreCompleto.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(nombreCompleto, "Indica el nombre completo.", "Datos pendientes", JOptionPane.WARNING_MESSAGE); return;
            }
            Persona persona = new Persona(idActual, nombreCompleto.getText(), "", "", dni.getText(), direccionCompleta(), telefono.getText(), email.getText(), seguridadSocial.getText(), iban.getText(), (String) coche.getSelectedItem(), fechaNacimiento.getText(), altura.getText(), tallaPantalon.getText(), tallaCamisa.getText(), tallaCamiseta.getText(), tallaCalzado.getText(), idiomas.getText(), experienciaSeleccionada(), observaciones.getText());
            int idGuardado = repositorio.guardar(persona);
            if (idGuardado < 0) { JOptionPane.showMessageDialog(nombreCompleto, "No se ha podido guardar la ficha. Comprueba que las bibliotecas de la carpeta lib están presentes.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            idActual = idGuardado; estadoFicha.setText("Ficha guardada n.º " + idActual + " · " + nombreCompleto.getText().trim());
            cargarListado(""); JOptionPane.showMessageDialog(nombreCompleto, "Ficha guardada en la base de datos local.", "PonPox", JOptionPane.INFORMATION_MESSAGE);
        }

        private void limpiarFicha() {
            idActual = -1; nombreCompleto.setText(""); dni.setText(""); fechaNacimiento.setText(""); tipoVia.setSelectedIndex(0); via.setText(""); numero.setText(""); piso.setText(""); codigoPostal.setText(""); ciudad.setText(""); provincia.setText(""); pais.setText(""); telefono.setText(""); email.setText(""); seguridadSocial.setText(""); iban.setText(""); altura.setText(""); tallaPantalon.setText(""); tallaCamisa.setText(""); tallaCamiseta.setText(""); tallaCalzado.setText(""); idiomas.setText(""); observaciones.setText(""); experienciaEventos.setSelected(false); experienciaImagen.setSelected(false); experienciaCongresos.setSelected(false); experienciaPromocion.setSelected(false); experienciaFerias.setSelected(false); experienciaCarga.setSelected(false); coche.setSelectedIndex(0);
            estadoFicha.setText("Nueva ficha"); nombreCompleto.requestFocusInWindow();
        }

        private String direccionCompleta() {
            return (String) tipoVia.getSelectedItem() + "|" + via.getText().trim() + "|" + numero.getText().trim() + "|" + piso.getText().trim() + "|" + codigoPostal.getText().trim() + "|" + ciudad.getText().trim() + "|" + provincia.getText().trim() + "|" + pais.getText().trim();
        }

        private void cargarDireccion(String texto) {
            String[] partes = texto == null ? new String[0] : texto.split("\\|", -1);
            if (partes.length == 8) {
                tipoVia.setSelectedItem(partes[0]); via.setText(partes[1]); numero.setText(partes[2]); piso.setText(partes[3]); codigoPostal.setText(partes[4]); ciudad.setText(partes[5]); provincia.setText(partes[6]); pais.setText(partes[7]);
            } else { via.setText(texto == null ? "" : texto); }
        }

        private void importarExcel() {
            JFileChooser selector = new JFileChooser();
            selector.setDialogTitle("Selecciona el cuadro comercial de PonPox");
            if (selector.showOpenDialog(tabla) != JFileChooser.APPROVE_OPTION) return;
            try {
                List<RegistroExcel> registros = LectorExcelPonPox.leer(selector.getSelectedFile());
                if (registros.isEmpty()) { JOptionPane.showMessageDialog(tabla, "No se han encontrado personas en la hoja 'AZAF+IBAN@6 NOV'."); return; }
                List<Persona> existentes = repositorio.listar(""); int nuevos = 0, actualizados = 0, conflictos = 0;
                for (RegistroExcel registro : registros) {
                    Persona ficha = buscarPorNombre(existentes, registro.nombreCompleto);
                    if (ficha == null) {
                        Persona nueva = new Persona(-1, registro.nombreCompleto, "", "", "", direccionInicial(registro.ciudad), "", "", "", registro.iban, "No", "", "", "", "", "", "", "", "", registro.observaciones);
                        int id = repositorio.guardar(nueva); if (id >= 0) { nuevos++; existentes.add(repositorio.buscar(id)); }
                        continue;
                    }
                    List<String> diferencias = diferencias(ficha, registro);
                    boolean usarExcel = false;
                    if (!diferencias.isEmpty()) {
                        conflictos++;
                        int opcion = JOptionPane.showOptionDialog(tabla, "La ficha de " + nombreCompleto(ficha) + " contiene valores distintos:\n\n" + String.join("\n", diferencias) + "\n\nElige qué dato conservar. Puedes detener la importación para abrir y corregir la ficha manualmente.", "Divergencia de datos", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[] { "Conservar ficha", "Usar datos Excel", "Detener importación" }, "Conservar ficha");
                        if (opcion == 2 || opcion == JOptionPane.CLOSED_OPTION) break;
                        usarExcel = opcion == 1;
                    }
                    Persona fusionada = fusionar(ficha, registro, usarExcel);
                    if (!mismaFicha(ficha, fusionada)) { repositorio.guardar(fusionada); actualizados++; }
                }
                cargarListado("");
                JOptionPane.showMessageDialog(tabla, "Importación terminada.\nNuevas fichas: " + nuevos + "\nFichas completadas o actualizadas: " + actualizados + "\nDivergencias revisadas: " + conflictos, "PonPox", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception error) {
                JOptionPane.showMessageDialog(tabla, "No se ha podido importar el Excel:\n" + error.getMessage(), "Error de importación", JOptionPane.ERROR_MESSAGE);
            }
        }

        private Persona buscarPorNombre(List<Persona> personas, String nombre) {
            for (Persona persona : personas) if (normalizar(nombreCompleto(persona)).equals(normalizar(nombre))) return persona;
            return null;
        }

        private List<String> diferencias(Persona ficha, RegistroExcel excel) {
            List<String> resultado = new ArrayList<>();
            if (distinto(ficha.iban, excel.iban)) resultado.add("IBAN · Ficha: " + ficha.iban + " | Excel: " + excel.iban);
            if (distinto(ciudadDeDireccion(ficha.direccion), excel.ciudad)) resultado.add("Ciudad · Ficha: " + ciudadDeDireccion(ficha.direccion) + " | Excel: " + excel.ciudad);
            if (distinto(ficha.observaciones, excel.observaciones)) resultado.add("Observaciones · Ficha: " + ficha.observaciones + " | Excel: " + excel.observaciones);
            return resultado;
        }

        private Persona fusionar(Persona ficha, RegistroExcel excel, boolean usarExcel) {
            String ibanFinal = elegir(ficha.iban, excel.iban, usarExcel);
            String ciudadFinal = elegir(ciudadDeDireccion(ficha.direccion), excel.ciudad, usarExcel);
            String observacionesFinal = elegir(ficha.observaciones, excel.observaciones, usarExcel);
            return new Persona(ficha.id, ficha.nombre, ficha.apellido1, ficha.apellido2, ficha.dni, direccionConCiudad(ficha.direccion, ciudadFinal), ficha.telefono, ficha.email, ficha.seguridadSocial, ibanFinal, ficha.coche, ficha.fechaNacimiento, ficha.altura, ficha.tallaPantalon, ficha.tallaCamisa, ficha.tallaCamiseta, ficha.tallaCalzado, ficha.idiomas, ficha.experiencia, observacionesFinal);
        }

        private boolean mismaFicha(Persona a, Persona b) { return a.iban.equals(b.iban) && a.direccion.equals(b.direccion) && a.observaciones.equals(b.observaciones); }
        private String elegir(String ficha, String excel, boolean usarExcel) { if (vacio(excel)) return ficha; return vacio(ficha) || usarExcel ? excel : ficha; }
        private boolean distinto(String ficha, String excel) { return !vacio(ficha) && !vacio(excel) && !normalizar(ficha).equals(normalizar(excel)); }
        private boolean vacio(String texto) { return texto == null || texto.trim().isEmpty(); }
        private String normalizar(String texto) { return (texto == null ? "" : texto).trim().toUpperCase().replaceAll("\\s+", " "); }
        private String ciudadDeDireccion(String direccion) { String[] p = direccion == null ? new String[0] : direccion.split("\\|", -1); return p.length == 8 ? p[5] : ""; }
        private String direccionInicial(String ciudadImportada) { return "Calle|||||" + ciudadImportada.trim() + "||"; }
        private String direccionConCiudad(String direccion, String ciudadImportada) {
            String[] p = direccion == null ? new String[0] : direccion.split("\\|", -1);
            if (p.length != 8) p = new String[] { "Calle", "", "", "", "", "", "", "" };
            p[5] = ciudadImportada == null ? "" : ciudadImportada.trim(); return String.join("|", p);
        }

        private void exportar(String formato) {
            Map<String, JCheckBox> campos = new LinkedHashMap<>();
            campos.put("Nombre completo", new JCheckBox("Nombre completo", true));
            campos.put("Dirección", new JCheckBox("Dirección", false));
            campos.put("Teléfono", new JCheckBox("Teléfono", true));
            campos.put("E-mail", new JCheckBox("E-mail", true));
            campos.put("DNI / NIE", new JCheckBox("DNI / NIE", false));
            campos.put("Fecha de nacimiento", new JCheckBox("Fecha de nacimiento", false));
            campos.put("Coche propio", new JCheckBox("Coche propio", true));
            campos.put("Altura", new JCheckBox("Altura", true));
            campos.put("Tallas", new JCheckBox("Tallas", true));
            campos.put("Idiomas certificados", new JCheckBox("Idiomas certificados", true));
            campos.put("Experiencia", new JCheckBox("Experiencia", true));
            campos.put("Observaciones", new JCheckBox("Observaciones", true));
            campos.put("N.º Seguridad Social", new JCheckBox("N.º Seguridad Social", false));
            campos.put("Cuenta bancaria / IBAN", new JCheckBox("Cuenta bancaria / IBAN", false));
            JPanel seleccion = new JPanel(new GridLayout(0, 2, 12, 8));
            seleccion.setBorder(new EmptyBorder(8, 8, 8, 8));
            for (JCheckBox casilla : campos.values()) seleccion.add(casilla);
            int opcion = JOptionPane.showConfirmDialog(tabla, seleccion,
                "Elige los campos para el informe " + formato.toUpperCase(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcion != JOptionPane.OK_OPTION) return;
            List<String> elegidos = new ArrayList<>();
            for (Map.Entry<String, JCheckBox> entrada : campos.entrySet()) if (entrada.getValue().isSelected()) elegidos.add(entrada.getKey());
            if (elegidos.isEmpty()) { JOptionPane.showMessageDialog(tabla, "Selecciona al menos un campo."); return; }
            JFileChooser selector = new JFileChooser();
            selector.setDialogTitle("Guardar informe de personal");
            selector.setSelectedFile(new File("Informe_personal_" + LocalDate.now() + "." + formato));
            if (selector.showSaveDialog(tabla) != JFileChooser.APPROVE_OPTION) return;
            File destino = selector.getSelectedFile();
            if (!destino.getName().toLowerCase().endsWith("." + formato)) destino = new File(destino.getAbsolutePath() + "." + formato);
            try {
                List<Persona> personas = personasMarcadasParaInforme();
                if (personas.isEmpty()) { JOptionPane.showMessageDialog(tabla, "Marca en la columna Informe la persona o personas que deseas incluir."); return; }
                if (formato.equals("xls")) generarXls(destino, elegidos, personas); else generarPdf(destino, elegidos, personas);
                JOptionPane.showMessageDialog(tabla, "Informe creado correctamente:\n" + destino.getAbsolutePath(), "PonPox", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception error) {
                JOptionPane.showMessageDialog(tabla, "No se ha podido crear el informe:\n" + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private List<Persona> personasMarcadasParaInforme() {
            List<Persona> personas = new ArrayList<>();
            for (int fila = 0; fila < modelo.getRowCount(); fila++) {
                if (Boolean.TRUE.equals(modelo.getValueAt(fila, 0))) {
                    Persona persona = repositorio.buscar((Integer) modelo.getValueAt(fila, 1));
                    if (persona != null) personas.add(persona);
                }
            }
            return personas;
        }

        private void generarXls(File archivo, List<String> campos, List<Persona> personas) throws IOException {
            try (BufferedWriter salida = new BufferedWriter(new FileWriter(archivo))) {
                salida.write("<?xml version=\"1.0\"?><Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"><Worksheet ss:Name=\"Personal\"><Table>");
                salida.write("<Row><Cell ss:MergeAcross=\"1\"><Data ss:Type=\"String\">PONPOX · Informe de personal</Data></Cell></Row>");
                salida.write("<Row><Cell ss:MergeAcross=\"1\"><Data ss:Type=\"String\">Fecha de emisión: " + LocalDate.now() + "</Data></Cell></Row>");
                for (Persona persona : personas) {
                    salida.write("<Row></Row><Row><Cell ss:MergeAcross=\"1\"><Data ss:Type=\"String\">FICHA · " + escaparXml(nombreCompleto(persona)) + "</Data></Cell></Row>");
                    for (String campo : campos) salida.write("<Row>" + celdaXls(campo) + celdaXls(valor(persona, campo)) + "</Row>");
                }
                salida.write("</Table></Worksheet></Workbook>");
            }
        }

        private String celdaXls(String valor) { return "<Cell><Data ss:Type=\"String\">" + escaparXml(valor) + "</Data></Cell>"; }
        private String escaparXml(String texto) { return texto == null ? "" : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }

        private void generarPdf(File archivo, List<String> campos, List<Persona> personas) throws Exception {
            com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 42, 42, 38, 38);
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new FileOutputStream(archivo));
            documento.open();
            com.itextpdf.text.Font titulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font normal = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            com.itextpdf.text.Font etiqueta = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(80, 80, 84));
            com.itextpdf.text.Font nombreFicha = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 15, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(224, 45, 133));
            for (int indice = 0; indice < personas.size(); indice++) {
                if (indice > 0) documento.newPage();
                Persona persona = personas.get(indice);
                agregarCabeceraPdf(documento, titulo, normal, personas.size());
                documento.add(new com.itextpdf.text.Paragraph(" "));
                documento.add(new com.itextpdf.text.Paragraph(nombreCompleto(persona), nombreFicha));
                documento.add(new com.itextpdf.text.Paragraph("Ficha individual de personal", normal));
                documento.add(new com.itextpdf.text.Paragraph(" "));
                com.itextpdf.text.pdf.PdfPTable tablaPdf = new com.itextpdf.text.pdf.PdfPTable(new float[] { 2.2f, 4.8f });
                tablaPdf.setWidthPercentage(100); tablaPdf.setKeepTogether(true);
                for (String campo : campos) {
                    com.itextpdf.text.pdf.PdfPCell celdaCampo = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(campo, etiqueta));
                    celdaCampo.setBackgroundColor(new com.itextpdf.text.BaseColor(248, 235, 243)); celdaCampo.setPadding(7);
                    com.itextpdf.text.pdf.PdfPCell celdaValor = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(valor(persona, campo), normal));
                    celdaValor.setPadding(7); tablaPdf.addCell(celdaCampo); tablaPdf.addCell(celdaValor);
                }
                documento.add(tablaPdf);
            }
            documento.close();
        }

        private void agregarCabeceraPdf(com.itextpdf.text.Document documento, com.itextpdf.text.Font titulo, com.itextpdf.text.Font normal, int totalPersonas) throws Exception {
            File logo = new File("Logotipo PonPox.png"); if (!logo.exists()) logo = new File("../Logotipo PonPox.png");
            if (logo.exists()) { com.itextpdf.text.Image imagen = com.itextpdf.text.Image.getInstance(logo.getAbsolutePath()); imagen.scaleToFit(180, 60); documento.add(imagen); }
            documento.add(new com.itextpdf.text.Paragraph("PonPox · Informe de personal", titulo));
            documento.add(new com.itextpdf.text.Paragraph("Fecha de emisión: " + LocalDate.now() + " · Personas incluidas: " + totalPersonas, normal));
        }

        private String nombreCompleto(Persona persona) { return (persona.nombre + " " + persona.apellido1 + " " + persona.apellido2).trim(); }

        private String valor(Persona p, String campo) {
            if (campo.equals("Nombre completo")) return nombreCompleto(p);
            if (campo.equals("Dirección")) return direccionParaInforme(p.direccion); if (campo.equals("Teléfono")) return p.telefono; if (campo.equals("E-mail")) return p.email;
            if (campo.equals("DNI / NIE")) return p.dni; if (campo.equals("Coche propio")) return p.coche;
            if (campo.equals("Fecha de nacimiento")) return p.fechaNacimiento; if (campo.equals("Altura")) return p.altura;
            if (campo.equals("Tallas")) return "Pantalón/falda: " + p.tallaPantalon + " · Camisa: " + p.tallaCamisa + " · Camiseta: " + p.tallaCamiseta + " · Calzado: " + p.tallaCalzado;
            if (campo.equals("Idiomas certificados")) return p.idiomas; if (campo.equals("Experiencia")) return p.experiencia;
            if (campo.equals("Observaciones")) return p.observaciones;
            if (campo.equals("N.º Seguridad Social")) return p.seguridadSocial; return p.iban;
        }

        private String direccionParaInforme(String texto) {
            if (texto == null) return "";
            String[] p = texto.split("\\|", -1);
            if (p.length != 8) return texto;
            String primera = (p[0] + " " + p[1] + (p[2].isEmpty() ? "" : ", " + p[2]) + (p[3].isEmpty() ? "" : " · " + p[3])).trim();
            String segunda = (p[4].isEmpty() ? "" : p[4] + " ") + p[5] + (p[6].isEmpty() ? "" : " · " + p[6]) + (p[7].isEmpty() ? "" : " · " + p[7]);
            return (primera + " — " + segunda).trim();
        }
    }

    private static class RegistroExcel {
        final String nombreCompleto, iban, ciudad, observaciones;
        RegistroExcel(String nombreCompleto, String iban, String ciudad, String observaciones) {
            this.nombreCompleto = limpiar(nombreCompleto); this.iban = limpiar(iban); this.ciudad = limpiar(ciudad); this.observaciones = limpiar(observaciones);
        }
        private static String limpiar(String texto) { return texto == null ? "" : texto.trim(); }
    }

    /** Lee el formato XLSX de la hoja de personal sin depender de programas externos. */
    private static class LectorExcelPonPox {
        static List<RegistroExcel> leer(File archivo) throws Exception {
            try (ZipFile zip = new ZipFile(archivo)) {
                List<String> compartidas = leerCompartidas(zip);
                String hoja = localizarHoja(zip, " AZAF+IBAN@6 NOV");
                if (hoja == null) hoja = localizarHoja(zip, "AZAF+IBAN@6 NOV");
                if (hoja == null) throw new IOException("No existe la hoja 'AZAF+IBAN@6 NOV'.");
                Element raiz = leerXml(zip, hoja).getDocumentElement();
                NodeList filas = raiz.getElementsByTagNameNS("*", "row");
                Map<String, RegistroExcel> unicos = new LinkedHashMap<>();
                for (int i = 0; i < filas.getLength(); i++) {
                    Element fila = (Element) filas.item(i); Map<Integer, String> celdas = new LinkedHashMap<>();
                    NodeList celdasXml = fila.getElementsByTagNameNS("*", "c");
                    for (int j = 0; j < celdasXml.getLength(); j++) {
                        Element celda = (Element) celdasXml.item(j); celdas.put(columnaDe(celda.getAttribute("r")), leerCelda(celda, compartidas));
                    }
                    String nombre = celdas.get(2); // Columna B: AZAFATA-O
                    if (nombre == null || nombre.trim().isEmpty() || nombre.trim().equalsIgnoreCase("AZAFATA-O")) continue;
                    RegistroExcel registro = new RegistroExcel(nombre, celdas.get(3), celdas.get(4), celdas.get(5));
                    unicos.put(normalizarClave(registro.nombreCompleto), registro);
                }
                return new ArrayList<>(unicos.values());
            }
        }

        private static List<String> leerCompartidas(ZipFile zip) throws Exception {
            ZipEntry entrada = zip.getEntry("xl/sharedStrings.xml"); if (entrada == null) return Collections.emptyList();
            NodeList cadenas = leerXml(zip, "xl/sharedStrings.xml").getDocumentElement().getElementsByTagNameNS("*", "si");
            List<String> resultado = new ArrayList<>();
            for (int i = 0; i < cadenas.getLength(); i++) resultado.add(cadenas.item(i).getTextContent());
            return resultado;
        }

        private static String localizarHoja(ZipFile zip, String nombreBuscado) throws Exception {
            org.w3c.dom.Document libro = leerXml(zip, "xl/workbook.xml");
            String id = null; NodeList hojas = libro.getDocumentElement().getElementsByTagNameNS("*", "sheet");
            for (int i = 0; i < hojas.getLength(); i++) {
                Element hoja = (Element) hojas.item(i);
                if (nombreBuscado.equals(hoja.getAttribute("name"))) { id = atributoId(hoja); break; }
            }
            if (id == null) return null;
            org.w3c.dom.Document relaciones = leerXml(zip, "xl/_rels/workbook.xml.rels");
            NodeList lista = relaciones.getDocumentElement().getElementsByTagNameNS("*", "Relationship");
            for (int i = 0; i < lista.getLength(); i++) {
                Element relacion = (Element) lista.item(i);
                if (id.equals(relacion.getAttribute("Id"))) return "xl/" + relacion.getAttribute("Target");
            }
            return null;
        }

        private static String atributoId(Element elemento) {
            for (int i = 0; i < elemento.getAttributes().getLength(); i++) {
                Node atributo = elemento.getAttributes().item(i);
                if ("id".equals(atributo.getLocalName()) || atributo.getNodeName().endsWith(":id")) return atributo.getNodeValue();
            }
            return "";
        }

        private static org.w3c.dom.Document leerXml(ZipFile zip, String ruta) throws Exception {
            ZipEntry entrada = zip.getEntry(ruta); if (entrada == null) throw new IOException("Falta " + ruta + " en el libro Excel.");
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance(); fabrica.setNamespaceAware(true);
            try { fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); } catch (Exception ignored) { }
            try (InputStream entradaXml = zip.getInputStream(entrada)) { return fabrica.newDocumentBuilder().parse(entradaXml); }
        }

        private static String leerCelda(Element celda, List<String> compartidas) {
            String tipo = celda.getAttribute("t"); String valor = textoHijo(celda, "v");
            if ("s".equals(tipo)) try { return compartidas.get(Integer.parseInt(valor)); } catch (Exception ignored) { return ""; }
            if ("inlineStr".equals(tipo)) return textoHijo(celda, "t");
            return valor;
        }

        private static String textoHijo(Element elemento, String nombre) {
            NodeList nodos = elemento.getElementsByTagNameNS("*", nombre); return nodos.getLength() == 0 ? "" : nodos.item(0).getTextContent();
        }

        private static int columnaDe(String referencia) {
            int valor = 0; for (int i = 0; i < referencia.length() && Character.isLetter(referencia.charAt(i)); i++) valor = valor * 26 + Character.toUpperCase(referencia.charAt(i)) - 'A' + 1; return valor;
        }
        private static String normalizarClave(String texto) { return texto.trim().toUpperCase().replaceAll("\\s+", " "); }
    }

    private static class Persona {
        final int id; final String nombre, apellido1, apellido2, dni, direccion, telefono, email, seguridadSocial, iban, coche, fechaNacimiento, altura, tallaPantalon, tallaCamisa, tallaCamiseta, tallaCalzado, idiomas, experiencia, observaciones;
        Persona(int id, String nombre, String apellido1, String apellido2, String dni, String direccion, String telefono, String email, String seguridadSocial, String iban, String coche, String fechaNacimiento, String altura, String tallaPantalon, String tallaCamisa, String tallaCamiseta, String tallaCalzado, String idiomas, String experiencia, String observaciones) {
            this.id = id; this.nombre = nombre; this.apellido1 = apellido1; this.apellido2 = apellido2; this.dni = dni; this.direccion = direccion; this.telefono = telefono; this.email = email; this.seguridadSocial = seguridadSocial; this.iban = iban; this.coche = coche; this.fechaNacimiento = fechaNacimiento; this.altura = altura; this.tallaPantalon = tallaPantalon; this.tallaCamisa = tallaCamisa; this.tallaCamiseta = tallaCamiseta; this.tallaCalzado = tallaCalzado; this.idiomas = idiomas; this.experiencia = experiencia; this.observaciones = observaciones;
        }
    }

    private static class RepositorioPersonal {
        private static final String URL = "jdbc:sqlite:ponpox.db";
        void inicializar() {
            try (Connection c = DriverManager.getConnection(URL); Statement s = c.createStatement()) {
                s.executeUpdate("CREATE TABLE IF NOT EXISTS personal (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, apellido1 TEXT NOT NULL, apellido2 TEXT, dni TEXT, direccion TEXT, telefono TEXT, email TEXT, seguridad_social TEXT, iban TEXT, coche TEXT)");
                agregarColumna(s, "fecha_nacimiento"); agregarColumna(s, "altura"); agregarColumna(s, "talla_pantalon"); agregarColumna(s, "talla_camisa"); agregarColumna(s, "talla_camiseta"); agregarColumna(s, "talla_calzado"); agregarColumna(s, "idiomas"); agregarColumna(s, "experiencia"); agregarColumna(s, "observaciones");
                s.executeUpdate("CREATE TABLE IF NOT EXISTS eventos (id INTEGER PRIMARY KEY AUTOINCREMENT, presupuesto TEXT, evento TEXT, fecha_inicio TEXT, fecha_fin TEXT)");
                s.executeUpdate("CREATE TABLE IF NOT EXISTS historial_experiencia (id INTEGER PRIMARY KEY AUTOINCREMENT, personal_id INTEGER, evento_id INTEGER, fecha_inicio TEXT, fecha_fin TEXT, evento TEXT, presupuesto TEXT)");
            } catch (SQLException e) { mostrarError(e); }
        }
        private void agregarColumna(Statement s, String nombre) { try { s.executeUpdate("ALTER TABLE personal ADD COLUMN " + nombre + " TEXT"); } catch (SQLException ignored) { } }
        List<Persona> listar(String filtro) {
            List<Persona> personas = new ArrayList<>(); String like = "%" + filtro.trim() + "%";
            String sql = "SELECT * FROM personal WHERE nombre LIKE ? OR apellido1 LIKE ? OR apellido2 LIKE ? OR telefono LIKE ? OR email LIKE ? ORDER BY apellido1 COLLATE NOCASE, nombre COLLATE NOCASE";
            try (Connection c = DriverManager.getConnection(URL); PreparedStatement p = c.prepareStatement(sql)) {
                for (int i = 1; i <= 5; i++) p.setString(i, like);
                ResultSet r = p.executeQuery(); while (r.next()) personas.add(deResultado(r));
            } catch (SQLException e) { mostrarError(e); } return personas;
        }
        Persona buscar(int id) {
            try (Connection c = DriverManager.getConnection(URL); PreparedStatement p = c.prepareStatement("SELECT * FROM personal WHERE id = ?")) {
                p.setInt(1, id); ResultSet r = p.executeQuery(); return r.next() ? deResultado(r) : null;
            } catch (SQLException e) { mostrarError(e); return null; }
        }
        int guardar(Persona persona) {
            boolean nueva = persona.id < 0;
            String sql = nueva ? "INSERT INTO personal (nombre,apellido1,apellido2,dni,direccion,telefono,email,seguridad_social,iban,coche,fecha_nacimiento,altura,talla_pantalon,talla_camisa,talla_camiseta,talla_calzado,idiomas,experiencia,observaciones) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" : "UPDATE personal SET nombre=?,apellido1=?,apellido2=?,dni=?,direccion=?,telefono=?,email=?,seguridad_social=?,iban=?,coche=?,fecha_nacimiento=?,altura=?,talla_pantalon=?,talla_camisa=?,talla_camiseta=?,talla_calzado=?,idiomas=?,experiencia=?,observaciones=? WHERE id=?";
            try (Connection c = DriverManager.getConnection(URL); PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                asignar(p, persona); if (!nueva) p.setInt(20, persona.id); p.executeUpdate();
                if (!nueva) return persona.id; ResultSet claves = p.getGeneratedKeys(); return claves.next() ? claves.getInt(1) : -1;
            } catch (SQLException e) { mostrarError(e); return -1; }
        }
        void eliminar(int id) { try (Connection c = DriverManager.getConnection(URL); PreparedStatement p = c.prepareStatement("DELETE FROM personal WHERE id=?")) { p.setInt(1, id); p.executeUpdate(); } catch (SQLException e) { mostrarError(e); } }
        private void asignar(PreparedStatement p, Persona x) throws SQLException {
            p.setString(1, x.nombre.trim()); p.setString(2, x.apellido1.trim()); p.setString(3, x.apellido2.trim()); p.setString(4, x.dni.trim()); p.setString(5, x.direccion.trim()); p.setString(6, x.telefono.trim()); p.setString(7, x.email.trim()); p.setString(8, x.seguridadSocial.trim()); p.setString(9, x.iban.trim()); p.setString(10, x.coche); p.setString(11, x.fechaNacimiento.trim()); p.setString(12, x.altura.trim()); p.setString(13, x.tallaPantalon.trim()); p.setString(14, x.tallaCamisa.trim()); p.setString(15, x.tallaCamiseta.trim()); p.setString(16, x.tallaCalzado.trim()); p.setString(17, x.idiomas.trim()); p.setString(18, x.experiencia.trim()); p.setString(19, x.observaciones.trim());
        }
        private Persona deResultado(ResultSet r) throws SQLException { return new Persona(r.getInt("id"), r.getString("nombre"), r.getString("apellido1"), r.getString("apellido2"), r.getString("dni"), r.getString("direccion"), r.getString("telefono"), r.getString("email"), r.getString("seguridad_social"), r.getString("iban"), r.getString("coche"), r.getString("fecha_nacimiento"), r.getString("altura"), r.getString("talla_pantalon"), r.getString("talla_camisa"), r.getString("talla_camiseta"), r.getString("talla_calzado"), r.getString("idiomas"), r.getString("experiencia"), r.getString("observaciones")); }
        private void mostrarError(SQLException e) { System.err.println("Error de base de datos: " + e.getMessage()); }
    }

    private static class HistorialEvento {
        final String fechaInicio, fechaFin, evento, presupuesto;
        HistorialEvento(String fechaInicio, String fechaFin, String evento, String presupuesto) { this.fechaInicio = fechaInicio; this.fechaFin = fechaFin; this.evento = evento; this.presupuesto = presupuesto; }
    }

    private static class RepositorioEventos {
        private static final String URL = "jdbc:sqlite:ponpox.db";
        boolean guardar(String presupuesto, String evento, String inicio, String fin, List<Integer> personas) {
            String insertarEvento = "INSERT INTO eventos (presupuesto,evento,fecha_inicio,fecha_fin) VALUES (?,?,?,?)";
            String insertarHistorial = "INSERT INTO historial_experiencia (personal_id,evento_id,fecha_inicio,fecha_fin,evento,presupuesto) VALUES (?,?,?,?,?,?)";
            try (Connection c = DriverManager.getConnection(URL)) {
                c.setAutoCommit(false);
                try (PreparedStatement e = c.prepareStatement(insertarEvento, Statement.RETURN_GENERATED_KEYS)) {
                    e.setString(1, presupuesto.trim()); e.setString(2, evento.trim()); e.setString(3, inicio.trim()); e.setString(4, fin.trim()); e.executeUpdate();
                    ResultSet claves = e.getGeneratedKeys(); if (!claves.next()) { c.rollback(); return false; } int eventoId = claves.getInt(1);
                    try (PreparedStatement h = c.prepareStatement(insertarHistorial)) {
                        for (Integer personaId : personas) { h.setInt(1, personaId); h.setInt(2, eventoId); h.setString(3, inicio.trim()); h.setString(4, fin.trim()); h.setString(5, evento.trim()); h.setString(6, presupuesto.trim()); h.addBatch(); }
                        h.executeBatch();
                    }
                }
                c.commit(); return true;
            } catch (SQLException e) { System.err.println("Error de producción: " + e.getMessage()); return false; }
        }
        List<HistorialEvento> historialDe(int personaId) {
            List<HistorialEvento> datos = new ArrayList<>();
            try (Connection c = DriverManager.getConnection(URL); PreparedStatement p = c.prepareStatement("SELECT fecha_inicio,fecha_fin,evento,presupuesto FROM historial_experiencia WHERE personal_id=? ORDER BY id DESC")) {
                p.setInt(1, personaId); ResultSet r = p.executeQuery(); while (r.next()) datos.add(new HistorialEvento(r.getString(1), r.getString(2), r.getString(3), r.getString(4)));
            } catch (SQLException e) { System.err.println("Error de historial: " + e.getMessage()); } return datos;
        }
    }

    private static void abrirProduccion() {
        JFrame ventana = crearVentanaArea("Producción");
        ventana.setContentPane(new GestionProduccion().crearPestanas());
        ventana.setVisible(true);
    }

    private static class GestionProduccion {
        private final JTextField presupuesto = nuevoCampo();
        private final JTextField evento = nuevoCampo();
        private final JTextField fechaInicio = nuevoCampo();
        private final JTextField fechaFin = nuevoCampo();
        private final DefaultTableModel personal = new DefaultTableModel(new String[] { "ID", "Asignar", "Nombre completo", "Teléfono", "Experiencia" }, 0) {
            @Override public Class<?> getColumnClass(int col) {
                if (col == 0) return Integer.class;
                return col == 1 ? Boolean.class : String.class;
            }
            @Override public boolean isCellEditable(int fila, int col) { return col == 1; }
        };

        JComponent crearPestanas() {
            new RepositorioPersonal().inicializar();
            JTabbedPane pestanas = new JTabbedPane(); pestanas.setFont(new Font("SansSerif", Font.BOLD, 14));
            pestanas.addTab("N.º presupuesto", panelCampo("N.º de presupuesto", "Referencia interna del presupuesto", presupuesto));
            pestanas.addTab("Evento", panelCampo("Evento", "Nombre del evento o cliente", evento));
            pestanas.addTab("Fechas del servicio", panelFechas());
            pestanas.addTab("Personal", panelPersonal());
            return pestanas;
        }

        private JComponent panelCampo(String tituloTexto, String ayudaTexto, JTextField campo) {
            JPanel contenido = new JPanel(new BorderLayout(0, 22)); contenido.setBackground(Color.WHITE); contenido.setBorder(new EmptyBorder(30, 38, 30, 38));
            JLabel titulo = new JLabel(tituloTexto); titulo.setFont(new Font("SansSerif", Font.BOLD, 29)); titulo.setForeground(ROSA_OSCURO);
            JLabel ayuda = new JLabel(ayudaTexto); ayuda.setForeground(new Color(85, 85, 90));
            JPanel cabecera = new JPanel(); cabecera.setOpaque(false); cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS)); cabecera.add(titulo); cabecera.add(Box.createVerticalStrut(7)); cabecera.add(ayuda);
            contenido.add(cabecera, BorderLayout.NORTH); campo.setFont(new Font("SansSerif", Font.PLAIN, 16)); campo.setPreferredSize(new Dimension(500, 38)); contenido.add(campo, BorderLayout.CENTER); return contenido;
        }

        private JComponent panelFechas() {
            JPanel contenido = new JPanel(new GridBagLayout()); contenido.setBackground(Color.WHITE); contenido.setBorder(new EmptyBorder(30, 38, 30, 38)); GridBagConstraints c = nuevasRestricciones();
            agregarSeccion(contenido, "Fechas del servicio", 0, c); agregarCampo(contenido, "Inicio (dd/mm/aaaa)", fechaInicio, 1, 0, c); agregarCampo(contenido, "Fin (dd/mm/aaaa)", fechaFin, 1, 1, c); return contenido;
        }

        private JComponent panelPersonal() {
            JPanel contenido = new JPanel(new BorderLayout(0, 18)); contenido.setBackground(Color.WHITE); contenido.setBorder(new EmptyBorder(30, 38, 30, 38));
            JLabel titulo = new JLabel("Personal asignado al servicio"); titulo.setFont(new Font("SansSerif", Font.BOLD, 29)); titulo.setForeground(ROSA_OSCURO); contenido.add(titulo, BorderLayout.NORTH);
            cargarPersonal(); JTable tabla = new JTable(personal); tabla.setRowHeight(29); tabla.setAutoCreateRowSorter(true); tabla.getColumnModel().getColumn(0).setMinWidth(0); tabla.getColumnModel().getColumn(0).setMaxWidth(0);
            contenido.add(new JScrollPane(tabla), BorderLayout.CENTER);
            JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT)); pie.setOpaque(false); JButton guardar = botonRosa("Guardar producción y asignaciones"); guardar.addActionListener(e -> guardarProduccion()); pie.add(guardar); contenido.add(pie, BorderLayout.SOUTH);
            return contenido;
        }

        private void cargarPersonal() { personal.setRowCount(0); for (Persona p : new RepositorioPersonal().listar("")) personal.addRow(new Object[] { p.id, false, (p.nombre + " " + p.apellido1 + " " + p.apellido2).trim(), p.telefono, p.experiencia }); }

        private void guardarProduccion() {
            if (presupuesto.getText().trim().isEmpty() || evento.getText().trim().isEmpty() || fechaInicio.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(presupuesto, "Indica al menos el número de presupuesto, el evento y la fecha de inicio."); return; }
            List<Integer> asignados = new ArrayList<>(); for (int fila = 0; fila < personal.getRowCount(); fila++) if (Boolean.TRUE.equals(personal.getValueAt(fila, 1))) asignados.add((Integer) personal.getValueAt(fila, 0));
            if (asignados.isEmpty()) { JOptionPane.showMessageDialog(presupuesto, "Marca al menos una persona para el evento."); return; }
            if (new RepositorioEventos().guardar(presupuesto.getText(), evento.getText(), fechaInicio.getText(), fechaFin.getText(), asignados)) JOptionPane.showMessageDialog(presupuesto, "Producción guardada. El historial de cada persona ha sido actualizado."); else JOptionPane.showMessageDialog(presupuesto, "No se ha podido guardar la producción.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static GridBagConstraints nuevasRestricciones() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        c.insets = new Insets(6, 0, 6, 18);
        return c;
    }

    private static void agregarSeccion(JPanel panel, String texto, int fila, GridBagConstraints base) {
        GridBagConstraints c = (GridBagConstraints) base.clone();
        c.gridx = 0; c.gridy = fila; c.gridwidth = 2; c.insets = new Insets(fila == 0 ? 0 : 20, 0, 7, 0);
        JLabel etiqueta = new JLabel(texto.toUpperCase());
        etiqueta.setForeground(ROSA_OSCURO);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(etiqueta, c);
    }

    private static void agregarCampo(JPanel panel, String etiqueta, JComponent campo, int fila, int columna, GridBagConstraints base) {
        GridBagConstraints c = (GridBagConstraints) base.clone();
        c.gridx = columna; c.gridy = fila;
        JPanel bloque = new JPanel();
        bloque.setOpaque(false);
        bloque.setLayout(new BoxLayout(bloque, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel(etiqueta);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        titulo.setForeground(new Color(65, 65, 70));
        bloque.add(titulo); bloque.add(Box.createVerticalStrut(4)); bloque.add(campo);
        panel.add(bloque, c);
    }

    private static JTextField nuevoCampo() {
        JTextField campo = new JTextField();
        campo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        campo.setPreferredSize(new Dimension(260, 33));
        return campo;
    }

    private static JTextArea nuevaArea() {
        JTextArea area = new JTextArea(3, 20);
        area.setFont(new Font("SansSerif", Font.PLAIN, 14)); area.setLineWrap(true); area.setWrapStyleWord(true);
        return area;
    }

    private static JButton botonRosa(String texto) {
        JButton boton = new JButton(texto);
        boton.setForeground(Color.WHITE); boton.setBackground(ROSA); boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(9, 18, 9, 18));
        return boton;
    }

    private static JButton botonSecundario(String texto) {
        JButton boton = new JButton(texto);
        boton.setForeground(GRAFITO); boton.setBackground(new Color(235, 235, 240)); boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(9, 18, 9, 18));
        return boton;
    }

}
