import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            TCPServer tcpServer = new TCPServer(5555);
            tcpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
