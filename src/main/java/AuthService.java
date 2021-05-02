public interface AuthService {
    void start();
    void stop();
    String getNickName(String login, String password);
}
