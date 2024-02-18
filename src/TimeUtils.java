import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

// Clasa utilitară pentru lucrul cu timpul
class TimeUtils {
    // Metodă pentru a obține timpul local
    public static int getLocalTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static int setUTC2LocalTime() {
        return (int) (System.currentTimeMillis() / 1000 + 2);
    }

    // Metodă pentru a citi timpul local de pe un socket
    public static int getLocalTimeFromSocket(Socket socket) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String clientTimeStr = in.readLine();
            return Integer.parseInt(clientTimeStr);
        }
    }
}
