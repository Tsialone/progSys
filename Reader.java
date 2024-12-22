import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.print.DocFlavor.STRING;

public class Reader {
    FileReader FileReader;
    static String str;
    static StringBuilder StringBuilder;

    public Reader(String file) {
        try (FileReader FileReader = new FileReader(file)) {
            str = FileReader.toString();
            int i = FileReader.read();
            StringBuilder = new StringBuilder();
            while (i != -1) {
                StringBuilder.append((char) i);
                i = FileReader.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAllFiles() {
        Reader Reader = new Reader("histo.txt");
        String[] str = StringBuilder.toString().split("\n");
        List<String> allfiles = new ArrayList<>();
        for (String string : str) {
            if (string.contains("[") && string.contains("]")) {
                string = string.replace("[", "");
                string = string.replace("]", "");
                string = string.trim();
                allfiles.add(string);
            }
        }
        return allfiles;
    }
    public static boolean readHistory(String val){
        StringBuilder StringBuilder1 = null;
        try (FileReader FileReader = new FileReader("histo.txt")) {
            String str1 = FileReader.toString();
            int i = FileReader.read();
            StringBuilder1 = new StringBuilder();
            while (i != -1) {
                StringBuilder1.append((char) i);
                i = FileReader.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String []strs = StringBuilder1.toString().split("\n");
        int i =0;
        while (i < strs.length) {
            if (strs[i].equals("[" + val + "]")) 
                return true;
            i++;
        }
        return false;
    }
    public static void delData(String filename) {
        String cheminFichier = "histo.txt";
        List<String> lignesModifiees = new ArrayList<>();
    
        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            boolean supprimerBloc = false;
    
            while ((ligne = reader.readLine()) != null) {
                if (ligne.contains("[") && ligne.contains("]")) {
                    String templigne = ligne;
                    templigne = templigne.replace("[", "");
                    templigne = templigne.replace("]", "");
                    if (templigne.equals(filename)) {
                        supprimerBloc = true;
                        continue; 
                    } else {
                        supprimerBloc = false;
                    }
                }
    
                if (supprimerBloc) {
                    continue;
                }
    
                lignesModifiees.add(ligne);
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture du fichier : " + e.getMessage());
            return;
        }
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cheminFichier))) {
            for (String ligne : lignesModifiees) {
                writer.write(ligne);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture du fichier : " + e.getMessage());
        }
    }


    public static Map<String, Object> getValue(String val, String file) {
        Reader Reader = new Reader(file);
        String[] str = StringBuilder.toString().split("\n");
        int i = 0;
        Map<String, Object> Map = new HashMap<>();
        while (i < str.length) {

            if (str[i].contains("[" + val + "]")) {
                int j = i + 1;
                while (j < str.length) {
                    if (str[j].contains("[") && !str[j].equals("[" + val + "]")) {
                        break;
                    }
                    if (str[j].split("=").length == 2) {
                        String val1 = str[j].split("=")[0];
                        String val2 = str[j].split("=")[1].trim();
                        // System.out.println( "val1 " +val1 + " val2 "+ val2);
                        if (val1.equals("localport")) {
                            Map.put(val1, Integer.parseInt(val2));
                        } else {
                            Map.put(val1, val2);
                        }
                    }
                    j++;

                }
            }
            i++;
        }
        return Map;
    }

    public static void main(String[] args) {
        List<String> files = getAllFiles();
        for (String string : files) {
            Map<String, Object> Map = getValue(string, "histo.txt");
       }
    }
}
