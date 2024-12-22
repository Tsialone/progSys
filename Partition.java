import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Partition implements Serializable {
    String nom;
    List<Fichier> fichiers = new ArrayList<>();
    int dynamicPort;

    public Partition(String nom) {
        setNom(nom);
    }
    public void setDynamicPort(int dynamicPort) {
        this.dynamicPort = dynamicPort;
    }
    public int getDynamicPort() {
        return dynamicPort;
    }

    public void addFichiers(Fichier fichier) {
        fichiers.add(fichier);
    }

    public void setFichiers(List<Fichier> fichiers) {
        this.fichiers = fichiers;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<Fichier> getFichiers() {
        return fichiers;
    }

    public String getNom() {
        return nom;
    }

}
