import java.awt.*;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            zona.add(crearBoton("Trabajadores", "Altas, fichas, disponibilidad e historial", PonPox::abrirRecursosHumanos));
            zona.add(Box.createVerticalStrut(16));
            zona.add(crearBoton("Clientes", "Base de datos, contactos y condiciones", PonPox::abrirClientes));
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
            JLabel pie = new JLabel("PonPox · Gestión de trabajadores, clientes y operaciones", SwingConstants.CENTER);
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
        JFrame ventana = crearVentanaArea("Trabajadores | Personal");
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
            new String[] { "ID", "Nombre completo" }, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
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
            JLabel ayuda = new JLabel("Busca por nombre y pulsa sobre una persona para abrir su ficha completa.");
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
            tabla.getColumnModel().getColumn(0).setMinWidth(0); tabla.getColumnModel().getColumn(0).setMaxWidth(0); tabla.getColumnModel().getColumn(0).setWidth(0);
            tabla.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 1) abrirSeleccionada(); }
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
            JLabel aviso = new JLabel("Se actualiza automáticamente al guardar cada jornada producida. Selecciona una ficha para consultar su historial.");
            aviso.setForeground(new Color(90, 90, 96));
            JButton actualizar = botonSecundario("Actualizar historial"); actualizar.addActionListener(e -> cargarHistorial());
            JPanel pie = new JPanel(new BorderLayout()); pie.setOpaque(false); pie.add(aviso, BorderLayout.WEST); pie.add(actualizar, BorderLayout.EAST); contenido.add(pie, BorderLayout.SOUTH);
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
                modelo.addRow(new Object[] { persona.id, nombreCompleto(persona) });
            }
        }

        private void abrirSeleccionada() {
            if (tabla.getSelectedRow() < 0) { JOptionPane.showMessageDialog(tabla, "Selecciona primero una persona de la tabla."); return; }
            int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
            Persona persona = repositorio.buscar((Integer) modelo.getValueAt(fila, 0));
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
            int id = (Integer) modelo.getValueAt(fila, 0);
            String persona = (String) modelo.getValueAt(fila, 1);
            if (JOptionPane.showConfirmDialog(tabla, "¿Eliminar la ficha de " + persona + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                repositorio.eliminar(id); BasePonPox.copiaSeguridad(); cargarListado(""); limpiarFicha();
            }
        }

        private void guardarFicha() {
            if (nombreCompleto.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(nombreCompleto, "Indica el nombre completo.", "Datos pendientes", JOptionPane.WARNING_MESSAGE); return;
            }
            Persona persona = new Persona(idActual, nombreCompleto.getText(), "", "", dni.getText(), direccionCompleta(), telefono.getText(), email.getText(), seguridadSocial.getText(), iban.getText(), (String) coche.getSelectedItem(), fechaNacimiento.getText(), altura.getText(), tallaPantalon.getText(), tallaCamisa.getText(), tallaCamiseta.getText(), tallaCalzado.getText(), idiomas.getText(), experienciaSeleccionada(), observaciones.getText());
            int idGuardado = repositorio.guardar(persona);
            if (idGuardado < 0) { JOptionPane.showMessageDialog(nombreCompleto, "No se ha podido guardar la ficha. Comprueba que las bibliotecas de la carpeta lib están presentes.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            BasePonPox.copiaSeguridad(); idActual = idGuardado; estadoFicha.setText("Ficha guardada n.º " + idActual + " · " + nombreCompleto.getText().trim());
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
                BasePonPox.copiaSeguridad(); cargarListado("");
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
                List<Persona> personas = seleccionarPersonasParaInforme();
                if (personas.isEmpty()) return;
                if (formato.equals("xls")) generarXls(destino, elegidos, personas); else generarPdf(destino, elegidos, personas);
                JOptionPane.showMessageDialog(tabla, "Informe creado correctamente:\n" + destino.getAbsolutePath(), "PonPox", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception error) {
                JOptionPane.showMessageDialog(tabla, "No se ha podido crear el informe:\n" + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private List<Persona> seleccionarPersonasParaInforme() {
            List<Persona> disponibles = repositorio.listar("");
            JList<Persona> lista = new JList<>(disponibles.toArray(new Persona[0]));
            lista.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            if (tabla.getSelectedRow() >= 0) {
                int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
                Persona actual = repositorio.buscar((Integer) modelo.getValueAt(fila, 0));
                if (actual != null) lista.setSelectedValue(actual, true);
            }
            JScrollPane scroll = new JScrollPane(lista); scroll.setPreferredSize(new Dimension(360, 300));
            int opcion = JOptionPane.showConfirmDialog(tabla, scroll, "Selecciona una o varias personas para el informe", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            return opcion == JOptionPane.OK_OPTION ? lista.getSelectedValuesList() : Collections.emptyList();
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
        @Override public String toString() { return (nombre + " " + apellido1 + " " + apellido2).trim(); }
    }

    /** Una sola base permanente: no forma parte de los ZIP de actualización. */
    private static class BasePonPox {
        private static File archivo;
        static synchronized String url() {
            preparar();
            return "jdbc:sqlite:" + archivo.getAbsolutePath();
        }
        static synchronized void preparar() {
            if (archivo != null) return;
            String sistema = System.getProperty("os.name", "").toLowerCase();
            File carpeta = sistema.contains("mac")
                ? new File(System.getProperty("user.home"), "Library/Application Support/PonPox")
                : new File(System.getProperty("user.home"), ".ponpox");
            carpeta.mkdirs(); archivo = new File(carpeta, "ponpox.db");
            File antigua = new File(System.getProperty("user.dir"), "ponpox.db");
            if (!archivo.exists() && antigua.exists()) {
                try { Files.copy(antigua.toPath(), archivo.toPath(), StandardCopyOption.COPY_ATTRIBUTES); }
                catch (Exception e) { System.err.println("No se ha podido migrar la base existente: " + e.getMessage()); }
            }
        }
        static synchronized void copiaSeguridad() {
            preparar(); if (!archivo.exists()) return;
            File copias = new File(archivo.getParentFile(), "copias de seguridad"); copias.mkdirs();
            String marca = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
            File destino = new File(copias, "ponpox_" + marca + ".db");
            try { Files.copy(archivo.toPath(), destino.toPath(), StandardCopyOption.COPY_ATTRIBUTES); }
            catch (Exception e) { System.err.println("No se ha podido crear la copia de seguridad: " + e.getMessage()); }
        }
        static String ubicacion() { preparar(); return archivo.getAbsolutePath(); }
    }

    private static class RepositorioPersonal {
        private static final String URL = BasePonPox.url();
        void inicializar() {
            BasePonPox.preparar();
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
        private static final String URL = BasePonPox.url();
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
        void registrarAnalisis(Presupuesto presupuesto, int personaId, String dia) {
            String existe = "SELECT id FROM historial_experiencia WHERE personal_id=? AND presupuesto=? AND fecha_inicio=?";
            try (Connection c = DriverManager.getConnection(URL); PreparedStatement comprobar = c.prepareStatement(existe)) {
                comprobar.setInt(1, personaId); comprobar.setString(2, presupuesto.numero); comprobar.setString(3, dia);
                if (comprobar.executeQuery().next()) return;
                c.setAutoCommit(false);
                try (PreparedStatement e = c.prepareStatement("INSERT INTO eventos(presupuesto,evento,fecha_inicio,fecha_fin) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    e.setString(1, presupuesto.numero); e.setString(2, presupuesto.evento); e.setString(3, dia); e.setString(4, dia); e.executeUpdate();
                    ResultSet claves=e.getGeneratedKeys(); if (!claves.next()) {c.rollback(); return;}
                    try (PreparedStatement h=c.prepareStatement("INSERT INTO historial_experiencia(personal_id,evento_id,fecha_inicio,fecha_fin,evento,presupuesto) VALUES(?,?,?,?,?,?)")) {
                        h.setInt(1,personaId);h.setInt(2,claves.getInt(1));h.setString(3,dia);h.setString(4,dia);h.setString(5,presupuesto.evento);h.setString(6,presupuesto.numero);h.executeUpdate();
                    }
                    c.commit();
                }
            } catch (SQLException e) { System.err.println("Error al actualizar historial: " + e.getMessage()); }
        }
    }

    private static void abrirClientes() {
        JFrame ventana = crearVentanaArea("Clientes");
        ventana.setContentPane(new GestionClientes().crearContenido());
        ventana.setVisible(true);
    }

    private static class GestionClientes {
        private final RepositorioClientes repositorio = new RepositorioClientes();
        private final DefaultTableModel modelo = new DefaultTableModel(new String[] { "ID", "Cliente", "CIF/NIF", "Teléfono", "E-mail", "Condiciones de pago", "Zona" }, 0) {
            @Override public boolean isCellEditable(int f, int c) { return false; }
        };
        private JTable tabla;
        JComponent crearContenido() {
            repositorio.inicializar();
            JPanel panel = new JPanel(new BorderLayout(0, 16)); panel.setBackground(Color.WHITE); panel.setBorder(new EmptyBorder(25, 32, 25, 32));
            JLabel titulo = new JLabel("Base de datos de clientes"); titulo.setFont(new Font("SansSerif", Font.BOLD, 28)); titulo.setForeground(ROSA_OSCURO);
            JLabel ayuda = new JLabel("Importa el CSV de clientes para conservar contactos, condiciones de pago y datos fiscales."); ayuda.setForeground(new Color(85,85,90));
            JPanel cabecera = new JPanel(new BorderLayout()); cabecera.setOpaque(false); JPanel textos = new JPanel(); textos.setOpaque(false); textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS)); textos.add(titulo); textos.add(Box.createVerticalStrut(6)); textos.add(ayuda); cabecera.add(textos, BorderLayout.WEST);
            JButton importar = botonRosa("Importar clientes CSV"); importar.addActionListener(e -> importar()); cabecera.add(importar, BorderLayout.EAST); panel.add(cabecera, BorderLayout.NORTH);
            tabla = new JTable(modelo); tabla.setAutoCreateRowSorter(true); tabla.setRowHeight(29); tabla.getTableHeader().setBackground(GRAFITO); tabla.getTableHeader().setForeground(Color.WHITE); tabla.getColumnModel().getColumn(0).setMinWidth(0); tabla.getColumnModel().getColumn(0).setMaxWidth(0); panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
            JTextField buscar = nuevoCampo(); buscar.setPreferredSize(new Dimension(300,33)); buscar.getDocument().addDocumentListener(new DocumentListener() { public void insertUpdate(DocumentEvent e){cargar(buscar.getText());} public void removeUpdate(DocumentEvent e){cargar(buscar.getText());} public void changedUpdate(DocumentEvent e){cargar(buscar.getText());} });
            JPanel pie = new JPanel(new BorderLayout()); pie.setOpaque(false); JPanel izquierda=new JPanel(new FlowLayout(FlowLayout.LEFT));izquierda.setOpaque(false);izquierda.add(new JLabel("Buscar: "));izquierda.add(buscar);pie.add(izquierda,BorderLayout.WEST);JButton historial=botonSecundario("Presupuestos del cliente");historial.addActionListener(e->historialCliente());JPanel derecha=new JPanel(new FlowLayout(FlowLayout.RIGHT));derecha.setOpaque(false);derecha.add(historial);pie.add(derecha,BorderLayout.EAST);panel.add(pie, BorderLayout.SOUTH); cargar(""); return panel;
        }
        private void cargar(String filtro) { if (tabla == null) return; modelo.setRowCount(0); for (Cliente c : repositorio.listar(filtro)) modelo.addRow(new Object[] {c.id,c.nombre,c.cif,c.telefono,c.email,c.condiciones,c.zona}); }
        private void importar() {
            JFileChooser selector = new JFileChooser(); selector.setDialogTitle("Selecciona el CSV de clientes");
            if (selector.showOpenDialog(tabla) != JFileChooser.APPROVE_OPTION) return;
            try { int[] r = repositorio.importarCsv(selector.getSelectedFile()); BasePonPox.copiaSeguridad(); cargar(""); JOptionPane.showMessageDialog(tabla, "Importación terminada. Registros procesados: " + r[0]); }
            catch (Exception ex) { JOptionPane.showMessageDialog(tabla, "No se ha podido importar el CSV:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        }
        private void historialCliente(){if(tabla.getSelectedRow()<0){JOptionPane.showMessageDialog(tabla,"Selecciona primero un cliente.");return;}int fila=tabla.convertRowIndexToModel(tabla.getSelectedRow());String nombre=textoModelo(fila,1);JFrame v=crearVentanaArea("Historial comercial · "+nombre);v.setSize(1050,620);DefaultTableModel m=new DefaultTableModel(new String[]{"Presupuesto","Factura","Evento","Inicio","Fin","Base imponible"},0){@Override public boolean isCellEditable(int a,int b){return false;}};JTable t=new JTable(m);JSpinner desde=fechaSpinner(),hasta=fechaSpinner();java.util.Calendar cal=java.util.Calendar.getInstance();cal.set(java.util.Calendar.MONTH,0);cal.set(java.util.Calendar.DAY_OF_MONTH,1);desde.setValue(cal.getTime());Runnable cargar=()->{m.setRowCount(0);for(Object[] x:new RepositorioProduccion().presupuestosCliente(nombre,fecha(desde),fecha(hasta)))m.addRow(x);};cargar.run();JPanel p=new JPanel(new BorderLayout(8,8));p.setBorder(new EmptyBorder(20,25,20,25));JPanel cab=new JPanel(new FlowLayout(FlowLayout.LEFT));cab.add(new JLabel("Desde"));cab.add(desde);cab.add(new JLabel("Hasta"));cab.add(hasta);JButton act=botonSecundario("Consultar");act.addActionListener(e->cargar.run());cab.add(act);p.add(cab,BorderLayout.NORTH);p.add(new JScrollPane(t),BorderLayout.CENTER);JPanel acciones=new JPanel(new FlowLayout(FlowLayout.RIGHT));JButton xls=botonSecundario("Exportar XLS");xls.addActionListener(e->exportarHistorial(nombre,m,"xls"));JButton pdf=botonRosa("Exportar PDF");pdf.addActionListener(e->exportarHistorial(nombre,m,"pdf"));acciones.add(xls);acciones.add(pdf);p.add(acciones,BorderLayout.SOUTH);v.setContentPane(p);v.setVisible(true);}
        private String textoModelo(int fila,int columna){Object x=modelo.getValueAt(fila,columna);return x==null?"":x.toString();}
        private void exportarHistorial(String cliente,DefaultTableModel m,String formato){JFileChooser s=new JFileChooser();s.setSelectedFile(new File("Presupuestos_"+cliente.replaceAll("[^A-Za-z0-9]","_")+"."+formato));if(s.showSaveDialog(null)!=JFileChooser.APPROVE_OPTION)return;File f=s.getSelectedFile();if(!f.getName().endsWith("."+formato))f=new File(f.getAbsolutePath()+"."+formato);try{if("xls".equals(formato)){try(BufferedWriter w=new BufferedWriter(new FileWriter(f))){w.write("<?xml version=\"1.0\"?><Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"><Worksheet ss:Name=\"Presupuestos\"><Table>");for(int c=0;c<m.getColumnCount();c++)w.write("<Cell><Data ss:Type=\"String\">"+xml(m.getColumnName(c))+"</Data></Cell>");for(int r=0;r<m.getRowCount();r++){w.write("<Row>");for(int c=0;c<m.getColumnCount();c++)w.write("<Cell><Data ss:Type=\"String\">"+xml(texto(m.getValueAt(r,c)))+"</Data></Cell>");w.write("</Row>");}w.write("</Table></Worksheet></Workbook>");}}else{com.itextpdf.text.Document d=new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate(),25,25,25,25);com.itextpdf.text.pdf.PdfWriter.getInstance(d,new FileOutputStream(f));d.open();d.add(new com.itextpdf.text.Paragraph("PONPOX · Presupuestos de "+cliente));com.itextpdf.text.pdf.PdfPTable q=new com.itextpdf.text.pdf.PdfPTable(m.getColumnCount());q.setWidthPercentage(100);for(int c=0;c<m.getColumnCount();c++)q.addCell(m.getColumnName(c));for(int r=0;r<m.getRowCount();r++)for(int c=0;c<m.getColumnCount();c++)q.addCell(texto(m.getValueAt(r,c)));d.add(q);d.close();}JOptionPane.showMessageDialog(null,"Informe creado correctamente.");}catch(Exception e){JOptionPane.showMessageDialog(null,"No se ha podido crear el informe: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}}
        private String texto(Object o){return o==null?"":o.toString();}private String xml(String x){return texto(x).replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    }

    private static void abrirProduccion() {
        JFrame ventana = crearVentanaArea("Producción");
        ventana.setMinimumSize(new Dimension(1030, 680)); ventana.setSize(1220, 790);
        ventana.setContentPane(new GestionProduccion().crearPestanas()); ventana.setVisible(true);
    }

    private static class GestionProduccion {
        private final RepositorioProduccion repo = new RepositorioProduccion();
        private final JTextField numero = nuevoCampo(), factura = nuevoCampo(), evento = nuevoCampo(), venue = nuevoCampo(), vestuario = nuevoCampo(), solicitante = nuevoCampo();
        private final JComboBox<Cliente> cliente = new JComboBox<>();
        private final JSpinner emision = fechaSpinner(), validoHasta = fechaSpinner(), inicio = fechaSpinner(), fin = fechaSpinner();
        private final DefaultTableModel lineas = new DefaultTableModel(new String[] {"Producto", "Indicaciones de la clienta", "Unidad", "Cantidad", "Precio sin IVA", "IVA %", "Descuento %", "Importe"}, 0) { @Override public boolean isCellEditable(int f,int c){return c < 7;} };
        private final JComboBox<Presupuesto> presupuestoLineas = new JComboBox<>();
        private final JComboBox<Presupuesto> presupuestoAnalisis = new JComboBox<>();
        private final JComboBox<Persona> trabajador = new JComboBox<>();
        private final JTextField ibanAnalisis = nuevoCampo(), puesto = nuevoCampo(), salarioNeto = nuevoCampo(), margenBruto = nuevoCampo(), margenTarifa = nuevoCampo(), dietaEmpleado = nuevoCampo(), dietaEmpresa = nuevoCampo(), kmEmpleado = nuevoCampo(), kmEmpresa = nuevoCampo(), conceptoLibre = nuevoCampo(), cantidadLibre = nuevoCampo(), margenIntegral = nuevoCampo();
        private final JComboBox<String> pss = new JComboBox<>(new String[] {"No", "Sí (33,62 €)"});
        private final JSpinner diaTrabajado = fechaSpinner(), horaDesde = horaSpinner(), horaHasta = horaSpinner();
        private final JLabel horas = valorCalculado(), costesCotizables = valorCalculado(), tarifa = valorCalculado(), totalOtros = valorCalculado(), totalCostes = valorCalculado(), tarifaIntegral = valorCalculado(), baseImponible = valorCalculado(), eventoAnalisis = valorCalculado();
        private final DefaultTableModel nominas = new DefaultTableModel(new String[] {"Presupuesto","Factura","Cliente","Evento","Trabajador","IBAN","Salario neto","CPT","PSS","Día"},0) { @Override public boolean isCellEditable(int f,int c){return false;} };
        private List<Persona> trabajadores = new ArrayList<>();
        private boolean filtrandoTrabajador;

        GestionProduccion() { new RepositorioPersonal().inicializar(); new RepositorioClientes().inicializar(); repo.inicializar(); }
        JComponent crearPestanas() {
            JTabbedPane p = new JTabbedPane(); p.setFont(new Font("SansSerif", Font.BOLD,14)); p.addTab("Presupuestos", panelPresupuestos()); p.addTab("Producción del presupuesto", panelLineas()); p.addTab("Análisis económico", panelAnalisis()); p.addTab("Informes", panelInformes()); return p;
        }
        private JComponent panelPresupuestos() {
            JPanel cont = new JPanel(new BorderLayout(0,16)); cont.setBackground(Color.WHITE); cont.setBorder(new EmptyBorder(25,32,25,32));
            JLabel titulo = new JLabel("Presupuesto"); titulo.setFont(new Font("SansSerif",Font.BOLD,28)); titulo.setForeground(ROSA_OSCURO); JButton importar = botonSecundario("Importar presupuesto PDF"); importar.addActionListener(e -> importarPdf()); JPanel cab = new JPanel(new BorderLayout()); cab.setOpaque(false); cab.add(titulo,BorderLayout.WEST); cab.add(importar,BorderLayout.EAST); cont.add(cab,BorderLayout.NORTH);
            JPanel f = new JPanel(new GridBagLayout()); f.setBackground(Color.WHITE); GridBagConstraints c=nuevasRestricciones(); agregarCampo(f,"N.º de presupuesto *",numero,0,0,c); agregarCampo(f,"Factura",factura,0,1,c); agregarCampo(f,"Fecha de emisión",emision,1,0,c); agregarCampo(f,"Válido hasta",validoHasta,1,1,c); agregarCampo(f,"Cliente",cliente,2,0,c); agregarCampo(f,"Evento *",evento,2,1,c); agregarCampo(f,"Inicio del servicio",inicio,3,0,c); agregarCampo(f,"Fin del servicio",fin,3,1,c); agregarCampo(f,"Venue",venue,4,0,c); agregarCampo(f,"Vestuario",vestuario,4,1,c); agregarCampo(f,"Solicitante",solicitante,5,0,c); cont.add(new JScrollPane(f),BorderLayout.CENTER);
            JButton guardar=botonRosa("Guardar presupuesto"); guardar.addActionListener(e -> guardarPresupuesto()); JPanel pie=new JPanel(new FlowLayout(FlowLayout.RIGHT));pie.setOpaque(false);pie.add(guardar);cont.add(pie,BorderLayout.SOUTH); cargarReferencias(); return cont;
        }
        private JComponent panelLineas() {
            JPanel cont=new JPanel(new BorderLayout(0,16));cont.setBackground(Color.WHITE);cont.setBorder(new EmptyBorder(25,32,25,32)); JLabel t=new JLabel("Producción del presupuesto");t.setFont(new Font("SansSerif",Font.BOLD,28));t.setForeground(ROSA_OSCURO);cont.add(t,BorderLayout.NORTH);
            JTable tabla=new JTable(lineas);tabla.setRowHeight(30);tabla.putClientProperty("terminateEditOnFocusLost",Boolean.TRUE);
            JPanel centro=new JPanel(new BorderLayout(0,10));centro.setOpaque(false);JPanel selector=new JPanel(new FlowLayout(FlowLayout.LEFT));selector.setOpaque(false);selector.add(new JLabel("Presupuesto:"));selector.add(presupuestoLineas);JButton cargar=botonSecundario("Cargar / modificar líneas");cargar.addActionListener(e->cargarLineas());selector.add(cargar);centro.add(selector,BorderLayout.NORTH);centro.add(new JScrollPane(tabla),BorderLayout.CENTER);cont.add(centro,BorderLayout.CENTER);
            JPanel pie=new JPanel(new FlowLayout(FlowLayout.RIGHT));pie.setOpaque(false); JButton nueva=botonSecundario("Añadir línea");nueva.addActionListener(e->lineas.addRow(new Object[]{"","","Hora-s",1,0,0,0,0})); JButton borrar=botonSecundario("Eliminar línea");borrar.addActionListener(e->{int r=tabla.getSelectedRow();if(r>=0)lineas.removeRow(r);}); JButton guardar=botonRosa("Guardar cambios de líneas");guardar.addActionListener(e->guardarLineas());pie.add(nueva);pie.add(borrar);pie.add(guardar);cont.add(pie,BorderLayout.SOUTH);return cont;
        }
        private JComponent panelAnalisis() {
            JPanel cont=new JPanel(new BorderLayout(0,12));cont.setBackground(Color.WHITE);cont.setBorder(new EmptyBorder(22,30,22,30)); JLabel t=new JLabel("Análisis económico por trabajador y día");t.setFont(new Font("SansSerif",Font.BOLD,27));t.setForeground(ROSA_OSCURO);cont.add(t,BorderLayout.NORTH); JPanel f=new JPanel(new GridBagLayout());f.setBackground(Color.WHITE);GridBagConstraints c=nuevasRestricciones();
            agregarSeccion(f,"Referencia y trabajador",0,c); agregarCampo(f,"Presupuesto",presupuestoAnalisis,1,0,c); agregarCampo(f,"Evento",eventoAnalisis,1,1,c); agregarCampo(f,"Trabajador",trabajador,2,0,c); agregarCampo(f,"IBAN",ibanAnalisis,2,1,c); agregarSeccion(f,"Trabajo y salario",3,c); agregarCampo(f,"Día",diaTrabajado,4,0,c); agregarCampo(f,"Puesto",puesto,4,1,c); agregarCampo(f,"Horario: de",horaDesde,5,0,c); agregarCampo(f,"Horario: a",horaHasta,5,1,c); agregarCampo(f,"Número de horas",horas,6,0,c); agregarCampo(f,"Salario neto (€)",salarioNeto,6,1,c); agregarCampo(f,"Margen bruto (%)",margenBruto,7,0,c); agregarCampo(f,"Costes cotizables",costesCotizables,7,1,c); agregarCampo(f,"Margen bruto tarifa (%)",margenTarifa,8,0,c); agregarCampo(f,"Tarifa",tarifa,8,1,c); agregarSeccion(f,"Otros costes",9,c); agregarCampo(f,"Dieta empleado",dietaEmpleado,10,0,c);agregarCampo(f,"Dieta empresa",dietaEmpresa,10,1,c);agregarCampo(f,"Km. empleado",kmEmpleado,11,0,c);agregarCampo(f,"Km. empresa",kmEmpresa,11,1,c);agregarCampo(f,"PSS",pss,12,0,c);agregarCampo(f,"Concepto libre",conceptoLibre,12,1,c);agregarCampo(f,"Cantidad concepto libre",cantidadLibre,13,0,c);agregarCampo(f,"Total otros costes",totalOtros,13,1,c);agregarSeccion(f,"Resultado",14,c);agregarCampo(f,"Total costes",totalCostes,15,0,c);agregarCampo(f,"Margen comercial integral (%)",margenIntegral,15,1,c);agregarCampo(f,"Tarifa integral",tarifaIntegral,16,0,c);agregarCampo(f,"Base imponible",baseImponible,16,1,c);
            cont.add(new JScrollPane(f),BorderLayout.CENTER); JPanel pie=new JPanel(new FlowLayout(FlowLayout.RIGHT));pie.setOpaque(false);JButton vista=botonSecundario("Previsualizar producción");vista.addActionListener(e->previsualizarProduccion());JButton calc=botonSecundario("Calcular");calc.addActionListener(e->calcular());JButton save=botonRosa("Guardar jornada y añadir otra");save.addActionListener(e->guardarAnalisis());pie.add(vista);pie.add(calc);pie.add(save);cont.add(pie,BorderLayout.SOUTH); configurarAnalisis();return cont;
        }
        private JComponent panelInformes() {
            JPanel cont=new JPanel(new BorderLayout(0,16));cont.setBackground(Color.WHITE);cont.setBorder(new EmptyBorder(25,32,25,32)); JPanel cab=new JPanel();cab.setOpaque(false);cab.setLayout(new BoxLayout(cab,BoxLayout.Y_AXIS));JLabel t=new JLabel("Cuadro de nóminas");t.setFont(new Font("SansSerif",Font.BOLD,28));t.setForeground(ROSA_OSCURO);JLabel a=new JLabel("Una línea por trabajador y día; la fila TOTAL agrupa exclusivamente cada presupuesto.");a.setForeground(new Color(90,90,96));cab.add(t);cab.add(Box.createVerticalStrut(5));cab.add(a);cont.add(cab,BorderLayout.NORTH); JTable tabla=new JTable(nominas);tabla.setAutoCreateRowSorter(true);tabla.setRowHeight(29);cont.add(new JScrollPane(tabla),BorderLayout.CENTER); JPanel pie=new JPanel(new FlowLayout(FlowLayout.RIGHT));pie.setOpaque(false);JButton actualizar=botonSecundario("Actualizar cuadro de nóminas");actualizar.addActionListener(e->cargarNominas());JButton xls=botonSecundario("Exportar XLS");xls.addActionListener(e->exportarNominas("xls"));JButton pdf=botonRosa("Exportar PDF");pdf.addActionListener(e->exportarNominas("pdf"));pie.add(actualizar);pie.add(xls);pie.add(pdf);cont.add(pie,BorderLayout.SOUTH);cargarNominas();return cont;
        }
        private void cargarReferencias() { cliente.removeAllItems(); for(Cliente x:new RepositorioClientes().listar("")) cliente.addItem(x); presupuestoAnalisis.removeAllItems(); presupuestoLineas.removeAllItems(); for(Presupuesto x:repo.presupuestos()){ presupuestoAnalisis.addItem(x); presupuestoLineas.addItem(x); } }
        private void configurarAnalisis() { trabajador.setEditable(true); trabajadores=new RepositorioPersonal().listar(""); for(Persona p:trabajadores) trabajador.addItem(p); trabajador.addActionListener(e->{Persona p=personaSeleccionada(); if(p!=null) ibanAnalisis.setText(p.iban);}); JTextField editor=(JTextField)trabajador.getEditor().getEditorComponent(); editor.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){filtrar();}public void removeUpdate(DocumentEvent e){filtrar();}public void changedUpdate(DocumentEvent e){filtrar();}private void filtrar(){if(filtrandoTrabajador)return;String buscado=editor.getText().trim().toUpperCase();if(buscado.length()<2)return;SwingUtilities.invokeLater(()->{if(filtrandoTrabajador)return;filtrandoTrabajador=true;trabajador.removeAllItems();for(Persona p:trabajadores)if(nombreTrabajador(p).toUpperCase().contains(buscado))trabajador.addItem(p);trabajador.setSelectedItem(buscado);editor.setText(buscado);trabajador.showPopup();filtrandoTrabajador=false;});}}); presupuestoAnalisis.addActionListener(e->{Presupuesto p=(Presupuesto)presupuestoAnalisis.getSelectedItem();eventoAnalisis.setText(p==null?"":p.evento);}); }
        private void importarPdf() { JFileChooser s=new JFileChooser();s.setDialogTitle("Selecciona un presupuesto PDF"); if(s.showOpenDialog(numero)!=JFileChooser.APPROVE_OPTION)return; try { DatosPdf d=DatosPdf.leer(s.getSelectedFile());numero.setText(d.numero);factura.setText(d.factura);evento.setText(d.evento);venue.setText(d.venue);vestuario.setText(d.vestuario);solicitante.setText(d.solicitante);emision.setValue(d.emision);validoHasta.setValue(d.validez);inicio.setValue(d.inicio);fin.setValue(d.fin);lineas.setRowCount(0);for(Object[] l:d.lineas)lineas.addRow(l);JOptionPane.showMessageDialog(numero,"Presupuesto importado. Revisa especialmente las fechas de servicio antes de guardarlo.");}catch(Exception ex){JOptionPane.showMessageDialog(numero,"No se ha podido leer el PDF:\n"+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);} }
        private void guardarPresupuesto(){if(numero.getText().trim().isEmpty()||evento.getText().trim().isEmpty()){JOptionPane.showMessageDialog(numero,"Indica el número de presupuesto y el evento.");return;} Cliente c=(Cliente)cliente.getSelectedItem();repo.guardarPresupuesto(new Presupuesto(0,numero.getText(),factura.getText(),fecha(emision),fecha(validoHasta),evento.getText(),fecha(inicio),fecha(fin),venue.getText(),vestuario.getText(),solicitante.getText(),c==null?"":c.nombre));BasePonPox.copiaSeguridad();cargarReferencias();JOptionPane.showMessageDialog(numero,"Presupuesto guardado y disponible para producir. El historial comercial del cliente se actualiza con este presupuesto.");}
        private void cargarLineas(){Presupuesto p=(Presupuesto)presupuestoLineas.getSelectedItem();if(p==null)return;numero.setText(p.numero);factura.setText(p.factura);evento.setText(p.evento);emision.setValue(fechaTexto(p.emision));validoHasta.setValue(fechaTexto(p.validez));inicio.setValue(fechaTexto(p.inicio));fin.setValue(fechaTexto(p.fin));venue.setText(p.venue);vestuario.setText(p.vestuario);solicitante.setText(p.solicitante);lineas.setRowCount(0);for(LineaPresupuesto l:repo.lineasDe(p.id))lineas.addRow(new Object[]{l.producto,l.indicaciones,l.unidad,l.cantidad,l.precio,l.iva,l.descuento,l.importe});}
        private void guardarLineas(){Presupuesto p=(Presupuesto)presupuestoLineas.getSelectedItem();if(p==null)p=repo.buscar(numero.getText());if(p==null){JOptionPane.showMessageDialog(numero,"Selecciona o guarda primero la cabecera del presupuesto.");return;}List<LineaPresupuesto> datos=new ArrayList<>();for(int i=0;i<lineas.getRowCount();i++){String prod=text(lineas.getValueAt(i,0));if(prod.isEmpty())continue;double q=numero(lineas.getValueAt(i,3)),precio=numero(lineas.getValueAt(i,4)),iva=numero(lineas.getValueAt(i,5)),dto=numero(lineas.getValueAt(i,6));datos.add(new LineaPresupuesto(prod,text(lineas.getValueAt(i,1)),text(lineas.getValueAt(i,2)),q,precio,iva,dto,importe(q,precio,dto)));}repo.guardarLineas(p.id,datos);BasePonPox.copiaSeguridad();JOptionPane.showMessageDialog(numero,"Cambios de las líneas guardados.");}
        private void calcular(){double h=Math.max(2d,(hora(horaHasta)-hora(horaDesde))/60d);double net=numero(salarioNeto.getText()),cot=net*1.40d,mt=porcentaje(margenTarifa.getText()),tar=division(cot,1-mt);double otros=numero(dietaEmpleado.getText())+numero(kmEmpleado.getText())+(pss.getSelectedIndex()==1?33.62d:0)+numero(cantidadLibre.getText());double total=tar+otros,mi=porcentaje(margenIntegral.getText()),integral=division(total,1-mi);horas.setText(moneda(h));costesCotizables.setText(moneda(cot));tarifa.setText(moneda(tar));totalOtros.setText(moneda(otros));totalCostes.setText(moneda(total));tarifaIntegral.setText(moneda(integral));baseImponible.setText(moneda(integral));}
        private void guardarAnalisis(){calcular();Presupuesto p=(Presupuesto)presupuestoAnalisis.getSelectedItem();Persona w=personaSeleccionada();if(p==null||w==null){JOptionPane.showMessageDialog(trabajador,"Selecciona un presupuesto y un trabajador de la lista.");return;}String dia=fecha(diaTrabajado);repo.guardarAnalisis(new Analisis(p.id,w.id,dia,puesto.getText(),horaTexto(horaDesde),horaTexto(horaHasta),numero(horas.getText()),numero(salarioNeto.getText()),numero(margenBruto.getText()),numero(costesCotizables.getText()),numero(margenTarifa.getText()),numero(tarifa.getText()),numero(dietaEmpleado.getText()),numero(dietaEmpresa.getText()),numero(kmEmpleado.getText()),numero(kmEmpresa.getText()),pss.getSelectedIndex()==1?33.62d:0,conceptoLibre.getText(),numero(cantidadLibre.getText()),numero(totalOtros.getText()),numero(totalCostes.getText()),numero(margenIntegral.getText()),numero(tarifaIntegral.getText()),numero(baseImponible.getText())));new RepositorioEventos().registrarAnalisis(p,w.id,dia);BasePonPox.copiaSeguridad();limpiarJornada();JOptionPane.showMessageDialog(trabajador,"Jornada guardada. Puedes añadir ahora otro trabajador o al mismo trabajador en otro día; el historial se ha actualizado.");}
        private void limpiarJornada(){trabajador.setSelectedItem(null);ibanAnalisis.setText("");puesto.setText("");salarioNeto.setText("");margenBruto.setText("");margenTarifa.setText("");dietaEmpleado.setText("");dietaEmpresa.setText("");kmEmpleado.setText("");kmEmpresa.setText("");conceptoLibre.setText("");cantidadLibre.setText("");pss.setSelectedIndex(0);}
        private void previsualizarProduccion(){Presupuesto p=(Presupuesto)presupuestoAnalisis.getSelectedItem();if(p==null){JOptionPane.showMessageDialog(presupuestoAnalisis,"Selecciona primero el presupuesto que quieres producir.");return;}DefaultTableModel m=new DefaultTableModel(new String[]{"Trabajador","Día","Salario neto","Otros gastos","PSS"},0){@Override public boolean isCellEditable(int r,int c){return false;}};double neto=0,otros=0,pssTotal=0;for(Object[] r:repo.produccionDe(p.id)){m.addRow(r);neto+=numero(r[2]);otros+=numero(r[3]);pssTotal+=numero(r[4]);}m.addRow(new Object[]{"TOTAL "+p.numero,"",moneda(neto),moneda(otros),moneda(pssTotal)});JTable t=new JTable(m);t.setRowHeight(28);JFrame v=crearVentanaArea("Previsualización · "+p.numero);v.setSize(820,500);JPanel q=new JPanel(new BorderLayout(10,10));q.setBorder(new EmptyBorder(20,22,20,22));JLabel h=new JLabel("Producción de "+p.numero+" · "+p.evento);h.setFont(new Font("SansSerif",Font.BOLD,22));q.add(h,BorderLayout.NORTH);q.add(new JScrollPane(t),BorderLayout.CENTER);v.setContentPane(q);v.setVisible(true);}
        private void cargarNominas(){nominas.setRowCount(0);for(Object[] r:repo.nominas())nominas.addRow(r);}
        private void exportarNominas(String formato){JFileChooser s=new JFileChooser();s.setSelectedFile(new File("Cuadro_nominas_"+LocalDate.now()+"."+formato));if(s.showSaveDialog(null)!=JFileChooser.APPROVE_OPTION)return;File f=s.getSelectedFile();if(!f.getName().endsWith("."+formato))f=new File(f.getAbsolutePath()+"."+formato);try{if("xls".equals(formato)){try(BufferedWriter w=new BufferedWriter(new FileWriter(f))){w.write("<?xml version=\"1.0\"?><Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"><Worksheet ss:Name=\"Cuadro nóminas\"><Table>");for(int col=0;col<nominas.getColumnCount();col++)w.write("<Cell><Data ss:Type=\"String\">"+esc(nominas.getColumnName(col))+"</Data></Cell>");for(int r=0;r<nominas.getRowCount();r++){w.write("<Row>");for(int col=0;col<nominas.getColumnCount();col++)w.write("<Cell><Data ss:Type=\"String\">"+esc(text(nominas.getValueAt(r,col)))+"</Data></Cell>");w.write("</Row>");}w.write("</Table></Worksheet></Workbook>");}}else{com.itextpdf.text.Document d=new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate(),24,24,24,24);com.itextpdf.text.pdf.PdfWriter.getInstance(d,new FileOutputStream(f));d.open();d.add(new com.itextpdf.text.Paragraph("PONPOX · Cuadro de nóminas"));com.itextpdf.text.pdf.PdfPTable t=new com.itextpdf.text.pdf.PdfPTable(nominas.getColumnCount());t.setWidthPercentage(100);for(int col=0;col<nominas.getColumnCount();col++)t.addCell(nominas.getColumnName(col));for(int r=0;r<nominas.getRowCount();r++)for(int col=0;col<nominas.getColumnCount();col++)t.addCell(text(nominas.getValueAt(r,col)));d.add(t);d.close();}JOptionPane.showMessageDialog(null,"Informe creado correctamente.");}catch(Exception x){JOptionPane.showMessageDialog(null,"No se ha podido crear el informe: "+x.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}}
        private Persona personaSeleccionada(){Object elegido=trabajador.getSelectedItem();if(elegido instanceof Persona)return (Persona)elegido;String b=text(elegido).toUpperCase();for(Persona p:trabajadores)if(nombreTrabajador(p).toUpperCase().equals(b))return p;return null;} private String nombreTrabajador(Persona p){return (p.nombre+" "+p.apellido1+" "+p.apellido2).trim();} private double importe(double q,double p,double d){return q*p*(1-d/100d);} private double division(double a,double b){return Math.abs(b)<.00001?0:a/b;} private String text(Object o){return o==null?"":o.toString().trim();} private double numero(Object o){try{return Double.parseDouble(text(o).replace("€","").replace("%","").replace(",","."));}catch(Exception e){return 0;}} private double porcentaje(String s){double x=numero(s);return x>1?x/100d:x;} private String moneda(double n){return String.format(Locale.US,"%.2f",n);} private String esc(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    }

    private static class Cliente { final int id; final String nombre,direccion,telefono,email,pais,cif,tipo,referencia,condiciones,moneda,zona; Cliente(int id,String n,String d,String t,String e,String p,String cif,String ti,String r,String co,String m,String z){this.id=id;nombre=n;direccion=d;telefono=t;email=e;pais=p;this.cif=cif;tipo=ti;referencia=r;condiciones=co;moneda=m;zona=z;} @Override public String toString(){return nombre;} }
    private static class RepositorioClientes {
        private static final String URL=BasePonPox.url();
        void inicializar(){try(Connection c=DriverManager.getConnection(URL);Statement s=c.createStatement()){s.executeUpdate("CREATE TABLE IF NOT EXISTS clientes (id INTEGER PRIMARY KEY AUTOINCREMENT,nombre TEXT UNIQUE,direccion TEXT,telefono TEXT,email TEXT,pais TEXT,cif TEXT,tipo TEXT,referencia TEXT,condiciones TEXT,moneda TEXT,zona TEXT)");}catch(SQLException e){System.err.println(e.getMessage());} File semilla=new File("Clientes-2026-11-23.csv"); if(listar("").isEmpty()&&semilla.exists())try{importarCsv(semilla);}catch(Exception e){System.err.println("No se ha podido cargar la base inicial de clientes: "+e.getMessage());}}
        List<Cliente> listar(String filtro){List<Cliente> r=new ArrayList<>();try(Connection c=DriverManager.getConnection(URL);PreparedStatement p=c.prepareStatement("SELECT * FROM clientes WHERE nombre LIKE ? OR cif LIKE ? OR email LIKE ? ORDER BY nombre COLLATE NOCASE")){String f="%"+filtro+"%";p.setString(1,f);p.setString(2,f);p.setString(3,f);ResultSet x=p.executeQuery();while(x.next())r.add(de(x));}catch(SQLException e){System.err.println(e.getMessage());}return r;}
        private Cliente de(ResultSet r)throws SQLException{return new Cliente(r.getInt("id"),r.getString("nombre"),r.getString("direccion"),r.getString("telefono"),r.getString("email"),r.getString("pais"),r.getString("cif"),r.getString("tipo"),r.getString("referencia"),r.getString("condiciones"),r.getString("moneda"),r.getString("zona"));}
        int[] importarCsv(File archivo)throws Exception{int n=0,a=0;int[] mapa={1,2,3,4,5,7,8,9,10,11,12};for(List<String> f:leerCsv(archivo)){if(f.size()<2||f.get(0).equalsIgnoreCase("N.º de cliente")||f.get(1).trim().isEmpty())continue;try(Connection conexion=DriverManager.getConnection(URL);PreparedStatement p=conexion.prepareStatement("INSERT INTO clientes(nombre,direccion,telefono,email,pais,cif,tipo,referencia,condiciones,moneda,zona) VALUES(?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT(nombre) DO UPDATE SET direccion=excluded.direccion,telefono=excluded.telefono,email=excluded.email,pais=excluded.pais,cif=excluded.cif,tipo=excluded.tipo,referencia=excluded.referencia,condiciones=excluded.condiciones,moneda=excluded.moneda,zona=excluded.zona")){for(int i=0;i<11;i++)p.setString(i+1,campo(f,mapa[i]));int filas=p.executeUpdate();if(filas>0)n++;}}return new int[]{n,a};}
        private String campo(List<String> r,int i){return i<r.size()?r.get(i).trim():"";}
        private List<List<String>> leerCsv(File f)throws Exception{List<List<String>> filas=new ArrayList<>();try(Reader in=new BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(f),"UTF-8"))){List<String> fila=new ArrayList<>();StringBuilder celda=new StringBuilder();boolean q=false;int v;while((v=in.read())!=-1){char ch=(char)v;if(ch=='\"'){if(q){in.mark(1);int siguiente=in.read();if(siguiente=='\"')celda.append('\"');else{q=false;if(siguiente!=-1)in.reset();}}else q=true;}else if(ch==';'&&!q){fila.add(celda.toString());celda.setLength(0);}else if((ch=='\n'||ch=='\r')&&!q){if(ch=='\r'){in.mark(1);if(in.read()!='\n')in.reset();}fila.add(celda.toString());celda.setLength(0);filas.add(fila);fila=new ArrayList<>();}else celda.append(ch);}if(!fila.isEmpty()||celda.length()>0){fila.add(celda.toString());filas.add(fila);}}return filas;}
    }

    private static class Presupuesto { final int id;final String numero,factura,emision,validez,evento,inicio,fin,venue,vestuario,solicitante,cliente; Presupuesto(int id,String n,String fa,String em,String va,String ev,String ini,String fi,String ve,String vest,String so,String cl){this.id=id;numero=n;factura=fa;emision=em;validez=va;evento=ev;inicio=ini;fin=fi;venue=ve;vestuario=vest;solicitante=so;cliente=cl;} @Override public String toString(){return numero+" · "+evento;} }
    private static class LineaPresupuesto { final String producto,indicaciones,unidad;final double cantidad,precio,iva,descuento,importe; LineaPresupuesto(String p,String i,String u,double c,double pr,double iv,double d,double im){producto=p;indicaciones=i;unidad=u;cantidad=c;precio=pr;iva=iv;descuento=d;importe=im;} }
    private static class Analisis {final int presupuestoId,trabajadorId;final String dia,puesto,desde,hasta,concepto;final double horas,neto,margenBruto,cotizables,margenTarifa,tarifa,dietaEmpleado,dietaEmpresa,kmEmpleado,kmEmpresa,pss,libre,totalOtros,totalCostes,margenIntegral,tarifaIntegral,base;Analisis(int p,int t,String d,String pu,String de,String ha,double h,double n,double mb,double co,double mt,double ta,double die,double diee,double km,double kme,double ps,String cn,double li,double ot,double tc,double mi,double ti,double b){presupuestoId=p;trabajadorId=t;dia=d;puesto=pu;desde=de;hasta=ha;horas=h;neto=n;margenBruto=mb;cotizables=co;margenTarifa=mt;tarifa=ta;dietaEmpleado=die;dietaEmpresa=diee;kmEmpleado=km;kmEmpresa=kme;pss=ps;concepto=cn;libre=li;totalOtros=ot;totalCostes=tc;margenIntegral=mi;tarifaIntegral=ti;base=b;}}
    private static class RepositorioProduccion { private static final String URL=BasePonPox.url();
        void inicializar(){try(Connection c=DriverManager.getConnection(URL);Statement s=c.createStatement()){s.executeUpdate("CREATE TABLE IF NOT EXISTS presupuestos (id INTEGER PRIMARY KEY AUTOINCREMENT,numero TEXT UNIQUE,factura TEXT,emision TEXT,validez TEXT,evento TEXT,inicio TEXT,fin TEXT,venue TEXT,vestuario TEXT,solicitante TEXT,cliente TEXT)");s.executeUpdate("CREATE TABLE IF NOT EXISTS lineas_presupuesto (id INTEGER PRIMARY KEY AUTOINCREMENT,presupuesto_id INTEGER,producto TEXT,indicaciones TEXT,unidad TEXT,cantidad REAL,precio REAL,iva REAL,descuento REAL,importe REAL)");s.executeUpdate("CREATE TABLE IF NOT EXISTS analisis_economico (id INTEGER PRIMARY KEY AUTOINCREMENT,presupuesto_id INTEGER,trabajador_id INTEGER,dia TEXT,puesto TEXT,desde TEXT,hasta TEXT,horas REAL,neto REAL,margen_bruto REAL,cotizables REAL,margen_tarifa REAL,tarifa REAL,dieta_empleado REAL,dieta_empresa REAL,km_empleado REAL,km_empresa REAL,pss REAL,concepto TEXT,libre REAL,total_otros REAL,total_costes REAL,margen_integral REAL,tarifa_integral REAL,base REAL)");}catch(SQLException e){System.err.println(e.getMessage());}}
        void guardarPresupuesto(Presupuesto x){String q="INSERT INTO presupuestos(numero,factura,emision,validez,evento,inicio,fin,venue,vestuario,solicitante,cliente) VALUES(?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT(numero) DO UPDATE SET factura=excluded.factura,emision=excluded.emision,validez=excluded.validez,evento=excluded.evento,inicio=excluded.inicio,fin=excluded.fin,venue=excluded.venue,vestuario=excluded.vestuario,solicitante=excluded.solicitante,cliente=excluded.cliente";try(Connection c=DriverManager.getConnection(URL);PreparedStatement p=c.prepareStatement(q)){String[]v={x.numero,x.factura,x.emision,x.validez,x.evento,x.inicio,x.fin,x.venue,x.vestuario,x.solicitante,x.cliente};for(int i=0;i<v.length;i++)p.setString(i+1,v[i]);p.executeUpdate();}catch(SQLException e){System.err.println(e.getMessage());}}
        Presupuesto buscar(String n){try(Connection c=DriverManager.getConnection(URL);PreparedStatement p=c.prepareStatement("SELECT * FROM presupuestos WHERE numero=?")){p.setString(1,n.trim());ResultSet r=p.executeQuery();return r.next()?pres(r):null;}catch(SQLException e){return null;}}
        List<Presupuesto> presupuestos(){List<Presupuesto>z=new ArrayList<>();try(Connection c=DriverManager.getConnection(URL);Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM presupuestos ORDER BY id DESC")){while(r.next())z.add(pres(r));}catch(SQLException e){System.err.println(e.getMessage());}return z;}
        private Presupuesto pres(ResultSet r)throws SQLException{return new Presupuesto(r.getInt("id"),r.getString("numero"),r.getString("factura"),r.getString("emision"),r.getString("validez"),r.getString("evento"),r.getString("inicio"),r.getString("fin"),r.getString("venue"),r.getString("vestuario"),r.getString("solicitante"),r.getString("cliente"));}
        List<LineaPresupuesto> lineasDe(int presupuestoId){List<LineaPresupuesto> z=new ArrayList<>();try(Connection c=DriverManager.getConnection(URL);PreparedStatement p=c.prepareStatement("SELECT producto,indicaciones,unidad,cantidad,precio,iva,descuento,importe FROM lineas_presupuesto WHERE presupuesto_id=? ORDER BY id")){p.setInt(1,presupuestoId);ResultSet r=p.executeQuery();while(r.next())z.add(new LineaPresupuesto(r.getString(1),r.getString(2),r.getString(3),r.getDouble(4),r.getDouble(5),r.getDouble(6),r.getDouble(7),r.getDouble(8)));}catch(SQLException e){System.err.println(e.getMessage());}return z;}
        void guardarLineas(int id,List<LineaPresupuesto> ls){try(Connection c=DriverManager.getConnection(URL)){c.setAutoCommit(false);try(PreparedStatement d=c.prepareStatement("DELETE FROM lineas_presupuesto WHERE presupuesto_id=?");PreparedStatement p=c.prepareStatement("INSERT INTO lineas_presupuesto(presupuesto_id,producto,indicaciones,unidad,cantidad,precio,iva,descuento,importe) VALUES(?,?,?,?,?,?,?,?,?)")){d.setInt(1,id);d.executeUpdate();for(LineaPresupuesto x:ls){p.setInt(1,id);p.setString(2,x.producto);p.setString(3,x.indicaciones);p.setString(4,x.unidad);p.setDouble(5,x.cantidad);p.setDouble(6,x.precio);p.setDouble(7,x.iva);p.setDouble(8,x.descuento);p.setDouble(9,x.importe);p.addBatch();}p.executeBatch();c.commit();}}catch(SQLException e){System.err.println(e.getMessage());}}
        void guardarAnalisis(Analisis x){String q="INSERT INTO analisis_economico(presupuesto_id,trabajador_id,dia,puesto,desde,hasta,horas,neto,margen_bruto,cotizables,margen_tarifa,tarifa,dieta_empleado,dieta_empresa,km_empleado,km_empresa,pss,concepto,libre,total_otros,total_costes,margen_integral,tarifa_integral,base) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";try(Connection c=DriverManager.getConnection(URL);PreparedStatement p=c.prepareStatement(q)){p.setInt(1,x.presupuestoId);p.setInt(2,x.trabajadorId);p.setString(3,x.dia);p.setString(4,x.puesto);p.setString(5,x.desde);p.setString(6,x.hasta);double[]d={x.horas,x.neto,x.margenBruto,x.cotizables,x.margenTarifa,x.tarifa,x.dietaEmpleado,x.dietaEmpresa,x.kmEmpleado,x.kmEmpresa,x.pss};for(int i=0;i<d.length;i++)p.setDouble(i+7,d[i]);p.setString(18,x.concepto);double[]e={x.libre,x.totalOtros,x.totalCostes,x.margenIntegral,x.tarifaIntegral,x.base};for(int i=0;i<e.length;i++)p.setDouble(i+19,e[i]);p.executeUpdate();}catch(SQLException e){System.err.println(e.getMessage());}}
        List<Object[]> produccionDe(int presupuestoId){List<Object[]> z=new ArrayList<>();String q="SELECT trim(w.nombre||' '||w.apellido1||' '||w.apellido2),a.dia,a.neto,(a.dieta_empleado+a.km_empleado+a.libre),a.pss FROM analisis_economico a JOIN personal w ON w.id=a.trabajador_id WHERE a.presupuesto_id=? ORDER BY a.dia,w.nombre";try(Connection c=DriverManager.getConnection(URL);PreparedStatement p=c.prepareStatement(q)){p.setInt(1,presupuestoId);ResultSet r=p.executeQuery();while(r.next())z.add(new Object[]{r.getString(1),r.getString(2),String.format(Locale.US,"%.2f",r.getDouble(3)),String.format(Locale.US,"%.2f",r.getDouble(4)),String.format(Locale.US,"%.2f",r.getDouble(5))});}catch(SQLException e){System.err.println(e.getMessage());}return z;}
        List<Object[]> presupuestosCliente(String cliente,String desde,String hasta){List<Object[]> z=new ArrayList<>();String q="SELECT p.numero,p.factura,p.evento,p.inicio,p.fin,COALESCE(SUM(l.importe),0) FROM presupuestos p LEFT JOIN lineas_presupuesto l ON l.presupuesto_id=p.id WHERE p.cliente=? AND date(substr(p.inicio,7,4)||'-'||substr(p.inicio,4,2)||'-'||substr(p.inicio,1,2)) BETWEEN date(?) AND date(?) GROUP BY p.id ORDER BY p.inicio";try(Connection c=DriverManager.getConnection(URL);PreparedStatement p=c.prepareStatement(q)){p.setString(1,cliente);p.setString(2,fechaSql(desde));p.setString(3,fechaSql(hasta));ResultSet r=p.executeQuery();while(r.next())z.add(new Object[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),String.format(Locale.US,"%.2f",r.getDouble(6))});}catch(SQLException e){System.err.println(e.getMessage());}return z;}
        private String fechaSql(String f){try{return new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd/MM/yyyy").parse(f));}catch(Exception e){return "1900-01-01";}}
        List<Object[]> nominas(){List<Object[]>z=new ArrayList<>();String q="SELECT p.numero,p.factura,p.cliente,p.evento,trim(w.nombre||' '||w.apellido1||' '||w.apellido2) trabajador,w.iban,a.neto,(a.dieta_empleado+a.km_empleado+a.libre) cpt,a.pss,a.dia FROM analisis_economico a JOIN presupuestos p ON p.id=a.presupuesto_id JOIN personal w ON w.id=a.trabajador_id ORDER BY p.numero,trabajador,a.dia";try(Connection c=DriverManager.getConnection(URL);Statement s=c.createStatement();ResultSet r=s.executeQuery(q)){String actual=null;double neto=0,cpt=0,pss=0;while(r.next()){String presupuesto=r.getString(1);if(actual!=null&&!actual.equals(presupuesto)){z.add(new Object[]{"TOTAL "+actual,"","","","","",String.format(Locale.US,"%.2f",neto),String.format(Locale.US,"%.2f",cpt),String.format(Locale.US,"%.2f",pss),""});neto=cpt=pss=0;}actual=presupuesto;double n=r.getDouble(7),ct=r.getDouble(8),ps=r.getDouble(9);neto+=n;cpt+=ct;pss+=ps;z.add(new Object[]{presupuesto,r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6),String.format(Locale.US,"%.2f",n),String.format(Locale.US,"%.2f",ct),String.format(Locale.US,"%.2f",ps),r.getString(10)});}if(actual!=null)z.add(new Object[]{"TOTAL "+actual,"","","","","",String.format(Locale.US,"%.2f",neto),String.format(Locale.US,"%.2f",cpt),String.format(Locale.US,"%.2f",pss),""});}catch(SQLException e){System.err.println(e.getMessage());}return z;}
    }

    private static class DatosPdf {String numero="",factura="",evento="",venue="",vestuario="",solicitante="";Date emision=new Date(),validez=new Date(),inicio=new Date(),fin=new Date();List<Object[]>lineas=new ArrayList<>();static DatosPdf leer(File f)throws Exception{com.itextpdf.text.pdf.PdfReader r=new com.itextpdf.text.pdf.PdfReader(f.getAbsolutePath());StringBuilder b=new StringBuilder();for(int i=1;i<=r.getNumberOfPages();i++)b.append(com.itextpdf.text.pdf.parser.PdfTextExtractor.getTextFromPage(r,i)).append('\n');r.close();String t=b.toString();DatosPdf d=new DatosPdf();d.numero=buscar(t,"N.º de presupuesto\\s+([A-Z]{1,4}\\d+)");if(d.numero.isEmpty())d.numero=buscar(t,"Presupuesto:\\s*([^\\n]+)").replaceAll("v\\d+$","");d.evento=buscar(t,"Evento:\\s*([^\\n]+)");d.venue=buscar(t,"Venue:\\s*([^\\n]+)");d.vestuario=buscar(t,"Vestuario:\\s*([^\\n]+)");d.solicitante=buscar(t,"Solicitante:\\s*([^\\n]+)");String em=buscar(t,"Fecha\\s+(\\d{2}\\.\\d{2}\\.\\d{4})");String va=buscar(t,"Válido hasta\\s+(\\d{2}\\.\\d{2}\\.\\d{4})");d.emision=fechaPdf(em,d.emision);d.validez=fechaPdf(va,d.validez);Date[] servicio=fechasServicio(t,d.validez);d.inicio=servicio[0];d.fin=servicio[1];for(String l:t.split("\\n")){Matcher m=Pattern.compile("^(.+?)\\s+(\\d+(?:[,.]\\d+)?)\\s+(\\S+)\\s+(\\d+[,.]\\d+)\\s+(\\d+[,.]\\d+)$").matcher(l.trim());if(m.matches())d.lineas.add(new Object[]{m.group(1),"",m.group(3),m.group(2),m.group(4).replace(',','.'),0,0,m.group(5).replace(',','.')});}return d;}static Date[] fechasServicio(String t,Date referencia){java.util.Calendar c=java.util.Calendar.getInstance();c.setTime(referencia);int anyo=c.get(java.util.Calendar.YEAR);Matcher m=Pattern.compile("\\((\\d{1,2})(?:,| y )(\\d{1,2})/(\\d{1,2})\\)").matcher(t);if(m.find()){int d1=Integer.parseInt(m.group(1)),d2=Integer.parseInt(m.group(2)),mes=Integer.parseInt(m.group(3));return new Date[]{crearFecha(d1,mes,anyo),crearFecha(d2,mes,anyo)};}Matcher texto=Pattern.compile("(?:sábado\\s+)?(\\d{1,2})\\s+y\\s+(?:domingo\\s+)?(\\d{1,2})\\s+de\\s+([A-ZÁÉÍÓÚÑa-záéíóúñ]+)",Pattern.CASE_INSENSITIVE).matcher(t);if(texto.find()){int mes=mes(texto.group(3));if(mes>0)return new Date[]{crearFecha(Integer.parseInt(texto.group(1)),mes,anyo),crearFecha(Integer.parseInt(texto.group(2)),mes,anyo)};}return new Date[]{referencia,referencia};}static int mes(String s){String x=s.toLowerCase(Locale.ROOT);String[] m={"enero","febrero","marzo","abril","mayo","junio","julio","agosto","septiembre","octubre","noviembre","diciembre"};for(int i=0;i<m.length;i++)if(x.startsWith(m[i].substring(0,3)))return i+1;return 0;}static Date crearFecha(int d,int m,int a){java.util.Calendar c=java.util.Calendar.getInstance();c.clear();c.set(a,m-1,d,0,0,0);return c.getTime();}static String buscar(String t,String re){Matcher m=Pattern.compile(re,Pattern.CASE_INSENSITIVE).matcher(t);return m.find()?m.group(1).trim():"";}static Date fechaPdf(String s,Date por){try{return new SimpleDateFormat("dd.MM.yyyy").parse(s);}catch(Exception e){return por;}}}

    private static JSpinner fechaSpinner(){JSpinner s=new JSpinner(new SpinnerDateModel(new Date(),null,null,java.util.Calendar.DAY_OF_MONTH));s.setEditor(new JSpinner.DateEditor(s,"dd/MM/yyyy"));return s;} private static String fecha(JSpinner s){return new SimpleDateFormat("dd/MM/yyyy").format((Date)s.getValue());} private static Date fechaTexto(String x){try{return new SimpleDateFormat("dd/MM/yyyy").parse(x);}catch(Exception e){return new Date();}} private static JSpinner horaSpinner(){java.util.Calendar c=java.util.Calendar.getInstance();c.set(java.util.Calendar.HOUR_OF_DAY,9);c.set(java.util.Calendar.MINUTE,0);JSpinner s=new JSpinner(new SpinnerDateModel(c.getTime(),null,null,java.util.Calendar.MINUTE));s.setEditor(new JSpinner.DateEditor(s,"HH:mm"));return s;} private static long hora(JSpinner s){java.util.Calendar c=java.util.Calendar.getInstance();c.setTime((Date)s.getValue());return c.get(java.util.Calendar.HOUR_OF_DAY)*60L+c.get(java.util.Calendar.MINUTE);} private static String horaTexto(JSpinner s){return new SimpleDateFormat("HH:mm").format((Date)s.getValue());} private static JLabel valorCalculado(){JLabel l=new JLabel("0,00");l.setFont(new Font("SansSerif",Font.BOLD,14));return l;}

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
