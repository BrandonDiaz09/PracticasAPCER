package Servidor;

import java.io.*;
import java.net.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Servidor {
    private static final int BUFFER_SIZE = 4096;
    private static String rutaCRemota = "./CarpetaRemota";

    public static void main(String[] args)throws Exception{
        try{
            ServerSocket s = new ServerSocket(1234);
            s.setReuseAddress(true);
            System.out.println("¡Servidor iniciado! \n");
            System.out.println("Esperando cliente para su conexión ...");
            int peticionCliente;

            for(;;){
                Socket cliente = s.accept();
                DataInputStream DataInputStream = new DataInputStream(cliente.getInputStream());
                ObjectOutputStream ObjectOutputStream = new ObjectOutputStream(cliente.getOutputStream());
                DataOutputStream DataOutputStream = new DataOutputStream(cliente.getOutputStream());
                ObjectInputStream ObjectInputStream = new ObjectInputStream(cliente.getInputStream());
                System.out.println("Cliente conectado");
                do {
                    peticionCliente = DataInputStream.readInt();

                    switch (peticionCliente) {
                        case 1:
                            System.out.println("peticion list");
                            String response = "remote: " + rutaCRemota + ":\n" + listar(rutaCRemota, 0);
                            DataOutputStream.writeUTF(response);
                            DataOutputStream.flush();
                            System.out.println("fin de peticion");
                            break;
                        case 2:
                            System.out.println("peticion delete");
                            String nombreArchivo = DataInputStream.readUTF();
                            if (eliminarArchivo(rutaCRemota + '/' + nombreArchivo)) {
                                System.out.println(rutaCRemota+'/'+nombreArchivo+ " ha sido eliminado con exito.");
                                System.out.println("fin de peticion");
                                DataOutputStream.writeInt(1);
                                DataOutputStream.flush();
                            } else {
                                System.out.println("No se pudo eliminar");
                                System.out.println("fin de peticion");
                                DataOutputStream.writeInt(0);
                                DataOutputStream.flush();
                            }
                            break;
                        case 3:
                            int file_folder = DataInputStream.readInt();
                            if(file_folder == 1) {
                                System.out.println("peticion recibir");
                                String archivoRecibido = DataInputStream.readUTF();
                                System.out.println("creando archivo");
                                FileOutputStream f = new FileOutputStream(rutaCRemota + '/' + archivoRecibido);
                                byte[] bufferInput = new byte[1024];
                                int bytesLeidos;
                                System.out.println("leyendo bytes");
                                while ((bytesLeidos = cliente.getInputStream().read(bufferInput)) != -1) {
                                    f.write(bufferInput, 0, bytesLeidos);
                                    if(bytesLeidos < 1024)
                                        break;
                                }
                                f.close();
                                System.out.println("fin de peticion");
                                DataOutputStream.writeInt(1);
                                DataOutputStream.flush();
                            }else if(file_folder == 2) {
                                System.out.println("peticion recibir");
                                String archivoRecibido = DataInputStream.readUTF();
                                String name = DataInputStream.readUTF();
                                System.out.println("creando archivo");
                                FileOutputStream f = new FileOutputStream(rutaCRemota + '/' + archivoRecibido);
                                byte[] bufferInput = new byte[1024];
                                int bytesLeidos;
                                System.out.println("leyendo bytes");
                                while ((bytesLeidos = cliente.getInputStream().read(bufferInput)) != -1) {
                                    f.write(bufferInput, 0, bytesLeidos);
                                    if(bytesLeidos < 1024)
                                        break;
                                }
                                f.close();
                                System.out.println("descomprimiendo");
                                File folder = new File(rutaCRemota + '/' + name);
                                folder.mkdir();
                                unzip(rutaCRemota + '/' + archivoRecibido, rutaCRemota);
                                eliminarArchivo(rutaCRemota + '/' + archivoRecibido);
                                System.out.println("fin de peticion");
                                DataOutputStream.writeInt(1);
                                DataOutputStream.flush();
                            }

                            break;
                        case 4:
                            System.out.println("peticion enviar");
                            String nombre = DataInputStream.readUTF();
                            File archivoLocal = new File(rutaCRemota + '/' + nombre);
                            if(archivoLocal.isFile()) {
                                DataOutputStream.writeInt(1);
                                DataOutputStream.flush();
                                System.out.println("abriendo archivo");
                                FileInputStream fi = new FileInputStream(archivoLocal);
                                BufferedInputStream bi = new BufferedInputStream(fi);
                                byte[] bufferOutput = new byte[1024];
                                int bytesL;
                                System.out.println("Enviando bytes");
                                while ((bytesL = bi.read(bufferOutput)) != -1) {
                                    cliente.getOutputStream().write(bufferOutput, 0, bytesL);
                                    if (bytesL < 1024)
                                        break;
                                }
                                fi.close();
                                bi.close();
                                System.out.println("fin de peticion");
                                DataOutputStream.writeInt(1);
                                DataOutputStream.flush();
                            }else if(archivoLocal.isDirectory()) {
                                try (FileOutputStream fos = new FileOutputStream(rutaCRemota + '/' + nombre + ".zip");
                                     ZipOutputStream zipOut = new ZipOutputStream(fos)) {
                                    zipFile(archivoLocal, archivoLocal.getName(), zipOut);
                                }
                                DataOutputStream.writeInt(2);
                                DataOutputStream.flush();
                                DataOutputStream.writeUTF(nombre + ".zip");
                                DataOutputStream.flush();
                                DataOutputStream.writeUTF(nombre);
                                DataOutputStream.flush();
                                System.out.println("abriendo carpeta");
                                File archivoLocalzip = new File(rutaCRemota + '/' + nombre+".zip");
                                FileInputStream fi = new FileInputStream(archivoLocalzip);
                                BufferedInputStream bi = new BufferedInputStream(fi);
                                byte[] bufferOutput = new byte[1024];
                                int bytesL;
                                System.out.println("Enviando bytes");
                                while ((bytesL = bi.read(bufferOutput)) != -1) {
                                    cliente.getOutputStream().write(bufferOutput, 0, bytesL);
                                    if (bytesL < 1024)
                                        break;
                                }
                                fi.close();
                                bi.close();
                                System.out.println("fin de peticion");
                                eliminarArchivo(rutaCRemota + '/' + nombre + ".zip");
                                DataOutputStream.writeInt(1);
                                DataOutputStream.flush();
                            }
                            break;
                        case 5:
                            System.out.println("peticion change");
                            String nuevaRutaRemota = DataInputStream.readUTF();
                            File nuevoDirectorio = new File(rutaCRemota + '/' + nuevaRutaRemota);
                            if (nuevoDirectorio.exists() && nuevoDirectorio.isDirectory()) {
                                if (nuevoDirectorio.canWrite()) {
                                    System.out.println("Cambiando el directorio a: " + nuevoDirectorio.getAbsolutePath());
                                    System.setProperty("user.dir", nuevoDirectorio.getAbsolutePath());
                                    rutaCRemota = nuevoDirectorio.getAbsolutePath();
                                    System.out.println("fin de peticion");
                                    DataOutputStream.writeInt(1);
                                    DataOutputStream.flush();
                                } //if --anidado
                                else {
                                    System.out.println("Error: Permisos insuficientes para cambiar el directorio");
                                    System.out.println("fin de peticion");
                                    DataOutputStream.writeInt(0);
                                    DataOutputStream.flush();
                                }//else --anidado
                            }//if
                            else {
                                System.out.println("Error: El directorio no existe o no es un directorio");
                                System.out.println("fin de peticion");
                                DataOutputStream.writeInt(0);
                                DataOutputStream.flush();
                            }  //else

                            break;
                        case 6:
                            System.out.println("peticion crear");
                            String nameNewFile = DataInputStream.readUTF();
                            File NewFile = new File(rutaCRemota + '/' + nameNewFile);
                            if (NewFile.mkdir()) {
                                System.out.println("Directorio creado exitosamente");
                                System.out.println("fin de peticion");
                                DataOutputStream.writeInt(1);
                                DataOutputStream.flush();
                            }//if
                            else {
                                System.out.println("¡ERROR!");
                                System.out.println("fin de peticion");
                                DataOutputStream.writeInt(0);
                                DataOutputStream.flush();
                            }//else
                            break;
                        default:
                            System.out.println("Opcion no valida");
                            break;
                    }
                }while (peticionCliente != 7);//switch
            }//for
        }//try
        catch(Exception e){
            e.printStackTrace();

        }//catch
    }//Main

    private static boolean eliminarArchivo(String ruta) {
        File archivo = new File(ruta);
        boolean flag = false;
        if(archivo.exists()) {
            if(archivo.isFile()) {
                try {
                    System.out.println(archivo.getName() + " eliminado");
                    archivo.delete();
                    flag = true;
                }catch (Exception e) {
                    System.out.println("No se pudo eliminar");
                }
            }else if(archivo.isDirectory()) {
                if(archivo.list().length > 0) {
                    File[] archivos = archivo.listFiles();
                    for (int i = 0; i < archivos.length; i++) {
                        if(archivos[i].isFile()) {
                            try{
                                System.out.println(archivos[i].getName() + " eliminado");
                                archivos[i].delete();
                            }catch (Exception er) {
                                System.out.println("No se pudo eliminar");
                            }
                        } else if (archivos[i].isDirectory()) {
                            eliminarArchivo(archivos[i].getAbsolutePath());
                        }

                    }
                }
                archivo.delete();
                flag = true;
            }
        }
        return flag;
    }

    private static String listar(String root, int tab) {
        File archivo = new File(root);
        File[] afiles = archivo.listFiles();
        String buffer = "";
        if(archivo.list().length > 0) {
            for (File f : afiles) {
                for (int i = 0; i < tab; i++) {
                    buffer = buffer + "\t";
                }
                buffer = buffer + f.getName() + "\n";
                if (f.isDirectory()) {
                    buffer = buffer + listar(root + '/' + f.getName(), tab + 1);
                }
            }
        }
        return buffer;
    }

    public static void unzip(String zipFilePath, String destFilePath) throws IOException {
        File destinationDirectory = new File(destFilePath);
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdir();
        }

        FileInputStream fileInputStream = new FileInputStream(zipFilePath);
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
        ZipEntry zipEntry = zipInputStream.getNextEntry();

        while (zipEntry != null) {
            System.out.println(zipEntry.getName());
            String filePath = destFilePath + '/' + zipEntry.getName();
            if (!zipEntry.isDirectory()) {
                extractFile(zipInputStream, filePath);
            } else {
                File directory = new File(filePath);
                directory.mkdirs();
            }
            zipInputStream.closeEntry();
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
    }

    private static void extractFile(ZipInputStream zipInputStream, String filePath) throws IOException {
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytes = new byte[BUFFER_SIZE];
        int readByte;
        while ((readByte = zipInputStream.read(bytes)) != -1) {
            bufferedOutputStream.write(bytes, 0, readByte);
        }
        bufferedOutputStream.close();
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }

        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }
}//Servidor