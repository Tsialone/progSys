import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Sb2 {
    public static int localport;// = 1724;
    public static String directory;// = "sb2_server/";
    public static List<List<String>>sbDispo;
    public static String ip;
    public static void delFile (Object demande) throws Exception
    {
        Delete del = (Delete) demande;
        String filename = del.getFilename();
        Fonction.deleteFile(filename, directory);
    }
    public static void receiveAndSave(Socket client, Object demande) throws IOException, ClassNotFoundException {
        Fichier fichier = (Fichier) demande;
        System.out.println(fichier.getNom());
        int i=1;
        String trueFileName = (fichier.getNom().replaceFirst("backup_",""));  
        while (Fonction.directorycheck(directory,fichier.getNom())) {
            fichier.setNom(trueFileName+"("+i+")");
            i++;
        }
        boolean containBackpBefore =fichier.getNom().contains("backup_");
        if (containBackpBefore) {
            fichier.setNom(fichier.getNom().replaceFirst("backup_",""));
        } 
        Fonction.ecrireFichier(fichier, directory);

        if (containBackpBefore) {
            fichier.setNom("backup_"+fichier.getNom());
        }
        if (sbDispo.size()>=1 && !fichier.getNom().contains("backup_")) {
            List<String>strs = new ArrayList<>();
            int random = Fonction.randomize(sbDispo.size()-1);
            Socket tempSb = new Socket(sbDispo.get(random).get(1), Integer.parseInt(sbDispo.get(random).get(0).toString()));
            String name = fichier.getNom().substring(0, fichier.getNom().indexOf("_"+fichier.getParentname()));
            strs.add(name+"=" + ip+","+localport+"|"+sbDispo.get(random).get(1)+","+sbDispo.get(random).get(0).toString());
            Writer.writeFile(strs);
            fichier.setNom("backup_"+fichier.getNom());
            Fonction.sendObjectToServer(tempSb,fichier);
        }
    }

    public static void AddAndgivePartition(Socket client, int portToGive,  String ip  ,  Object demande)
            throws ClassNotFoundException, IOException {
        Partition partition = (Partition) demande;
        Fichier fichier = Fonction.searchFile(partition.getNom(), directory);
        System.out.println(partition);
        System.out.println(partition.getNom());
        partition.addFichiers(fichier);

        Socket Demandetelechargement = new Socket(ip, portToGive);
        Fonction.sendObjectToServer(Demandetelechargement,partition);
        Demandetelechargement.close();
    }
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {

        Map<String, Object> Map = Reader.getValue(Sb2.class.getName() , "conf.txt");
        localport = Integer.parseInt(Map.get("localport").toString());
        directory = Map.get("directory").toString();
        ip = Map.get("ip").toString();

        Broadcast.sendPresence(localport, Map.get("ip").toString());

        try (ServerSocket server = new ServerSocket(localport)) {
            System.out.println("Server actif sur port: " + localport);
            while (true) {
                Socket client = server.accept();
                System.out.println("Client connecté: " + client.getInetAddress());
                Object demande = Fonction.getObjectFromClient(client);

                // sauvegarder la partition
                if (demande != null) {
                    if (demande.getClass().equals(Fichier.class)) {
                        receiveAndSave(client, demande);
                    }
                      // ajouter et donner la partition a Sb3
                      else if (demande.getClass().equals(Partition.class)) {
                        AddAndgivePartition(client,
                                1999,
                                Reader.getValue("Server" , "conf.txt").get("ip").toString(), demande);
                    }
                    else if (demande.getClass().equals(Delete.class)) {
                        System.out.println(demande.getClass().getSimpleName());
                        delFile(demande);
                    } else if (demande.getClass().equals(Liste.class)) {
                        Liste liste = new Liste(Fonction.afficheListe(directory));

                        Thread.sleep(5000);
                        try (Socket socket = new Socket(client.getInetAddress().getHostAddress(), 4456)) {
                            Fonction.sendObjectToServer(socket, liste);
                            socket.close();
                        } catch (Exception e) {
                            System.out.println(e.getMessage() + " " + client.getInetAddress().getHostAddress());
                        }

                }else if (demande.getClass().equals(java.util.ArrayList.class)) {
                    System.out.println("list io");
                    
                    sbDispo = (List<List<String>>)demande;
                    for (List<String> list : sbDispo) {
                        System.out.println("localport = "+list.get(0)+" "+"ip = "+list.get(1));
                    }
                }
                }
               
                client.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
