package at.RIEMER.server.database;

import at.RIEMER.server.database.objects.DatabasePlayer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private final String url;
    private final String user;
    private final String pass;

    public static Connection connection;

    public Database() {
        // TODO: Credentials aus Config laden, nicht hardcodieren
        this.url  = "jdbc:mysql://localhost:3306/ArkCraft";
        this.user = "root";
        this.pass = "felix123";
    }

    /**
     * Stellt eine Verbindung zur DB her und richtet das Schema ein.
     * Sollte einmal beim Serverstart aufgerufen werden.
     */
    public void runDB() {
        try {
            // 1) Verbindung herstellen und in static Feld ablegen
            connection = DriverManager.getConnection(url, user, pass);
            System.out.println("✅ Connected to database!");

            // Wenn du explizit ein Schema setzen willst:
            // connection.setSchema("ArkCraft");
            // Bei URL mit DB-Namen ist das meistens nicht nötig.

            // 2) Tabellen sicherstellen
            DatabasePlayer.ensureTable();

        } catch (SQLException e) {
            e.printStackTrace();
            // Optional: Serverstart abbrechen
            // throw new RuntimeException("Could not connect to database", e);
        }
    }
}
