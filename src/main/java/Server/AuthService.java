package Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AuthService {
    private static Connection connection;
    private static Statement stat;
    private static Vector<String> chatlog;
    private static final Logger LOGGER = LogManager.getLogger(AuthService.class.getName());

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:DBUsers.db");
            stat = connection.createStatement();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getNiceByLoginAndPass(String login, String pass) {
        int passHash = pass.hashCode();
        String sql = String.format("SELECT nickname FROM main where login = '%s' and password = '%d'", login, passHash);
        try {
            ResultSet rs = stat.executeQuery(sql);
            if (rs.next()) {
                return rs.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getBlByNick(String nick) {
        String sql = String.format("SELECT bl FROM main where nickname = '%s'", nick);
        try {
            ResultSet rs = stat.executeQuery(sql);
            if (rs.next()) {
                return rs.getString("bl");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addToBl(String nick, List<String> bl) {
        String nickToBl = "";
        for (String s : bl) {
            nickToBl += s + " ";
        }
        String sql = String.format("UPDATE main SET bl = '%s ' where nickname = '%s'", nickToBl, nick);
        try {

            stat.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void connectUser(String nick) {
        String sql = String.format("UPDATE main SET status = '1' where nickname = '%s'", nick);
        try {
            stat.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnectUser(String nick) {
        String sql = String.format("UPDATE main SET status = '0' where nickname = '%s'", nick);
        try {
            stat.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isAlreadyAuth(String login, String pass) {
        int passHash = pass.hashCode();
        String sql = String.format("SELECT status FROM main where login = '%s' and password = '%d'", login, passHash);
        try {
            ResultSet rs = stat.executeQuery(sql);
            if (rs.next()) {
                return rs.getBoolean("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int dbCounts(){
        String sql = String.format("SELECT count(id) from chatlog");
        try {
            ResultSet rs = stat.executeQuery(sql);
            return rs.getInt(1);
        } catch (SQLException e){
            e.printStackTrace();
        }
        return 0;
    }


    public static String loadChatLog() {
        String answer ="";
        try {
            connection.setAutoCommit(false);
            {
                String sql = String.format("SELECT value FROM chatlog");
                ResultSet rs = stat.executeQuery(sql);
                while (rs.next()) {

                    answer+=(rs.getString("value")+"\n");
                }
            }
            connection.setAutoCommit(true);
            return answer;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addToChatLog(String msg) {
        try {
                String sql = String.format("insert into chatlog (value) values ('%s')", msg);
                stat.executeUpdate(sql);

        } catch (SQLException e){
            e.printStackTrace();
        }

    }
}
