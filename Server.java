import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Server {
    public static int localport;// = 1902;
    public static ServerSocket serveur;
    public static List<Socket> clients = new ArrayList<>();

    public static void askToDel(Object demande) throws Exception {
        List<String> sbDispo = new ArrayList<>(Fonction.getSbDispo());
        if (sbDispo.size() != 3) {
            System.out.println("Erreur-delete:Tout les slaves ne sont pas prets...");
            throw new Exception("Erreur:le serveur ne peut pas demander la suppression du fichier");
        }
        Delete del = (Delete) demande;
        for (int i = 0; i < sbDispo.size(); i++) {
            Map<String, Object> sbMap = Reader.getValue(sbDispo.get(i), "conf.txt");

            Socket tempSb = new Socket(sbMap.get("ip").toString(), Integer.parseInt(sbMap.get("localport").toString()));
            Fonction.sendObjectToServer(tempSb, del);
            tempSb.close();
        }
    }

    
    public static void getFromClientAndShare(Socket client, Object demande,List<List<String>>sbDispo) throws Exception {
        System.out.println("Client connecté: " + client.getInetAddress());
        Fichier fichier = (Fichier) demande;

        // list subserveur disopo->
        //List<String> sbDispo = new ArrayList<>(Fonction.getSbDispo());
        // if (sbDispo.size() != 3) {
        //     System.out.println("Erreur-upload:Tout les slaves ne sont pas prets...");
        //     throw new Exception("Erreur:le serveur ne peut pas telecharger le fichier");
        // }
        List<String> str = new ArrayList<>();
        int k =1;
        String oldname = fichier.getParentname();
        while (Reader.readHistory(fichier.getParentname())) {
            fichier.setParentname(oldname+k);
            k++;
        }
        str.add("["+fichier.getParentname()+"]");
        Writer.writeFile(str);
        List<Fichier> segFichier = Main.partition(fichier, sbDispo.size());
        for (int i = 0; i < segFichier.size(); i++) {
            // Map<String, Object> sbMap = Reader.getValue(sbDispo.get(i));
            Socket tempSb1 = new Socket(sbDispo.get(i).get(1), Integer.parseInt(sbDispo.get(i).get(0).toString()));
            List<List<String>>tempServList = new ArrayList<>(sbDispo);
            tempServList.remove(i);
            Fonction.sendObjectToServer(tempSb1, tempServList);
            tempSb1.close();

            Socket tempSb = new Socket(sbDispo.get(i).get(1), Integer.parseInt(sbDispo.get(i).get(0).toString()));
            Fonction.sendObjectToServer(tempSb, segFichier.get(i));
            tempSb.close();
        }
    }

    public static Thread receiveThread(Socket client, Partition partition) throws IOException, ClassNotFoundException {
        Thread thread = new Thread(() -> {
            try {
                Partition received = (Partition) Fonction.getObjectFromClient(client);
                System.out.println(received + " received");
                partition.addFichiers(received.getFichiers().getFirst());
            } catch (Exception e) {
            }

        });
        thread.start();
        return thread;
    }

    public static Thread downloadThread(String ip, int port, Partition partition)
            throws IOException, ClassNotFoundException {
        Thread thread = new Thread(() -> {
            try {
                Socket Demandetelechargement = new Socket(ip, port);
                System.out.println("Telechargement de " + partition.getNom() + " de " + ip + " sur port " + port);
                Fonction.sendObjectToServer(Demandetelechargement, partition);
                Demandetelechargement.close();
            } catch (Exception e) {
            }

        });
        thread.start();
        return thread;
    }

    public static void askServerAndDownload(Socket client, Object demande, ServerSocket receivePartitionBySb)
            throws IOException, ClassNotFoundException, InterruptedException {

        Partition partition = null;
        Object fromClient = demande;
        Map<String, List<String>> owners = Fonction.owner();
        if (fromClient.getClass().equals(String.class)) {
            System.out.println("Demande de telechargement de " + fromClient);
            List<String> sbDispo = owners.get(demande.toString().trim());
            if (sbDispo.isEmpty()) {
                System.out.println("Erreur-download:Tout les slaves ne sont pas prets...");
                Fichier fichiervide = new Fichier("null", null, null);
                Socket download = new Socket(client.getInetAddress().getHostAddress(), 2025);
                Fonction.sendObjectToServer(download, fichiervide);
                download.close();
            }
            String filedoDown = (String) fromClient;
            Partition part = new Partition(filedoDown);
            Thread tt = new Thread(() -> {
                try {
                    System.out.println("Telechargement des partitions......");
                    for (String string : sbDispo) {
                        String[] sb = string.split("\\,");
                        String ip = sb[0];
                        Integer port = Integer.parseInt(sb[1]);
                        downloadThread(ip, port, part).join();
                        receiveThread(receivePartitionBySb.accept(), part).join();
                    }

                } catch (Exception e) {
                }

            });
            tt.start();
            tt.join();
            List<Fichier> fichiers = new ArrayList<>(part.getFichiers());
            System.out.println("Telechargement terminé " + fichiers.size());
            Fichier assembler = Main.assembler(fichiers);
            try {
                Socket download = new Socket(client.getInetAddress().getHostAddress(), 2025);
                if (assembler == null) {
                    Fonction.sendObjectToServer(download, new Fichier("null", null, null));
                } else {
                    assembler.setNom(part.getNom());
                    System.out.println("ito ilay alefa" + assembler);
                    Fonction.sendObjectToServer(download, assembler);
                    download.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage() + " " + client.getInetAddress().getHostAddress());
            }

        }

    }

    public static void main(String[] args) throws Exception {

        Map<String, Object> Map = Reader.getValue(Server.class.getName(), "conf.txt");

        localport = Integer.parseInt(Map.get("localport").toString());
        List<List<String>> listServer = Broadcast.searchPresence();
        if (listServer.isEmpty()) {
        Ecouteur.afficherAlerte("Aucun sous serveur est actif!");
        return;
        }
        System.err.println("Liste des sous Serveurs actifs");
        for (List<String> list : listServer) {
        System.out.println("localport = "+list.get(0)+" "+"ip = "+list.get(1));
        }
        try (ServerSocket server = new ServerSocket(localport)) {
            ServerSocket receivePartitionBySb = new ServerSocket(1999);

            System.out.println("Server actif sur port: " + localport);
            while (true) {
                Socket client = server.accept();
                clients.add(client);
                new ClientHandler(client, receivePartitionBySb, listServer).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private ServerSocket receivePartitionBySb;
    private List<List<String>> sbDispo;

    public ClientHandler(Socket socket, ServerSocket receivePartitionBySb, List<List<String>> sbDispo) {
        this.clientSocket = socket;
        this.receivePartitionBySb = receivePartitionBySb;
        this.sbDispo = sbDispo;
    }

    @Override
    public void run() {
        try {
            Object demande = Fonction.getObjectFromClient(clientSocket);
            System.out.println(demande);

            if (demande != null) {
                if (demande.getClass().equals(Fichier.class)) {
                    System.out.println("Ito ilay fichier " + demande);
                    try {
                        Server.getFromClientAndShare(clientSocket, demande, sbDispo);
                    } catch (Exception e) {
                        Ecouteur.afficherAlerte(e.getMessage());
                    }
                } else if (demande.getClass().equals(String.class)) {
                    Server.askServerAndDownload(clientSocket, demande, receivePartitionBySb);
                }

                else if (demande.getClass().equals(Delete.class)) {
                    Server.askToDel(demande);
                } else if (demande.getClass().equals(Liste.class)) {
                    Liste liste = new Liste(Fonction.liste());
                    Liste listeFromClient = (Liste) demande;
                    System.out.println(listeFromClient.getDynamicPort());
                    for (int i = 0; i < 1; i++) {
                        try (Socket socket = new Socket(clientSocket.getInetAddress().getHostAddress(),
                        listeFromClient.getDynamicPort())) {
                            Fonction.sendObjectToServer(socket, liste);
                            socket.close();
                            break;
                        } catch (Exception e) {
                            // System.out.println(e.getMessage() + " " + clientSocket.getInetAddress().getHostAddress());
                        }
                    }

                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
