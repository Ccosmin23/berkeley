import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Server {
    private List<Client> clientsList = new ArrayList<>();
    private ServerSocket serverSocket;
    long serverTime;

    public void start() throws IOException, ClassNotFoundException {
        serverTime = TimeUtils.getLocalTime();

        receive().start();
        userInputHandler();
    }

    public Thread receive () {
        return new Thread(new Runnable() {
            @Override
            public void run () {
                Socket clientSocket;
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

    private void sincronizeazaCeasurile() {
        if(clientsList.isEmpty()) {
            System.out.println("!!! lista de clienti este goala");
        } else {
            for (Client client: clientsList) {
                syncTimeWith(client);
            }
        }
    }

    public synchronized void syncTimeWith(Client client) {
        client.setLocalTime(serverTime);
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

    private void afisareTimpLocal() {
        afisareOraCurenta();
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
                    afisareTimpLocal();
                    break;
                } case "utc2": {
                    // afisare ora veche
                    afisareOraVeche();

                    // actualizare + afisare ora noua
                    serverTime = TimeUtils.getLocalTime() + (2 * 60 * 60 * 1000);
                    afisareOraCurenta();
                    break;
                } case "utc3": {
                    // afisare ora veche
                    afisareOraVeche();

                    // actualizare + afisare ora noua
                    serverTime = TimeUtils.getLocalTime() + (3 * 60 * 60 * 1000);
                    afisareOraCurenta();
                    break;
                } case "reset": {
                    // afisare ora veche
                    afisareOraVeche();

                    // actualizare + afisare ora noua
                    serverTime = TimeUtils.getLocalTime();
                    afisareOraCurenta();
                    break;
                }
                default: {
                    System.out.println("s-au introdus gresit argumentele");
                    break;
                }
            }
        }
    }

    private void afisareOraCurenta() {
        Date res = new Date(serverTime);
        DateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[server] ora curenta este: " + sdf1.format(res));
    }

    private void afisareOraVeche() {
        Date res = new Date(serverTime);
        DateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[server] ora veche a fost: " + sdf1.format(res));
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
                "    `utc2` pentru a schimba ora curenta cu 2 ore\n" +
                "    `utc3` pentru a schimba ora curenta cu 3 ore\n" +
                "    `reset` pentru a reseta ora la cea locala\n");
    }

    private void iesireProgram() {
        System.exit(0);
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(int serverTime) {
        this.serverTime = serverTime;
    }
}