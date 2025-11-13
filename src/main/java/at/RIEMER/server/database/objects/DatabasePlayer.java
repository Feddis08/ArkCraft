package at.RIEMER.server.database.objects;

import at.RIEMER.server.database.Database;

import java.sql.*;
import java.util.UUID;

public class DatabasePlayer {
    private String playerName;
    private UUID uuid;
    private int joinedAt;
    private int lastJoined;
    private int gold;

    public Connection conn = Database.connection;

    public DatabasePlayer(String playerName, UUID uuid, int joinedAt) {
        this.playerName = playerName;
        this.uuid = uuid;
        this.joinedAt = joinedAt;
        this.lastJoined = joinedAt;
        this.gold = 0;
    }

    // =========================
    // SCHEMA / TABLE-SETUP
    // =========================

    /**
     * Stellt sicher, dass die Tabelle 'players' existiert und alle Spalten hat.
     * Kann z.B. einmal beim Server-Start aufgerufen werden.
     */
    public static void ensureTable() throws SQLException {
        Connection conn = Database.connection;
        if (conn == null) {
            throw new SQLException("Database.connection is null");
        }

        // 1) Tabelle erstellen, falls sie nicht existiert
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS players (" +
                            "player_name   VARCHAR(64) NOT NULL PRIMARY KEY," +
                            "uuid          CHAR(36)    NOT NULL," +
                            "joined_at     INTEGER     NOT NULL," +
                            "last_joined   INTEGER     NOT NULL," +
                            "gold          INTEGER     NOT NULL DEFAULT 0" +
                            ")"
            );
        }

        // 2) Sicherstellen, dass alle Spalten existieren
        ensureColumn(conn, "players", "player_name", "VARCHAR(64) NOT NULL PRIMARY KEY");
        ensureColumn(conn, "players", "uuid",        "CHAR(36)    NOT NULL");
        ensureColumn(conn, "players", "joined_at",   "INTEGER     NOT NULL");
        ensureColumn(conn, "players", "last_joined", "INTEGER     NOT NULL");
        ensureColumn(conn, "players", "gold",        "INTEGER     NOT NULL DEFAULT 0");
    }

    /**
     * Fügt eine Spalte hinzu, falls sie nicht existiert.
     * Funktioniert mit den meisten JDBC-Treibern (z.B. SQLite, MySQL, Postgres).
     */
    private static void ensureColumn(Connection conn,
                                     String table,
                                     String column,
                                     String columnDef) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, table, column)) {
            if (rs.next()) {
                // Spalte existiert schon
                return;
            }
        }

        // Spalte fehlt -> ALTER TABLE
        String sql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + columnDef;
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    // =========================
    // SPEICHERN / LADEN (Beispiele)
    // =========================

    public void save() throws SQLException {
        String sql =
                "INSERT INTO players (player_name, uuid, joined_at, last_joined, gold) " +
                        "VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "  uuid = VALUES(uuid), " +
                        "  joined_at = VALUES(joined_at), " +
                        "  last_joined = VALUES(last_joined), " +
                        "  gold = VALUES(gold)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerName);
            ps.setString(2, uuid.toString());
            ps.setInt(3, joinedAt);
            ps.setInt(4, lastJoined);
            ps.setInt(5, gold);
            ps.executeUpdate();
        }
    }


    public static DatabasePlayer loadByName(String name) throws SQLException {
        Connection conn = Database.connection;
        String sql = "SELECT player_name, uuid, joined_at, last_joined, gold FROM players WHERE player_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String playerName = rs.getString("player_name");
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                int joinedAt = rs.getInt("joined_at");
                int lastJoined = rs.getInt("last_joined");
                int gold = rs.getInt("gold");

                DatabasePlayer p = new DatabasePlayer(playerName, uuid, joinedAt);
                p.lastJoined = lastJoined;
                p.gold = gold;
                return p;
            }
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getJoinedAt() {
        return joinedAt;
    }

    public int getLastJoined() {
        return lastJoined;
    }

    public int getGold() {
        return gold;
    }

    public void setJoinedAt(int joinedAt) {
        this.joinedAt = joinedAt;
    }

    public void setLastJoined(int lastJoined) {
        this.lastJoined = lastJoined;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    // Getter/Setter kannst du nach Bedarf ergänzen
}
