import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

public class Writer {
    public static void writeFile(List<String>Texts){
            try(FileWriter Fw = new FileWriter("histo.txt",true)) {
                BufferedWriter Bw = new BufferedWriter(Fw);
                for (String Text : Texts) {
                Bw.write(Text);
                Bw.newLine();
            }
            Bw.close();
        }
            catch (Exception e) {
                e.printStackTrace();
        }
    }
}
