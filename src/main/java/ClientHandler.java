import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private TCPServer server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickName;
    private Long lastMessageTime;
    private Long timeOut;
    private String login;
    private boolean isAuthOk = false;

    public ClientHandler(Socket socket) {
        try {
            this.server = TCPServer.getServer();
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    auth();
                    readMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void auth() throws IOException {
        while (true) {
            String str = in.readUTF();

            //  /auth login pass
            if (str.toLowerCase().startsWith("/auth ")) {
                String [] parts = str.split(" ");
                if (parts.length < 3) {
                    sendMessage("Для авторизации введите логин и пароль");
                    return;
                }
                login = parts[1];
                String password = parts[2];

                nickName = server.getAuthService().getNickName(login, password);
                if (nickName != null) {
                    if (!server.isNickNameAlreadyExists(nickName)) {
                        sendMessage("/authok " + nickName);
                        server.broadcastMessage(nickName + " зашел в чат");
                        server.subscribe(this);
                        isAuthOk = true;
                        return;
                    } else {
                        sendMessage("Учетная запись уже авторизована");
                    }
                } else {
                    if (!server.isNickNameAlreadyExists(nickName)) {
                        nickName = "Инкогнито";
                        sendMessage("/authok " + nickName);
                        server.broadcastMessage(nickName + " зашел в чат");
                        server.subscribe(this);
                        timeOut = System.currentTimeMillis();
                        return;
                    } else {
                        sendMessage("Учетная запись уже авторизована");
                    }
                    sendMessage("Неверные логин/пароль");
                }
            } else {
                sendMessage("Перед тем как отправлять сообщения авторизуйтесь через команду </auth login pass>");
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readMessage() throws IOException {
        while (socket.isConnected()) {
            String message = in.readUTF();
            System.out.println("от " + nickName + ": " + message);

            if (message.equals("/end")) {
                return;
            } else if (!isAuthOk) {
                if (lastMessageTime != null && System.currentTimeMillis() - timeOut < 120000) {
                    server.sendPersonalMessage(this, getNickName(), "Время для авторизации вышло");
                    closeConnection();
                } else if (lastMessageTime != null && System.currentTimeMillis() - lastMessageTime < 10000){
                    server.sendPersonalMessage(this, getNickName(), "Незарегистрированным пользователям нельзя отправлять сообщение чаще, чем раз в 10 секунд");
                    continue;
                }
                lastMessageTime = System.currentTimeMillis();
            } else if (message.startsWith("/w ")) {
                String[] parts = message.split(" ");
                String nickNameTo = parts[1];
                String msg = message.substring(3 + nickNameTo.length() + 1);
                server.sendPersonalMessage(this, nickNameTo, msg);
            } else if (message.startsWith("/newnick ")) {
                String[] parts = message.split(" ");
                String newNick = parts[1];
                if (newNick.isEmpty()) {
                    server.sendPersonalMessage(this, getNickName(), "Требуется указать новый никнейм");
                } else {
                    if (server.getAuthService().changeNickName(login, newNick)) {
                        this.nickName = newNick;
                        server.sendPersonalMessage(this, getNickName(), "Никнейм изменен");
                    } else {
                        server.sendPersonalMessage(this, getNickName(), "Никнейм изменить не удалось");
                    }
                }
            } else if (!message.startsWith("/")) {
                server.broadcastMessage(message);
            }
        }
    }

    public void closeConnection() {
        server.unsubscribe(this);
        server.broadcastMessage(nickName + " вышел из чата");
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickName() {
        return nickName;
    }
}
