import java.awt.Desktop;
import java.awt.Taskbar;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.file.*;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;
import java.util.zip.*;

/** Lanzadores de producción y prueba: no cambian la base de la instalación existente. */
public final class Oficina {
    static final boolean MAC=System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac");
    static Path datos(boolean demo){return Path.of(System.getProperty("user.home"),MAC?"Library/Application Support":"",MAC?(demo?"PonPox-Prueba":"PonPox"):(demo?".ponpox-prueba":".ponpox"));}
    static Connection db(Path p)throws Exception{return DriverManager.getConnection("jdbc:sqlite:"+p.toAbsolutePath());}
    static void limpiar(Path p)throws IOException{if(!Files.exists(p))return;try(var w=Files.walk(p)){for(Path f:w.sorted(Comparator.reverseOrder()).toArray(Path[]::new))Files.delete(f);}}
    static boolean tabla(Connection c,String t)throws SQLException{try(var s=c.prepareStatement("select 1 from sqlite_master where type='table' and name=?")){s.setString(1,t);return s.executeQuery().next();}}
    static Cipher cipher(int modo,char[] clave,byte[] salt,byte[] iv)throws Exception{
        PBEKeySpec spec=new PBEKeySpec(clave,salt,310000,256);
        try{var k=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(modo,new SecretKeySpec(k.getEncoded(),"AES"),new GCMParameterSpec(128,iv));return c;}finally{spec.clearPassword();}
    }
    static void entry(ZipOutputStream out,String n,Path f)throws IOException{out.putNextEntry(new ZipEntry(n));Files.copy(f,out);out.closeEntry();}
    static void exportar(Path origen,Path out,char[] clave)throws Exception{
        if(!Files.isRegularFile(origen)||Files.exists(out))throw new IOException("Falta la base de origen o ya existe el archivo de destino.");
        Path tmp=Files.createTempDirectory("ponpox-envio-"),snap=tmp.resolve("ponpox.db"),enc=Files.createTempFile(out.toAbsolutePath().getParent(),".ponpox-envio-",".tmp");
        try{
            try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+origen.toUri()+"?mode=ro");Statement s=c.createStatement()){
                if(!tabla(c,"personal"))throw new IOException("La base seleccionada no es de PonPox.");
                s.execute("VACUUM INTO '"+snap.toString().replace("'","''")+"'");
            }
            Map<String,Path> fotos=new LinkedHashMap<>();
            try(Connection c=db(snap)){if(tabla(c,"personal_fotos")){
                try(var s=c.createStatement();var r=s.executeQuery("select personal_id,posicion,ruta from personal_fotos where coalesce(ruta,'')<>''")){
                    while(r.next()){Path src=Path.of(r.getString(3));if(!Files.isRegularFile(src))throw new IOException("Falta una foto: "+src+". Revisa la ficha antes de compartir.");
                        String n="fotos/"+r.getInt(1)+"_"+r.getInt(2)+"."+ext(src);fotos.put(n,src);}
                }
                try(var u=c.prepareStatement("update personal_fotos set ruta=? where personal_id=? and posicion=?")){
                    for(String n:fotos.keySet()){String[] ids=n.substring(6,n.lastIndexOf('.')).split("_");u.setString(1,n);u.setInt(2,Integer.parseInt(ids[0]));u.setInt(3,Integer.parseInt(ids[1]));u.addBatch();}u.executeBatch();}
            }}
            byte[] salt=new byte[16],iv=new byte[12];SecureRandom rng=new SecureRandom();rng.nextBytes(salt);rng.nextBytes(iv);
            try(OutputStream raw=Files.newOutputStream(enc)){raw.write(new byte[]{80,80,88,49});raw.write(salt);raw.write(iv);
                try(ZipOutputStream z=new ZipOutputStream(new CipherOutputStream(raw,cipher(Cipher.ENCRYPT_MODE,clave,salt,iv)))){entry(z,"ponpox.db",snap);for(var e:fotos.entrySet())entry(z,e.getKey(),e.getValue());}}
            Files.move(enc,out);
        }finally{Files.deleteIfExists(enc);limpiar(tmp);}
    }
    static String ext(Path p){String n=p.getFileName().toString();int i=n.lastIndexOf('.');String x=i<0?"":n.substring(i+1).toLowerCase(Locale.ROOT);return x.matches("[a-z0-9]{1,8}")?x:"img";}
    static void importar(Path envio,Path dest,char[] clave)throws Exception{
        if(Files.exists(dest))throw new IOException("Ya existe la carpeta de prueba. No se ha sobrescrito.");
        Files.createDirectories(dest.getParent());Path tmp=Files.createTempDirectory(dest.getParent(),".ponpox-prueba-"),zip=tmp.resolve("copia.zip");
        try{try(InputStream raw=Files.newInputStream(envio)){
                if(!Arrays.equals(raw.readNBytes(4),new byte[]{80,80,88,49}))throw new IOException("Elige el archivo .ppx generado en el Mac de Enrique.");
                try(var dec=new CipherInputStream(raw,cipher(Cipher.DECRYPT_MODE,clave,raw.readNBytes(16),raw.readNBytes(12)));var out=Files.newOutputStream(zip)){dec.transferTo(out);}
            }
            try(ZipInputStream z=new ZipInputStream(Files.newInputStream(zip))){ZipEntry e;while((e=z.getNextEntry())!=null){String n=e.getName();if(!n.equals("ponpox.db")&&!n.matches("fotos/[0-9]+_[0-9]+[.][a-z0-9]{1,8}"))throw new IOException("Archivo de datos no válido.");Path f=tmp.resolve(n);Files.createDirectories(f.getParent());Files.copy(z,f);}}
            Files.delete(zip);Path base=tmp.resolve("ponpox.db");if(!Files.isRegularFile(base))throw new IOException("No se encuentra la base en el envío.");
            try(Connection c=db(base);var s=c.createStatement()){if(!tabla(c,"personal")||!s.executeQuery("pragma quick_check").getString(1).equals("ok"))throw new IOException("Copia no válida.");
                if(tabla(c,"personal_fotos"))try(var r=s.executeQuery("select ruta from personal_fotos where coalesce(ruta,'')<>''");var u=c.prepareStatement("update personal_fotos set ruta=? where ruta=?")){
                    while(r.next()){String n=r.getString(1);if(!n.matches("fotos/[0-9]+_[0-9]+[.][a-z0-9]{1,8}")||!Files.isRegularFile(tmp.resolve(n)))throw new IOException("Fotografía no encontrada en el envío.");u.setString(1,dest.resolve(n).toAbsolutePath().toString());u.setString(2,n);u.addBatch();}r.close();u.executeBatch();}
            }
            Files.move(tmp,dest);
        }finally{limpiar(tmp);}
    }
    static char[] clave(boolean crear){
        JPasswordField a=new JPasswordField(24),b=new JPasswordField(24);
        if(JOptionPane.showConfirmDialog(null,crear?new Object[]{"Contraseña para el envío (12 caracteres o más)",a,"Repítela",b}:new Object[]{"Contraseña del envío",a},"PonPox · Copia de oficina",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return null;
        char[] k=a.getPassword();if(crear&&(k.length<12||!Arrays.equals(k,b.getPassword())))throw new IllegalArgumentException("Las contraseñas deben coincidir y tener al menos 12 caracteres.");return k;
    }
    public static void main(String[] args){
        try{
           try{if(Taskbar.isTaskbarSupported())Taskbar.getTaskbar().setIconImage(ImageIO.read(new File("Logotipo PonPox.png")));}catch(Exception ignored){}
           String modo=args.length==0?"prueba":args[0];
           if(modo.equals("exportar")){
               Path origen=datos(false).resolve("ponpox.db");
               JFileChooser s=new JFileChooser(origen.toFile().getParentFile());s.setSelectedFile(origen.toFile());s.setDialogTitle("Confirma la base ACTUAL de PonPox");if(s.showOpenDialog(null)!=JFileChooser.APPROVE_OPTION)return;origen=s.getSelectedFile().toPath();
               String resumen;try(var c=DriverManager.getConnection("jdbc:sqlite:"+origen.toUri()+"?mode=ro");var st=c.createStatement()){StringJoiner j=new StringJoiner(" · ");for(String t:new String[]{"personal","clientes","presupuestos"})if(tabla(c,t))j.add(t+": "+st.executeQuery("select count(*) from "+t).getInt(1));resumen=j.toString();}
               if(JOptionPane.showConfirmDialog(null,"Origen: "+origen+"\n"+resumen+"\nLa copia incluirá datos personales, fotos y PDF. ¿Continuar?","Confirma la copia de prueba",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
               Path f;if(args.length>1)f=Path.of(args[1]);else{s=new JFileChooser();s.setSelectedFile(new File("PonPox-Datos-Prueba.ppx"));if(s.showSaveDialog(null)!=JFileChooser.APPROVE_OPTION)return;f=s.getSelectedFile().toPath();if(!f.toString().endsWith(".ppx"))f=Path.of(f+".ppx");}
               char[] k=clave(true);if(k==null)return;try{exportar(origen,f,k);}finally{Arrays.fill(k,' ');}JOptionPane.showMessageDialog(null,"Copia creada: "+f+"\nComunica la contraseña por otro canal.");return;
           }
           if(modo.equals("prueba")){
               System.setProperty("ponpox.prueba","true");Path dest=datos(true);
               if(!Files.isRegularFile(dest.resolve("ponpox.db"))){JFileChooser s=new JFileChooser();s.setDialogTitle("Primera instalación: selecciona los datos de prueba .ppx");if(s.showOpenDialog(null)!=JFileChooser.APPROVE_OPTION)return;
                   char[] k=clave(false);if(k==null)return;try{importar(s.getSelectedFile().toPath(),dest,k);}finally{Arrays.fill(k,' ');}}
           }else if(!modo.equals("principal"))throw new IOException("Modo desconocido.");
           else if(!Files.isRegularFile(datos(false).resolve("ponpox.db")))throw new IOException("No encuentro tu base habitual en "+datos(false)+". No se ha creado una base vacía.");
           PonPox.main(new String[0]);
        }catch(Exception e){JOptionPane.showMessageDialog(null,"No se pudo completar la operación.\n"+e.getMessage(),"PonPox",JOptionPane.ERROR_MESSAGE);}
    }
}
