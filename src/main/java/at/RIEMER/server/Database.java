package at.RIEMER.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    String url;
    String user;
    String pass;
    public Database(){

        url = "jdbc:mysql://localhost:3306/ArkCraft";
        user = "root";
        pass = "felix123";
    }

    public void runDB(){

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("✅ Connected to database!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
