import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.net.*;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Ecouteur implements ActionListener {

    Ui ui;

    public static void afficherAlerte(String message) {
        JOptionPane.showMessageDialog(null, message, "Alerte", JOptionPane.INFORMATION_MESSAGE);
    }

    public Ecouteur(Ui ui) {
        this.ui = ui;
    }

    public static void showList() {
        List<String> listeFile = new ArrayList<>();
        int dynamicPort = 0;
        Map<String, Object> ServerMap = Reader.getValue("Server","conf.txt");
        String Aip = ServerMap.get("ip").toString();
        int localport = Integer.parseInt(ServerMap.get("localport").toString());
        try (Socket client = new Socket(Aip, localport)) {
            
            ServerSocket serverSocket = new ServerSocket(0);
             dynamicPort = serverSocket.getLocalPort();
             Liste liste = new Liste(null);
             liste.setDynamicPort(dynamicPort);
             serverSocket.close();
             Client.liste(dynamicPort);
             Fonction.sendObjectToServer(client, liste);
             Thread.sleep(1000);
            client.close();
        } catch (Exception e1) {
            // e1.printStackTrace();
        }
        System.out.println("Liste de fichier disponible");
        try {
        } catch (Exception e) {
            // e.printStackTrace();
        }
        System.out.println();
    }



    
    public static void actionHandler(String ip, String port, String action) throws Exception {
        Scanner Scanner = new Scanner(System.in);
        if (action.equalsIgnoreCase("list")) {
            showList();
        } else if (action.equalsIgnoreCase("do")) {
            showList();
            System.out.println("lequel telecharger?");
            Liste liste = new Liste(null);

            String filedown = Scanner.nextLine();
            if (filedown.length() == 0) {
                afficherAlerte("Veulliez choisir le  fichier a telecharger!");
            } else if (ip.length() == 0 || port.length() == 0) {
                afficherAlerte("L'addresse IP et le port ne doit pas etre vide");
            } else {
             Thread dodo =   new Thread(()->{
                    try (Socket client = new Socket(ip, Integer.parseInt(port))) {
                        Client cli = new Client();
                        cli.download(client, filedown).join();
                        System.out.println("Fichier telecharger");
                        client.close();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
                dodo.start();
                dodo.join();
              

            }

        } else if (action.equalsIgnoreCase("del")) {
            showList();
            System.out.println("lequel supprimer?");
            String filedown = Scanner.nextLine();
            if (filedown.length() == 0) {
                afficherAlerte("Veulliez choisir le  fichier a supprimer!");
            } else if (ip.length() == 0 || port.length() == 0) {
                afficherAlerte("L'addresse IP et le port ne doit pas etre vide");
            } else {
                Map<String, Object> serverConf = Reader.getValue("Server" , "conf.txt");
                String Aip = serverConf.get("ip").toString();
                int localport = Integer.parseInt(serverConf.get("localport").toString());
                try (Socket delTest = new Socket(Aip, localport)) {
                    Delete del = new Delete(filedown, null);
                    Fonction.sendObjectToServer(delTest, del);
                    delTest.close();
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }

        } else if (action.equalsIgnoreCase("up")) {

            System.out.println("Entrer le path du fichier a telecharger");
            String fileup = Scanner.nextLine();
            if (fileup.length() == 0) {
                afficherAlerte("Veulliez choisir le  fichier a telecharger!");
            } else if (ip.length() == 0 || port.length() == 0) {
                afficherAlerte("L'addresse IP et le port ne doit pas etre vide");
            } else {
                try (Socket client = new Socket(ip, Integer.parseInt(port))) {
                    Client.upload(client, fileup).join();
                    client.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        } else {
            afficherAlerte("Commande Introuvable");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        String ip = Ui.ipAddress.getText();
        String port = Ui.port.getText();
        if (action.equalsIgnoreCase("upload")) {
            ui.configurationUploadPanel();
        } else if (action.equalsIgnoreCase("Download")) {
            List<String> listeFile = new ArrayList<>();
            List<String> sbDispo = new ArrayList<>(Fonction.getSbDispo());
            Map<String, Object> sbMap = Reader.getValue(sbDispo.get(0) , "conf.txt");
            String Aip = sbMap.get("ip").toString();
            int localport = Integer.parseInt(sbMap.get("localport").toString());
            try (Socket client = new Socket(Aip, localport)) {
                if (sbDispo.size() != 3) {
                    System.out.println("Erreur-delete:Tout les slaves ne sont pas prets...");
                    throw new Exception("Erreur:le serveur ne peut pas demander la suppression du fichier");
                }
                Liste liste = new Liste(null);
                Fonction.sendObjectToServer(client, liste);
                client.close();
                try (ServerSocket lis = new ServerSocket(4456)) {
                    Socket sb1 = lis.accept();
                    Object demande = Fonction.getObjectFromClient(sb1);
                    if (demande.getClass().equals(Liste.class)) {
                        Liste l = (Liste) demande;
                        listeFile.addAll(l.getAllfiles());
                        sb1.close();
                        lis.close();
                    }
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            Ui.fileToDonw.removeAllItems();
            for (String string : listeFile) {
                Ui.fileToDonw.addItem(string);
            }
            ui.configurationDownloadPanel();

        } else if (action.equalsIgnoreCase("refresh")) {
            try {
                System.out.println(Server.serveur);
                Ui.uploadChoose.setText("file");
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        } else if (action.equalsIgnoreCase("file")) {
            JFileChooser uploadChoose = new JFileChooser();
            int returnValue = uploadChoose.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                Ui.uploadChoose.setText(uploadChoose.getSelectedFile().getAbsolutePath());
            }
        } else if (action.equalsIgnoreCase("do")) {
            Liste liste = new Liste(null);
            String filedown = Ui.fileToDonw.getSelectedItem().toString();
            if (filedown.length() == 0) {
                afficherAlerte("Veulliez choisir le  fichier a telecharger!");
            } else if (ip.length() == 0 || port.length() == 0) {
                afficherAlerte("L'addresse IP et le port ne doit pas etre vide");
            } else {
                try (Socket client = new Socket(ip, Integer.parseInt(port))) {
                    // Client.download(client, filedown);
                    // if (!file.getNom().equals("null")) {
                    // Map<String, Object> Map = Reader.getValue(Client.class.getName());
                    // Fonction.ecrireFichier(file, Map.get("directory").toString());
                    // afficherAlerte("Fichier telecharger");
                    // } else {
                    // afficherAlerte("Fichier introuvable");
                    // }
                    client.close();
                } catch (Exception e1) {
                }

            }

        } else if (action.equalsIgnoreCase("del")) {
            String filedown = Ui.fileToDonw.getSelectedItem().toString();
            if (filedown.length() == 0) {
                afficherAlerte("Veulliez choisir le  fichier a supprimer!");
            } else if (ip.length() == 0 || port.length() == 0) {
                afficherAlerte("L'addresse IP et le port ne doit pas etre vide");
            } else {
                Map<String, Object> serverConf = Reader.getValue("Server" , "conf.txt");
                String Aip = serverConf.get("ip").toString();
                int localport = Integer.parseInt(serverConf.get("localport").toString());
                try (Socket delTest = new Socket(Aip, localport)) {
                    Delete del = new Delete(filedown, null);
                    Fonction.sendObjectToServer(delTest, del);
                    delTest.close();
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }

        } else if (action.equalsIgnoreCase("up")) {
            String fileup = Ui.uploadChoose.getText();
            if (fileup.length() == 0 || fileup.equalsIgnoreCase("file")) {
                afficherAlerte("Veulliez choisir le  fichier a telecharger!");
            } else if (ip.length() == 0 || port.length() == 0) {
                afficherAlerte("L'addresse IP et le port ne doit pas etre vide");
            } else {
                try (Socket client = new Socket(ip, Integer.parseInt(port))) {
                    Client.upload(client, Ui.uploadChoose.getText());
                    client.close();
                    // afficherAlerte("Fichier envoye");

                } catch (Exception e1) {
                }

            }
        }
    }

}
