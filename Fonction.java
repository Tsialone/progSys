import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;

public class Fonction {
    public static boolean directorycheck(String directory,String Filename){
        File doss = new File(directory);
        File [] filedispo = doss.listFiles();
        for (File file : filedispo) {
            if (file.getName().equals(Filename)) {
                System.out.println(file.getName().contains(Filename));
                return true;
            }
        }
        return false;
    }

    public static int randomize(int max){
        Random random = new Random();
        return random.nextInt((max-0)+1);
    }
    public static String removeIndex(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return fileName;
        }
        return fileName.replaceAll("(\\.[^\\d]+)\\d+$", "$1");
    }

    public static List<String> parts(String file) {
        List<String> resp = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> Map = Reader.getValue(file, "histo.txt");
            String part = "part" + i;
            Map.get(part);
            if (Map.get(part) != null) {
                resp.add(part);
            }
        }
        return resp;
    }

    public static Map<String, List<String>> owner() {
        List<String> allfiles = Reader.getAllFiles();
        Map<String, List<String>> owners = new HashMap<>();
        for (String file : allfiles) {
            boolean test = true;
            List<String> parts = parts(file);
            List<String> owner = new ArrayList<>();
            Map<String, Object> Map = Reader.getValue(file, "histo.txt");
            String info = "";
            for (String part : parts) {
                info = Map.get(part).toString();
                if (test(info.trim()).size() == 0) {
                    test = false;
                    owner.clear();
                } else {
                    owner.add(test(info.trim()).getFirst());
                }
            }
            if (test) {
                owner = owner.stream().distinct().toList();
                owners.put(removeIndex(file.trim()), owner);
            }
        }
        return owners;
    }
    public static List<String> liste() {
        List<String> resp = new ArrayList<>();
        List<String> allfiles = Reader.getAllFiles();
        for (String file : allfiles) {
            boolean test = true;
            List<String> parts = parts(file);
            Map<String, Object> Map = Reader.getValue(file, "histo.txt");
            for (String part : parts) {
                String info = Map.get(part).toString();
                if (test(info).size() == 0) {
                    test = false;
                }
            }
            if (test) {
                resp.add(removeIndex(file.trim()));
            }
        }
        resp = resp.stream().distinct().toList();
        return resp;
    }

    public static List<String> test(String info) {
        List<String> resp = new ArrayList<>();
        String[] sbs = info.split("\\|");
        for (String string : sbs) {
            String[] sbsIpPort = string.split("\\,");
            String Ip = sbsIpPort[0];
            Integer Port = Integer.parseInt(sbsIpPort[1].trim());
            try {
                Socket test = new Socket();
                InetSocketAddress address = new InetSocketAddress(Ip, Port);
                test.connect(address, 2000);
                test.close();
                resp.add(string);
            } catch (Exception e) {
            }
            // System.out.println("Ip " + Ip);
            // System.out.println("Port " + Port);
        }
        return resp;
    }

    public static List<String> afficheListe(String repository) {
        File repositor = new File(repository);
        List<String> resp = new ArrayList<>();
        for (String string : repositor.list()) {
            string = string.replace("part1_", "");
            resp.add(string);
        }
        return resp;
    }

    public static void deleteFile(String filename, String repository) {
        File repositor = new File(repository);
        if (repositor.exists()) {
            for (String string : repositor.list()) {
                if (string.contains(filename)) {
                    Reader.delData(filename);
                    System.out.println(string);
                    File theFile = new File(repositor + "/" + string);
                    theFile.delete();
                }
            }
        }
    }

    public static List<String> getSbDispo() {
        Reader reader = new Reader("conf.txt");
        String sub = "Sb";
        String[] conf = Reader.StringBuilder.toString().split("\n");
        List<String> sbDispo = new ArrayList<>();
        for (int i = 0; i < conf.length; i++) {
            if (conf[i].contains(sub)) {

                conf[i] = conf[i].replace("]", "");
                conf[i] = conf[i].replace("[", "");
                Map<String, Object> sbmap = Reader.getValue(conf[i], "conf.txt");

                String sbip = sbmap.get("ip").toString();
                int sbport = Integer.parseInt(sbmap.get("localport").toString());
                try {
                    Socket clienttest = new Socket();
                    InetSocketAddress address = new InetSocketAddress(sbip, sbport);
                    clienttest.connect(address, 1000);
                    clienttest.close();
                    sbDispo.add(conf[i]);
                } catch (Exception e) {
                }
            }
        }
        return sbDispo;
    }

    public static byte[] getData(File theFile) throws IOException {
        FileInputStream f = new FileInputStream(theFile);
        long tranche = Files.size(theFile.toPath());
        byte[] buffer = new byte[(int) (tranche)];
        byte[] resp = new byte[buffer.length];
        int byteData;
        while ((byteData = f.read(buffer)) != -1) {
            resp = Arrays.copyOf(buffer, byteData);
        }
        return resp;
    }

    public static Fichier searchFile(String filename, String directory) {
        try {
            for (int i = 0; i < 3; i++) {
                String part = "part" + (i + 1) + "_" + filename;
                File file = new File(directory + File.separator + part);

                if (file.exists()) {
                    byte[] data = Fonction.getData(file);
                    return new Fichier(file.getName(), file.getName(), data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("File not found: " + filename + " in directory: " + directory);
        return null;
    }

    public static void ecrireFichier(Fichier fichier, String path) throws IOException {
        File resp = new File(path + fichier.getNom());
        resp.createNewFile();
        FileOutputStream ecrire = new FileOutputStream(resp);
        ecrire.write(fichier.getData());
        ecrire.close();
    }

    public static void sendObjectToServer(Socket client, Object object) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
        objectOutputStream.writeObject(object);
        System.out.println("Fichier envoyer " + object.toString());
    }

    public static void sendObjectToClient(Socket client, Object object) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
        objectOutputStream.writeObject(object);
        System.out.println("Object sent to client: " + object.toString());
    }

    public static void sendFileToServer(Socket client, String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Le fichier n'existe pas.");
            return;
        }
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
        byte[] data = getData(file);
        Fichier fichier = new Fichier(file.getName(), file.getName(), data);
        objectOutputStream.writeObject(fichier);
        System.out.println("Fichier envoyer " + fichier.getNom());
    }

    public static Object getObjectFromClient(Socket inclient) throws IOException, ClassNotFoundException {
        try (ObjectInputStream lecture = new ObjectInputStream(inclient.getInputStream())) {
            return lecture.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getObjectFromServer(Socket inserver) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(inserver.getInputStream());
        Object receivedObject = objectInputStream.readObject();
        System.out.println("Object received from server: " + receivedObject.toString());
        return receivedObject;
    }

}
