import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class Server {
    private List<Client> clientsList = new ArrayList<>();
    private ServerSocket serverSocket;
    private Socket clientSocket;

    public void start() throws IOException, ClassNotFoundException {
        receive().start();
        userInputHandler();
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
        if(clientsList.isEmpty()) {
            System.out.println("!!! lista de clienti este goala");
        } else {
            for (Client client: clientsList) {
                syncTimeWithClient(client.getLocalTime());
            }
        }
    }


    private void afisareTimpLocalClienti() {

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
                            System.out.println("A fost creat un nou client cu adresa: " + clientAdaugat.getIPAddress());
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
                "    `show` pentru afisare timp local\n" +
                "    `exit` pentru iesire din program\n");
    }

    private void iesireProgram() {
        try {
            serverSocket.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}