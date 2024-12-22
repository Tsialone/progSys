import java.io.Serializable;

public class Fichier implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String parentname;
    private String nom;
    private byte[] data;

    public Fichier(String nom, String parentname, byte[] data) {
        setData(data);
        setNom(nom);
        setParentname(parentname);
    }
    
    public void setParentname(String parentname) {
        this.parentname = parentname;
    }
    
    public String getParentname() {
        return parentname;
    }
    
    public byte[] getData() {
        return data;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setData(byte[] data) {
        this.data = data;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
}
