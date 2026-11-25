import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

final class DocumentosPonPox {
    private DocumentosPonPox() {}
    static File destino(java.awt.Component padre,String nombre) {
        return destino(padre,nombre,Carpetas.carpeta(Carpetas.P));
    }
    static File destino(java.awt.Component padre,String nombre,File directorio) {
        JFileChooser s=new JFileChooser();
        s.setSelectedFile(new File(directorio,nombre.replaceAll("[^\\p{L}\\p{N} ._-]","_")+".pdf"));
        if(s.showSaveDialog(padre)!=JFileChooser.APPROVE_OPTION)return null;
        File f=s.getSelectedFile();
        if(!f.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".pdf"))f=new File(f+".pdf");
        if(f.exists()&&JOptionPane.showConfirmDialog(padre,"¿Sustituir "+f.getName()+"?","Guardar PDF",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return null;
        return f.getAbsoluteFile();
    }
    private static File plantilla(String nombre)throws Exception {
        File clases=new File(DocumentosPonPox.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        for(File raiz:new File[]{new File("."),clases,clases.getParentFile()}) {
            File f=new File(raiz,"plantillas/"+nombre+".pdf");if(f.isFile())return f;
        }
        throw new IOException("Falta plantillas/"+nombre+".pdf en el proyecto PonPox.");
    }
    interface Escribir {void escribir(OutputStream out)throws Exception;}
    private static void guardar(File f,Escribir crear)throws Exception {
        Path dest=f.toPath().toAbsolutePath();
        Path tmp=Files.createTempFile(dest.getParent(),".ponpox-",".pdf");
        try{try(OutputStream out=Files.newOutputStream(tmp)){crear.escribir(out);}
            Files.move(tmp,dest,StandardCopyOption.REPLACE_EXISTING);
        }finally{Files.deleteIfExists(tmp);}
    }
    static void reporte(File f,Map<String,String> valores)throws Exception {
        File base=plantilla("Reporte");
        if(f.getCanonicalFile().equals(base.getCanonicalFile()))throw new IOException("Elige otro archivo; este es la plantilla.");
        guardar(f,out->{PdfReader r=new PdfReader(base.getPath());try{
            PdfStamper s=new PdfStamper(r,out);AcroFields campos=s.getAcroFields();campos.setGenerateAppearances(true);
            for(Map.Entry<String,String> v:valores.entrySet())if(!campos.setField(v.getKey(),v.getValue()==null?"":v.getValue()))throw new IOException("Campo desconocido: "+v.getKey());
            s.close();
        }finally{r.close();}});
    }
    static class Perfil {
        final String nombre,datos;final List<String> fotos;
        Perfil(String n,List<String> f,String d){nombre=n;fotos=List.copyOf(f);datos=d;if(fotos.size()!=3)throw new IllegalArgumentException("Se requieren tres fotografías.");}
    }
    private static void caja(PdfContentByte cb,String texto,int x,int y,int w,int h,int tam)throws Exception{
        for(int t=tam;t>=8;t--){
            ColumnText ct=new ColumnText(cb);ct.setSimpleColumn(new Phrase(texto,new Font(Font.FontFamily.HELVETICA,t)),x,y,x+w,y+h,t*1.22f,Element.ALIGN_LEFT);
            if(!ColumnText.hasMoreText(ColumnText.duplicate(ct).go(true))){ct.go();return;}
        }
        throw new IOException("El texto no cabe en la ficha. Acorta el texto del perfil o la leyenda.");
    }
    static void setcards(File f,String cliente,String evento,List<Perfil> perfiles)throws Exception{
        File base=plantilla("Setcards");
        if(f.getCanonicalFile().equals(base.getCanonicalFile()))throw new IOException("Elige otro archivo; este es la plantilla.");
        guardar(f,out->{PdfReader r=new PdfReader(base.getPath());try{
            Document d=new Document(r.getPageSize(1));PdfWriter w=PdfWriter.getInstance(d,out);d.addTitle("Setcards · "+cliente+" · "+evento);d.open();
            for(int i=0;i<perfiles.size()+3;i++){if(i>0)d.newPage();
                PdfContentByte cb=w.getDirectContent();int origen=i==0?1:i==1?2:i==perfiles.size()+2?4:3;
                cb.addTemplate(w.getImportedPage(r,origen),0,0);
                if(origen==2)caja(cb,cliente+"   ·   "+evento,105,91,566,62,17);
                if(origen==3){Perfil p=perfiles.get(i-2);caja(cb,p.nombre,35,420,555,75,32);caja(cb,p.datos,35,55,296,345,17);
                    int[][] marcos={{349,50,185,362},{541,234,184,178},{541,50,184,177}};
                    cb.saveState();
                    for(int j=0;j<3;j++){int[] m=marcos[j];cb.setColorFill(BaseColor.WHITE);cb.setColorStroke(new BaseColor(215,201,211));cb.rectangle(m[0],m[1],m[2],m[3]);cb.fillStroke();Image img=Image.getInstance(p.fotos.get(j));img.scaleToFit(m[2]-8,m[3]-8);img.setAbsolutePosition(m[0]+(m[2]-img.getScaledWidth())/2,m[1]+(m[3]-img.getScaledHeight())/2);cb.addImage(img);}
                    cb.restoreState();
                }
            }d.close();
        }finally{r.close();}});
    }
}
