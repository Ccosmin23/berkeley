import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

// Clasa care reprezintă un client în rețeaua locală
class Client implements Serializable {
    private String serverAddress = "192.168.30.10";
    private int serverPort = 9700;
    private String ipAddress;
    private int timeOffset; // Diferența de timp între server și client

    public Client() {
        try {
            this.ipAddress = InetAddress.getLocalHost().getHostAddress();
            start();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        try (Socket socket = new Socket(serverAddress, serverPort);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.out.println("eroare la creerea clientului");
            e.printStackTrace();
        }
    }

    // Metodă pentru a conecta la server și a trimite timpul local
    public void connectToServer() {
        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Trimitem timpul local la server
            out.println(TimeUtils.getLocalTime());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodă pentru a seta diferența de timp între server și client
    public void setTimeOffset(int timeOffset) {
        this.timeOffset = timeOffset;
    }

    // Metodă pentru a obține timpul local ajustat
    public int getAdjustedLocalTime() {
        return TimeUtils.getLocalTime() + timeOffset;
    }

    public int getLocalTime() {
        return TimeUtils.getLocalTime();
    }

    public String getIPAddress() {
        return ipAddress;
    }

    public void setIPAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
