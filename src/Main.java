import java.io.*;
import java.net.*;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("nu sunt destule argumente adaugate in comanda introdusa");
            return;
        }

        startAppWith(args);
    }

    public static void startAppWith(String[] args) {
        switch (args[0]) {
            case "client":
                startAsAClient();
                break;
            case "server":
                startAsAServer();
                break;
            default: {
                System.out.println("argument introdus incorect");
                break;
            }
        }
    }

    public static void startAsAClient() {
        new Client();
    }

    public static void startAsAServer() {
        Server server = new Server();

        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
