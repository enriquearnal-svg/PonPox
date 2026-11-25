import java.io.*;
import java.nio.file.*;
import java.sql.*;
import javax.swing.filechooser.FileSystemView;
final class Carpetas{
 static final String T="Trabajadores",C="Clientes",P="Producción";
 static File carpeta(String area){
  File docs=new File(System.getProperty("user.home"));
  File proyecto=new File(docs,Boolean.getBoolean("ponpox.prueba")?"PonPox-Prueba":"PonPox");
  File interior=new File(proyecto,"PonPox");
  if(!Boolean.getBoolean("ponpox.prueba") && new File(interior,"src/PonPox.java").isFile())proyecto=interior;
  File f=new File(new File(proyecto,"Informes"),area);
  if(!f.isDirectory()&&!f.mkdirs()&&!f.isDirectory())throw new IllegalStateException("No se puede crear "+f);return f;
 }
 static void sincronizar(String url)throws Exception{
  carpeta(T);carpeta(P);carpeta(C);
  try(Connection c=DriverManager.getConnection(url);Statement s=c.createStatement()){
   if(!Oficina.tabla(c,"pdf_presupuesto"))return;
   try(ResultSet r=s.executeQuery("select d.id,p.cliente,p.numero from pdf_presupuesto d join presupuestos p on p.id=d.presupuesto_id");PreparedStatement b=c.prepareStatement("select contenido from pdf_presupuesto where id=?")){
    while(r.next()){int id=r.getInt(1);Path dir=cliente(r.getString(2),"Presupuestos importados");Files.createDirectories(dir);
     Path f=dir.resolve("Presupuesto - "+seguro(r.getString(3))+" - "+id+".pdf");if(Files.exists(f))continue;
     b.setInt(1,id);try(ResultSet v=b.executeQuery()){if(v.next()){Path temp=Files.createTempFile(dir,".original-",".tmp");try(InputStream in=v.getBinaryStream(1)){Files.copy(in,temp,StandardCopyOption.REPLACE_EXISTING);Files.move(temp,f);}finally{Files.deleteIfExists(temp);}}}
    }
   }
  }
 }
 static void recuperarAnteriores() throws IOException {
   File docs=Oficina.MAC?new File(System.getProperty("user.home"),"Documents"):FileSystemView.getFileSystemView().getDefaultDirectory();
   Path origen=new File(new File(docs,Boolean.getBoolean("ponpox.prueba")?"PonPox-Prueba":"PonPox"),"Informes").toPath();
   Path raiz=carpeta(T).toPath().getParent();
   if(!Boolean.getBoolean("ponpox.prueba"))migrar(Path.of(System.getProperty("user.home"),"PonPox","Informes"),raiz,".v31-exterior");
   migrar(origen,raiz,".migracion-v29-completa");
 }
 static void migrar(Path origen,Path raiz,String marcador)throws IOException{
   Path marca=raiz.resolve(marcador);
   if(!Files.isDirectory(origen,LinkOption.NOFOLLOW_LINKS)||Files.isSameFile(origen,raiz)||Files.exists(marca))return;
   try(var stream=Files.walk(origen)){
     var iter=stream.iterator();while(iter.hasNext()){
       Path p=iter.next(),d=raiz.resolve(origen.relativize(p));
       if(Files.isSymbolicLink(p))continue;
       if(Files.isSymbolicLink(d))throw new IOException("Revisa el enlace en "+d);
       if(Files.isDirectory(p))Files.createDirectories(d);
       else if(Files.isRegularFile(p))copia(p,d.getParent());
     }
   }
   Files.createFile(marca);
 }
 static String seguro(String x){String n=x==null?"":x.replaceAll("[^\\p{L}\\p{N} _-]","_").trim();return n.isEmpty()?"Sin determinar":n.substring(0,Math.min(80,n.length())).trim();}
 static String sugerir(String n,String a){
    n=n.trim();a=a==null?"":a.trim();if(a.isEmpty()){String[] partes=n.split("\\s+");int pos=Math.max(1,partes.length-2);if(pos<partes.length){a=partes[pos];n=String.join(" ",java.util.Arrays.copyOf(partes,pos));}}
    return n+(a.isEmpty()?"":" "+a.substring(0,1).toUpperCase(java.util.Locale.ROOT));
 }
 static Path personal(String url,int id)throws Exception{
    Path raiz=carpeta(T).toPath();
    try(Connection c=DriverManager.getConnection(url);Statement s=c.createStatement()){
      s.execute("create table if not exists carpetas_personal(id integer primary key, alias text not null)");
      String alias=null;
      try(var q=c.prepareStatement("select alias from carpetas_personal where id=?")){q.setInt(1,id);try(var r=q.executeQuery()){if(r.next())alias=r.getString(1);}}
      if(alias==null){
        try(var q=c.prepareStatement("select nombre,apellido1 from personal where id=?")){q.setInt(1,id);try(var r=q.executeQuery()){if(!r.next())throw new IOException("Trabajador no encontrado.");alias=sugerir(r.getString(1),r.getString(2));}}
        String valor=javax.swing.JOptionPane.showInputDialog(null,"Confirma el nombre y la inicial del primer apellido para la carpeta:",alias);
        if(valor==null||valor.isBlank())throw new IOException("No se ha creado la carpeta personal.");alias=seguro(valor);
        try(var q=c.prepareStatement("insert into carpetas_personal values(?,?)")){q.setInt(1,id);q.setString(2,alias);q.executeUpdate();}
      }
      Path dir=raiz.resolve(seguro(alias));Path marca=dir.resolve(".ponpox-id");
      if(Files.exists(dir)&&(!Files.isRegularFile(marca)||!Files.readString(marca).equals(""+id))){
        dir=raiz.resolve("Coincidencias").resolve(""+id).resolve(seguro(alias));marca=dir.resolve(".ponpox-id");
      }
      Files.createDirectories(dir);if(!Files.exists(marca))Files.writeString(marca,""+id,StandardOpenOption.CREATE_NEW);
      for(String sub:new String[]{"Fotos","Reportes","Informes"})Files.createDirectories(dir.resolve(sub));
      try(var q=c.prepareStatement("select posicion,ruta from personal_fotos where personal_id=?")){q.setInt(1,id);try(var r=q.executeQuery()){while(r.next()){
       String v=r.getString(2);if(v==null||v.isBlank())continue;Path src=Path.of(v);if(!Files.isRegularFile(src))continue;
       Path foto=dir.resolve("Fotos").resolve("Foto-"+(r.getInt(1)+1)+"."+Oficina.ext(src));if(!Files.exists(foto)||Files.mismatch(src,foto)!=-1)Files.copy(src,foto,StandardCopyOption.REPLACE_EXISTING);
      }}}
      return dir;
    }
 }
 static void copia(Path documento,Path directorio)throws IOException{
    Path origen=documento.toAbsolutePath(),dest=directorio.resolve(origen.getFileName());
    if(dest.toAbsolutePath().equals(origen))return;
    for(int i=2;Files.exists(dest);i++){if(Files.mismatch(origen,dest)==-1)return;String n=origen.getFileName().toString();int j=n.lastIndexOf('.');dest=directorio.resolve((j<0?n:n.substring(0,j))+" ("+i+")"+(j<0?"":n.substring(j)));}
    Files.copy(origen,dest);
 }
 static Path cliente(String nombre,String sub)throws IOException{
    String n=seguro(nombre);if(n.matches("(?i)CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]"))n="_"+n;
    return Files.createDirectories(carpeta(C).toPath().resolve(n).resolve(sub));
 }
}
