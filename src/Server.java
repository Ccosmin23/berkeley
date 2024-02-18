import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

class Server {
    private List<Client> clientsList = new ArrayList<>();
    private ServerSocket serverSocket;
    private Socket clientSocket;

    public void start() throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(9700);

        while (true) {
            clientSocket = serverSocket.accept();
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

            //primeste si afiseaza mesajul
            Client clientAdaugat = (Client) ois.readObject();

            if (clientAdaugat != null) {
                System.out.println("A fost creat un nou client cu adresa: " + clientAdaugat.getIPAddress());
                clientsList.add(clientAdaugat);
            }
        }
    }

    // Metodă pentru a sincroniza timpul cu fiecare client
    public synchronized void syncTimeWithClient(int clientTime) {
        // Calculăm diferența de timp între server și client
        int serverTime = (int) (System.currentTimeMillis() / 1000);
        int timeOffset = clientTime - serverTime;

        // Setăm diferența de timp pentru client adica ii trimitem clientului noua valoare a timpului
        setTimeOffset(timeOffset);
    }

    public void setTimeOffset(int timeOffset) {
        try (Socket socket = new Socket("192.168.30.10", 9700);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
            oos.writeObject(timeOffset);
        } catch (IOException e) {
            System.out.println("eroare la setarea offset-ului");
        }
    }

    private void sincronizeazaCeasurile() {
        for (Client client: clientsList) {
            syncTimeWithClient(client.getLocalTime());
        }
    }
}