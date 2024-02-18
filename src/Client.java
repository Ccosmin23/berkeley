import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class Client implements Serializable {
    private String serverAddress = "192.168.30.10";
    private int serverPort = 9700;
    private String ipAddress;
    private ServerSocket serverSocket;
    private long clientTime;

    public Client() {
        clientTime = TimeUtils.getLocalTime();

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

                        if(clientulActualizat.ipAddress.equals(InetAddress.getLocalHost().getHostAddress())) {
                            System.out.println("\nOra a fost actualizata");

                            afisareOra(clientTime);
                            setLocalTime(clientulActualizat.clientTime);
                            afisareOraCurenta();
                        } else {
                            System.out.println("nu este pentru mine" + clientulActualizat.ipAddress);
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

    private void afisareOraCurenta() {
        Date res = new Date(clientTime);
        DateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[client] ora curenta este: " + sdf1.format(res));
    }

    private void afisareOra(long miliseconds) {
        Date res = new Date(miliseconds);
        DateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[client] ora veche a fost: " + sdf1.format(res));
    }

    public void setLocalTime(long time) {
        clientTime = time;
    }

    public String getIPAddress() {
        return ipAddress;
    }
}
