import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Client  {
    public static Socket client ;
    public  static String ip;
    public  static int port;
    public static ServerSocket clientServer;
    public Client ()
    {

    }

    public static Thread upload(Socket client, String path) throws IOException {
        Thread thread = new Thread(()->{
            try {
                Fonction.sendFileToServer(client, path);
                
            } catch (Exception e) {
            }
        });
        thread.start();
        return thread;
    }

    public  Thread download(Socket client, String fileToDown) throws IOException, ClassNotFoundException {
        Fonction.sendObjectToServer(client, fileToDown);
        Thread thread = new Thread(()->{
            try {
                ServerSocket socket = new ServerSocket(2025);
                System.out.println("Telechargement en cours de "+fileToDown+" en cours.....");
                Socket c1 = socket.accept();
                Fichier file = (Fichier) Fonction.getObjectFromClient(c1);
                if (!file.getNom().equals("null")) {
                    Fonction.ecrireFichier(file, "download/");
                }
                c1.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
           
        });
        thread.start();
        return thread;
    }
    public static Thread liste(int dynamicPort) {
        System.out.println(dynamicPort);
        Thread thread = new Thread(() -> {
            try (ServerSocket socket = new ServerSocket()) {
                socket.setReuseAddress(true);  
                socket.bind(new InetSocketAddress(dynamicPort));  
                System.out.println("En attente de liste depuis le MasterServer.....");
                Socket masterServer = socket.accept();
                System.out.println("Connection accepted from " + masterServer.getInetAddress());
                Object demande = Fonction.getObjectFromClient(masterServer);
                System.out.println("Object received: " + demande.getClass().getName());
    
                if (demande instanceof Liste) {
                    Liste l = (Liste) demande;
                    List<String> resp =  new ArrayList<>(l.getAllfiles());
                    System.out.println("Files received:");
                    for (String string : resp) {
                        System.out.println(string);
                    }
                } else {
                    System.out.println("Received object is not an instance of Liste");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return thread;
    }

    public static void main(String[] args) {
        try (Socket client = new Socket(ip, port)) {
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
