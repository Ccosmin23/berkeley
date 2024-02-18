import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class Server {
    private List<Client> clientsList = new ArrayList<>();
    private ServerSocket serverSocket;
    private Socket clientSocket;
    int serverTime;

    public void start() throws IOException, ClassNotFoundException {
        serverTime = TimeUtils.setUTC2LocalTime();
        receive().start();
        userInputHandler();
    }

    private void sincronizeazaCeasurile() {
        if(clientsList.isEmpty()) {
            System.out.println("!!! lista de clienti este goala");
        } else {
            for (Client client: clientsList) {
                syncTimeWith(client);
            }
        }
    }

    // Metodă pentru a sincroniza timpul cu fiecare client
    public synchronized void syncTimeWith(Client client) {
        // Calculăm diferența de timp între server și client
        int timeOffset = client.getLocalTime() - serverTime;

        client.setTimeOffset(timeOffset);
        setTimeOffsetFor(client);
    }

    public void setTimeOffsetFor(Client client) {
        try (Socket socket = new Socket(client.getIPAddress(), 9700);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
            oos.writeObject(client);
        } catch (IOException e) {
            System.out.println("eroare la setarea offset-ului");
        }
    }

    private void afisareOraCurenta(int miliSeconds) {
        Date res = new Date(miliSeconds);
        DateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("\n - server, ora curenta: " + sdf1.format(res));
    }

    private void afisareTimpLocalClienti() {
        afisareOraCurenta(serverTime);
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

                        //primeste si afiseaza clientul adaugat
                        Client clientAdaugat = (Client) ois.readObject();

                        if (clientAdaugat != null) {
                            System.out.println("\nA fost creat un nou client cu adresa: " + clientAdaugat.getIPAddress());
                            clientsList.add(clientAdaugat);
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

    private void userInputHandler() throws UnknownHostException {
        afisareMesajInitial();
        afisareMeniuPrincipal();

        while (true) {
            Console consola = System.console();
            switch (consola.readLine("-> ")) {
                case "exit": {
                    iesireProgram();
                    break;
                } case "sync": {
                    sincronizeazaCeasurile();
                    break;
                } case "show": {
                    afisareTimpLocalClienti();
                    break;
                } case "UTC+2": {
                    setServerTime(2);
                    break;
                } case "UTC+3": {
                    setServerTime(3);
                    break;
                }
                default: {
                    System.out.println("s-au introdus gresit argumentele");
                    break;
                }
            }
        }
    }

    private void afisareMesajInitial() {
        try {
            System.out.println("\n==== A fost pornit server-ul pe adresa: " + InetAddress.getLocalHost().getHostAddress() + " ====\n");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private void afisareMeniuPrincipal() {
        System.out.println("Introduceti de la tastatura una din comenzile de mai jos:\n" +
                "    `sync` pentru a sincroniza ceasurile\n" +
                "    `show` pentru afisare ora curenta pentru server si clienti\n" +
                "    `exit` pentru iesire din program\n" +
                "    `UTC+2` pentru a schimba ora cu 2 ore\n" +
                "    `UTC+3` pentru a schimba ora cu 3 ore");
    }

    private void iesireProgram() {
        try {
            serverSocket.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getServerTime() {
        return serverTime;
    }

    public void setServerTime(int serverTime) {
        this.serverTime = serverTime;
    }
}