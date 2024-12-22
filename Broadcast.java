import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Broadcast {
    public static final int PORT = 4455;
    public static void sendPresence(int port,String ip) throws SocketException{
        try(DatagramSocket Broadcast = new DatagramSocket()){
            String add = "localport="+ port+" | ip="+ip;
            byte [] data = add.getBytes();
            InetAddress LOCALHOST =InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(data, data.length, LOCALHOST, PORT);
            Broadcast.send(packet);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    public static List<List<String>> searchPresence(){
        List<List<String>>List = new ArrayList<>();
        try (DatagramSocket Listener = new DatagramSocket(PORT)) {
            Listener.setSoTimeout(10000);
            while (true) {
                try {
                    List<String>Obj = new ArrayList<>();
                    System.out.println("Server actif sur le Broadcast");
                    byte[]buffer = new byte[1024];
                    DatagramPacket received = new DatagramPacket(buffer,buffer.length);
                    Listener.receive(received);
                    String rep = new String(received.getData(), 0, received.getLength());
                    System.out.println(rep);
                    String [] val = rep.split(" | ",2);
                    String localport = val[0].split("=",2)[1];
                    String ip = val[1].split("=",2)[1];
                    Obj.add(localport);
                    Obj.add(ip);
                    List.add(Obj);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return List;
    }
}

