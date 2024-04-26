package Cliente;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Cliente {
    private static final int BUFFER_SIZE = 4096;
    private static DataOutputStream DataOutputStream;
    private static DataInputStream DataInputStream;
    private static ObjectOutputStream ObjectOutputStream;
    private static ObjectInputStream ObjectInputStream;
    private static String rutaCLocal = "./CarpetaLocal";

    private static String rutaCRemote = "./CarpetaRemota";

    private static Socket cli;
    private static int status;

    public Cliente(String direccion, int puerto) {
        try {
            cli = new Socket(direccion, puerto);
            System.out.println("Conexión con servidor establecida...");
            initStream(cli);
            rutaCLocal = "./CarpetaLocal";
            rutaCRemote = "./CarpetaRemota";

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args)throws Exception {
        try {

            int puerto = 1234;
            String direccion = "127.0.0.1";
            Cliente cliente = new Cliente(direccion, puerto);

            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new InterfazGrafica(cliente).setVisible(true);
                }
            });
            /*String direccion = "127.0.0.1";

            cliente = new Socket(direccion, 1234);
            System.out.println("Conectado con servidor");
            initStream(cliente);
            int opc, status;
            String nombreArchivo;
            do {
                System.out.println("Cliente FTP");
                System.out.println("1. Listar archivos. \n2. Eliminar archivos/carpetas. \n3. Enviar archivos. \n4. Solicitar archivos. \n5. Cambiar directorio.\n6. Crear carpeta.\n7. Salir");
                System.out.println("Que desea hacer: ");
                Scanner scanner = new Scanner(System.in);
                opc = scanner.nextInt();
                int chc;
                switch (opc) {
                    case 1:
                        System.out.println("\n1.Local\n2.Remoto\nDonde? ");
                        chc = scanner.nextInt();
                        if(chc == 1) {
                            String list = listarLocal(rutaCLocal, 0);
                            System.out.println(rutaCLocal + ":\n" +list);
                        } else if (chc == 2) {
                            listarRemoto();
                        }
                        break;
                    case 2:
                        System.out.println("\n1.Local\n2.Remoto\nDonde? ");
                        chc = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println("Ingrese el nombre del archivo/carpeta a eliminar: ");
                        nombreArchivo = scanner.nextLine();
                        if(chc == 1) {
                            if(eliminarArchivo(rutaCLocal + '/' + nombreArchivo)){
                                System.out.println("Directorio eliminado con exito");
                            }else{
                                System.out.println("Error al eliminar el directorio");
                            }
                        } else if (chc == 2) {
                            eliminarRemoto(nombreArchivo);
                            status = DataInputStream.readInt();
                            if(status == 1)
                                System.out.println("Directorio eliminado con exito");
                            else
                                System.out.println("Error al eliminar el directorio");
                        }

                        break;
                    case 3:
                        DataOutputStream.writeInt(3);
                        DataOutputStream.flush();
                        scanner.nextLine();
                        System.out.println("Ingrese el nombre del archivo a mandar: ");
                        nombreArchivo = scanner.nextLine();
                        File aux = new File(rutaCLocal + '/' + nombreArchivo);
                        if(aux.isFile()) {
                            DataOutputStream.writeInt(1);
                            DataOutputStream.flush();
                            DataOutputStream.writeUTF(nombreArchivo);
                            DataOutputStream.flush();
                            File archivoLocal = new File(rutaCLocal + '/' + nombreArchivo);
                            FileInputStream fi = new FileInputStream(archivoLocal);
                            BufferedInputStream bi = new BufferedInputStream(fi);
                            byte[] bufferOutput = new byte[1024];
                            int bytesL;
                            while ((bytesL = bi.read(bufferOutput)) != -1) {
                                cliente.getOutputStream().write(bufferOutput, 0, bytesL);
                                if (bytesL < 1024)
                                    break;
                            }
                            fi.close();
                            bi.close();
                            status = DataInputStream.readInt();
                            if (status == 1)
                                System.out.println("Archivo mandado con exito");
                            else
                                System.out.println("Error al mandar al archivo");
                        } else if (aux.isDirectory()) {
                            try (FileOutputStream fos = new FileOutputStream(rutaCLocal + '/' + nombreArchivo + ".zip");
                                 ZipOutputStream zipOut = new ZipOutputStream(fos)) {
                                zipFile(aux, aux.getName(), zipOut);
                            }
                            DataOutputStream.writeInt(2);
                            DataOutputStream.flush();
                            DataOutputStream.writeUTF(nombreArchivo + ".zip");
                            DataOutputStream.flush();
                            DataOutputStream.writeUTF(nombreArchivo);
                            DataOutputStream.flush();
                            File archivoLocal = new File(rutaCLocal + '/' + nombreArchivo+".zip");
                            FileInputStream fi = new FileInputStream(archivoLocal);
                            BufferedInputStream bi = new BufferedInputStream(fi);
                            byte[] bufferOutput = new byte[1024];
                            int bytesL;
                            while ((bytesL = bi.read(bufferOutput)) != -1) {
                                cliente.getOutputStream().write(bufferOutput, 0, bytesL);
                                if (bytesL < 1024)
                                    break;
                            }
                            fi.close();
                            bi.close();
                            eliminarArchivo(rutaCLocal + '/' + nombreArchivo + ".zip");
                            status = DataInputStream.readInt();
                            if (status == 1)
                                System.out.println("Archivo mandado con exito");
                            else
                                System.out.println("Error al mandar al archivo");
                        }
                        break;
                    case 4:
                        DataOutputStream.writeInt(4);
                        DataOutputStream.flush();
                        scanner.nextLine();
                        System.out.println("Ingrese el nombre del archivo a solicitar: ");
                        nombreArchivo = scanner.nextLine();
                        DataOutputStream.writeUTF(nombreArchivo);
                        DataOutputStream.flush();
                        int opc5 = DataInputStream.readInt();
                        if(opc5 == 1) {
                            FileOutputStream f = new FileOutputStream(rutaCLocal + '/' + nombreArchivo);
                            byte[] bufferInput = new byte[1024];
                            int bytesLeidos;
                            while ((bytesLeidos = cliente.getInputStream().read(bufferInput)) != -1) {
                                f.write(bufferInput, 0, bytesLeidos);
                                if(bytesLeidos < 1024)
                                    break;
                            }
                            f.close();
                        } else if (opc5 == 2) {
                            String archivoRecibido = DataInputStream.readUTF();
                            String name = DataInputStream.readUTF();
                            FileOutputStream f = new FileOutputStream(rutaCLocal + '/' + archivoRecibido);
                            byte[] bufferInput = new byte[1024];
                            int bytesLeidos;
                            while ((bytesLeidos = cliente.getInputStream().read(bufferInput)) != -1) {
                                f.write(bufferInput, 0, bytesLeidos);
                                if(bytesLeidos < 1024)
                                    break;
                            }
                            f.close();
                            File folder = new File(rutaCLocal + '/' + name);
                            folder.mkdir();
                            unzip(rutaCLocal + '/' + archivoRecibido, rutaCLocal);
                            eliminarArchivo(rutaCLocal + '/' + archivoRecibido);
                        }
                        status = DataInputStream.readInt();
                        if(status == 1)
                            System.out.println("Archivo recivido con exito");
                        else
                            System.out.println("Error al solicitar el archivo");
                        break;
                    case 5:
                        System.out.println("\n1.Local\n2.Remoto\nDonde? ");
                        chc = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println("Ingrese el nombre de la carpeta: ");
                        String carpeta = scanner.nextLine();
                        if(chc == 1) {
                            ChangeFileBaseLocal(rutaCLocal + '/' + carpeta);
                        } else if (chc == 2) {
                            ChangeFileBaseRemote(carpeta);
                            status = DataInputStream.readInt();
                            if(status == 1)
                                System.out.println("Directorio cambiado con exito");
                            else
                                System.out.println("Error al cambiar el directorio");
                        }
                        break;
                    case 6:
                        System.out.println("\n1.Local\n2.Remoto\nDonde? ");
                        chc = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println("Ingrese el nombre de la carpeta: ");
                        nombreArchivo = scanner.nextLine();
                        if(chc == 1) {
                            NewFolderLocal(nombreArchivo);
                        } else if (chc == 2) {
                            NewFolderInRemote(nombreArchivo);
                            status = DataInputStream.readInt();
                            if(status == 1)
                                System.out.println("Directorio creado con exito");
                            else
                                System.out.println("Error al crear el directorio");
                        }
                        break;
                    case 7:
                        System.out.println("Hasta pronto");
                        finishConection(cliente);
                        break;
                    default:
                        System.out.println("Opcion no valida");
                        break;
                }
                scanner.nextLine();
            }while (opc < 7);*/

        }//try
        catch (Exception e) {
            e.printStackTrace();
        }//catch
    }//main

    public static String getRutaCLocal() {
        return rutaCLocal;
    }

    public static String getRutaCRemote() {
        return rutaCRemote;
    }

    public static void enviarARemoto(String nombreArchivo, String ruta){
        try {
            DataOutputStream.writeInt(3);
            DataOutputStream.flush();
            File aux = new File(ruta);
            if(aux.isFile()) {
                DataOutputStream.writeInt(1);
                DataOutputStream.flush();
                DataOutputStream.writeUTF(nombreArchivo);
                DataOutputStream.flush();
                File archivoLocal = new File(ruta);
                FileInputStream fi = new FileInputStream(archivoLocal);
                BufferedInputStream bi = new BufferedInputStream(fi);
                byte[] bufferOutput = new byte[1024];
                int bytesL;
                while ((bytesL = bi.read(bufferOutput)) != -1) {
                    cli.getOutputStream().write(bufferOutput, 0, bytesL);
                    if (bytesL < 1024)
                        break;
                }
                fi.close();
                bi.close();
                status = DataInputStream.readInt();
                if (status == 1)
                    System.out.println("Archivo mandado con exito");
                else
                    System.out.println("Error al mandar al archivo");
            } else if (aux.isDirectory()) {
                try (FileOutputStream fos = new FileOutputStream(rutaCLocal + '/' + nombreArchivo + ".zip");
                     ZipOutputStream zipOut = new ZipOutputStream(fos)) {
                    zipFile(aux, aux.getName(), zipOut);
                }
                DataOutputStream.writeInt(2);
                DataOutputStream.flush();
                DataOutputStream.writeUTF(nombreArchivo + ".zip");
                DataOutputStream.flush();
                DataOutputStream.writeUTF(nombreArchivo);
                DataOutputStream.flush();
                File archivoLocal = new File(rutaCLocal + '/' + nombreArchivo+".zip");
                FileInputStream fi = new FileInputStream(archivoLocal);
                BufferedInputStream bi = new BufferedInputStream(fi);
                byte[] bufferOutput = new byte[1024];
                int bytesL;
                while ((bytesL = bi.read(bufferOutput)) != -1) {
                    cli.getOutputStream().write(bufferOutput, 0, bytesL);
                    if (bytesL < 1024)
                        break;
                }
                fi.close();
                bi.close();
                eliminarArchivo(rutaCLocal + '/' + nombreArchivo + ".zip");
                status = DataInputStream.readInt();
                if (status == 1)
                    System.out.println("Archivo mandado con exito");
                else
                    System.out.println("Error al mandar al archivo");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ocurrió un error de E/S: " + e.getMessage());
        }
    }

    public static void enviarALocal(String nombreArchivo, String ruta){
        try {
            DataOutputStream.writeInt(4);
            DataOutputStream.flush();
            DataOutputStream.writeUTF(nombreArchivo);
            DataOutputStream.flush();
            int opc5 = DataInputStream.readInt();
            if(opc5 == 1) {
                FileOutputStream f = new FileOutputStream(ruta);
                byte[] bufferInput = new byte[1024];
                int bytesLeidos;
                while ((bytesLeidos = cli.getInputStream().read(bufferInput)) != -1) {
                    f.write(bufferInput, 0, bytesLeidos);
                    if(bytesLeidos < 1024)
                        break;
                }
                f.close();
            } else if (opc5 == 2) {
                String archivoRecibido = DataInputStream.readUTF();
                String name = DataInputStream.readUTF();
                FileOutputStream f = new FileOutputStream(ruta);
                byte[] bufferInput = new byte[1024];
                int bytesLeidos;
                while ((bytesLeidos = cli.getInputStream().read(bufferInput)) != -1) {
                    f.write(bufferInput, 0, bytesLeidos);
                    if(bytesLeidos < 1024)
                        break;
                }
                f.close();
                File folder = new File(rutaCLocal + '/' + name);
                folder.mkdir();
                unzip(rutaCLocal + '/' + archivoRecibido, rutaCLocal);
                eliminarArchivo(rutaCLocal + '/' + archivoRecibido);
            }
            status = DataInputStream.readInt();
            if(status == 1)
                System.out.println("Archivo recivido con exito");
            else
                System.out.println("Error al solicitar el archivo");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ocurrió un error de E/S: " + e.getMessage());
        }
    }

    public static void eliminarRemoto(String nombreArchivo) {
        try {
            DataOutputStream.writeInt(2);//Llamar la función del servidor (número provisional)
            DataOutputStream.flush();

            DataOutputStream.writeUTF(nombreArchivo); //Pasar argumento de la ruta nueva
            DataOutputStream.flush();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void listarRemoto() {
        try {
            DataOutputStream.writeInt(1);//Llamar la función del servidor (número provisional)
            DataOutputStream.flush();

            String response = DataInputStream.readUTF();
            System.out.println(response);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String listarLocal(String root, int tab) {
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
                    buffer = buffer + listarLocal(root + '/' + f.getName(), tab + 1);
                }
            }
        }
        return buffer;
    }

    private static void initStream(Socket cliente) throws IOException {
        DataOutputStream = new DataOutputStream(cliente.getOutputStream());
        DataInputStream = new DataInputStream(cliente.getInputStream());
        ObjectOutputStream = new ObjectOutputStream(cliente.getOutputStream());
        ObjectInputStream = new ObjectInputStream(cliente.getInputStream());
    }

    //Función para cambiar la carpeta base de la carpeta Remota
    public static void ChangeFileBaseRemote(String nuevaRutaBase){
        try{
            DataOutputStream.writeInt(5);//Llamar la función del servidor (número provisional)
            DataOutputStream.flush();

            DataOutputStream.writeUTF(nuevaRutaBase); //Pasar argumento de la ruta nueva
            DataOutputStream.flush();
        }//try
        catch(IOException e){
            e.printStackTrace();
        }//catch
    }//ChangeFileBaseRemote

    //Función para cambiar la carpeta base de la carpeta Local
    public static void ChangeFileBaseLocal(String nuevaRutaLocal){

        File nuevoDirectorio = new File(nuevaRutaLocal);
        if (nuevoDirectorio.exists() && nuevoDirectorio.isDirectory()) {
            if (nuevoDirectorio.canWrite()) {
                System.out.println("Cambiando el directorio a: " + nuevoDirectorio.getAbsolutePath());
                System.setProperty("user.dir", nuevoDirectorio.getAbsolutePath());
                rutaCLocal = nuevoDirectorio.getAbsolutePath();
            } else {
                System.out.println("Error: Permisos insuficientes para cambiar el directorio");
            }
        } else {
            System.out.println("Error: El directorio no existe o no es un directorio");
        }
    }//ChangeFileBaseRemote

    //Función para crear nueva carpeta en la carpeta remota
    public static void NewFolderInRemote(String nameNewFile) {
        try{
            DataOutputStream.writeInt(6);//Llamar la función del servidor (número provisional)
            DataOutputStream.flush();

            DataOutputStream.writeUTF(nameNewFile); //Pasar argumento de nombre de la carpeta nueva
            DataOutputStream.flush();
        }//try
        catch(IOException e){
            e.printStackTrace();
        }//catch
    }//NewFileInRemote

    public static void NewFolderLocal(String nameNewFile){
        File NewFile = new File( rutaCLocal + '/' + nameNewFile);
        if (NewFile.mkdir()) {
            System.out.println("Directorio creado exitosamente");
        }//if
        else {
            System.out.println("¡ERROR!");
        }//else
    }//NewFlieLocal

    //Función para cerrar Conexión y salir
    public static void finishConection() {
        try {
            DataOutputStream.close();
            ObjectOutputStream.close();
            DataInputStream.close();
            ObjectInputStream.close();
            cli.close();
        }//try
        catch (IOException e) {
            e.printStackTrace();
        }//catch
    }//finishConection

    public static boolean eliminarArchivo(String ruta) {
        File archivo = new File(ruta);
        boolean flag = false;
        if(archivo.exists()) {
            if(archivo.isFile()) {
                try {
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

}//Cliente