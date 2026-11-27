import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

class RecursosInstalador {
    public static void main(String[] args) throws Exception {
        Path logo = Path.of(args[0]);
        Path manual = Path.of(args[1]);
        Path output = Path.of(args[2]);
        generarIcono(logo, output.resolve("PonPox.ico"));
        generarManual(logo, manual, output.resolve("Manual.pdf"));
    }

    private static void generarIcono(Path logo, Path destino) throws Exception {
        BufferedImage original = ImageIO.read(logo.toFile());
        BufferedImage icono = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        double escala = 240.0 / Math.max(original.getWidth(), original.getHeight());
        int ancho = (int) Math.round(original.getWidth() * escala);
        int alto = (int) Math.round(original.getHeight() * escala);
        Graphics2D graphics = icono.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.drawImage(original, (256 - ancho) / 2, (256 - alto) / 2, ancho, alto, null);
        graphics.dispose();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(icono, "png", bytes);
        ByteBuffer ico = ByteBuffer.allocate(22 + bytes.size()).order(ByteOrder.LITTLE_ENDIAN);
        ico.putShort((short) 0).putShort((short) 1).putShort((short) 1);
        ico.putInt(0).putShort((short) 1).putShort((short) 32);
        ico.putInt(bytes.size()).putInt(22).put(bytes.toByteArray());
        Files.write(destino, ico.array());
    }

    private static void generarManual(Path logo, Path manual, Path destino) throws Exception {
        try (var salida = Files.newOutputStream(destino)) {
            Document pdf = new Document();
            PdfWriter.getInstance(pdf, salida);
            pdf.addTitle("PonPox para Windows");
            pdf.open();
            try {
                Image marca = Image.getInstance(logo.toString());
                marca.scaleToFit(190, 65);
                pdf.add(marca);
                for (String linea : Files.readAllLines(manual)) {
                    boolean encabezado = linea.startsWith("#");
                    int tam = linea.startsWith("# ") ? 21 : encabezado ? 13 : 11;
                    Font fuente = new Font(Font.FontFamily.HELVETICA, tam, encabezado ? Font.BOLD : Font.NORMAL);
                    Paragraph parrafo = new Paragraph(linea.replaceFirst("^#+ ", ""), fuente);
                    parrafo.setSpacingBefore(encabezado ? 9 : 3);
                    pdf.add(parrafo);
                }
            } finally {
                pdf.close();
            }
        }
    }
}
