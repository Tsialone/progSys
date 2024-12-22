import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Liste implements Serializable {
    List<String> allfiles = new ArrayList<>();
    int dynamicPort;

    public Liste(List<String> allfiles) {
        setAllfiles(allfiles);
    }

    public void setDynamicPort(int dynamicPort) {
        this.dynamicPort = dynamicPort;
    }

    public void setAllfiles(List<String> allfiles) {
        this.allfiles = allfiles;
    }

    public List<String> getAllfiles() {
        return allfiles;
    }

    public int getDynamicPort() {
        return dynamicPort;
    }

}
