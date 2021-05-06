import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class BaseAuth implements AuthService {

    private static final String url = "jdbc:mysql://localhost:3307/net_chat";
    private static final String user = "root";
    private static final String password = "root";

    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;
    Map<String, User> users;

    @Override
    public void start() {
        String query = "select login, password, nick_name from users";
        try {
            users = new HashMap<>();

            con = DriverManager.getConnection(url, user, password);
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                users.put(rs.getString("login"), new User(rs.getString("login"), rs.getString("password"), rs.getString("nick_name")));
            }

        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            try { rs.close(); } catch(SQLException se) {  }
            try { stmt.close(); } catch(SQLException se) {  }
            try { con.close(); } catch(SQLException se) {  }
        }
    }

    @Override
    public void stop() {
        System.out.println("Пользователь отключился");
    }

    @Override
    public String getNickName(String login, String password) {
        User user = users.get(login);
        if (user != null && user.getPassword().equals(password)) {
            return user.getNickName();
        }
        return null;
    }
}
