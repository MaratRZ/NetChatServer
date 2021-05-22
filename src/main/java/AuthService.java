import java.sql.SQLException;

public interface AuthService {
    void start();
    void stop();
    String getNickName(String login, String password);
    boolean changeNickName(String login, String newNickName);
}
