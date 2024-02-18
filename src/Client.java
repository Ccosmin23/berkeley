import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// Clasa care reprezintă un client în rețeaua locală
class Client implements Serializable {
    private String serverAddress = "192.168.30.10";
    private int serverPort = 9700;
    private String ipAddress;

    private int timeOffset; // Diferența de timp între server și client
    private ServerSocket serverSocket;

    public Client() {
        try {
            this.ipAddress = InetAddress.getLocalHost().getHostAddress();
            start();
            receive().start();
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

    private void afisareOraCurenta() {
        Date res = new Date(TimeUtils.getLocalTime());
        DateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("\n - client, ora curenta: " + sdf1.format(res));
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

    public int getTimeOffset() {
        return timeOffset;
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


    public Thread receive () {
        return new Thread(new Runnable() {
            @Override
            public void run () {
                Socket clientSocket;
                DataInputStream clientIStream;
                DataOutputStream clientOStream;
                ObjectOutputStream oos;
                ObjectInputStream ois;

                try {
                    serverSocket = new ServerSocket(9700);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }

                while (true) {
                    try {
                        clientSocket = serverSocket.accept();
                        ois = new ObjectInputStream(clientSocket.getInputStream());

                        Client clientulActualizat = (Client) ois.readObject();
                        System.out.println("Am primit noul offset cu valoarea: " + clientulActualizat.timeOffset);

                        if(clientulActualizat.ipAddress.equals(InetAddress.getLocalHost().getHostAddress())) {
                            setTimeOffset(clientulActualizat.timeOffset);
                        } else {
                            System.out.println("nu este pentru mine" + clientulActualizat.ipAddress + " ggg " + InetAddress.getLocalHost().getHostAddress());
                        }


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}
