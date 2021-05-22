import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer {

    private static TCPServer server;
    private ServerSocket serverSocket;
    private AuthService authService;
    private ExecutorService executorService;

    private List<ClientHandler> clients;

    public TCPServer(int port) throws IOException {
        server = this;
        serverSocket = new ServerSocket(port);
        clients = new ArrayList<>();
        authService = new BaseAuth();
        executorService = Executors.newCachedThreadPool();
        authService.start();
    }

    public void start() {
        try {
            while (true) {
                System.out.println("Ждем подключения клиента");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public AuthService getAuthService() {
        return authService;
    }
    public ExecutorService getExecutorService() { return executorService; }

    public synchronized void sendPersonalMessage(ClientHandler fromClient, String toNickName, String message) {
        for (ClientHandler client : clients) {
            if (client.getNickName().equals(toNickName)) {
                if (!fromClient.getNickName().equals(toNickName)) {
                    client.sendMessage(fromClient.getNickName() + ": " + message);
                }
                fromClient.sendMessage(toNickName + ": " + message);
                return;
            }
        }
        fromClient.sendMessage("Участник с ником \"" + toNickName + "\" не найден");
    }

    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public synchronized boolean isNickNameAlreadyExists(String nickName) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getNickName().equals(nickName)) {
                return true;
            }
        }
        return false;
    }

    public static TCPServer getServer() {
        return server;
    }
}
