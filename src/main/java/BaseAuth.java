import java.sql.*;

public class BaseAuth implements AuthService {

    private final String url = "jdbc:mysql://localhost:3307/net_chat";
    private final String user = "root";
    private final String password = "root";

    private Connection conn = null;
    private PreparedStatement stmt = null;

    @Override
    public void start() {
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Сервис запущен");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try { if (stmt != null && !stmt.isClosed()) stmt.close(); } catch(SQLException e) { e.printStackTrace(); }
        try { if (conn != null && !conn.isClosed()) conn.close(); } catch(SQLException e) { e.printStackTrace(); }
        System.out.println("Сервис остановлен");
    }

    @Override
    public String getNickName(String login, String password) {
        try {
            if (conn == null || conn.isClosed()) return null;

            stmt = conn.prepareStatement("select nick_name from users where login = ? and password = ?");
            stmt.setString(1, login);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nick_name");
            } else {
                System.out.println("Неправильно введен логин и/или пароль");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean changeNickName(String login, String newNickName) {
        try {
            if (conn == null || conn.isClosed()) return false;

            stmt = conn.prepareStatement("update users set nick_name  = ? where login = ?");
            stmt.setString(1, newNickName);
            stmt.setString(2, login);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                System.out.println("Не удалось сменить никнейм у пользователя с логином " + login);
            }
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
