package at.riemer.server.database.objects;

import at.riemer.server.database.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseWorld {

    private String worldName;
    private UUID creatorUuid;
    private Timestamp dateCreated;
    private String worldType;

    public Connection conn = Database.connection;

    // =========================
    // Konstruktoren
    // =========================

    /**
     * Neue World, Datum wird automatisch auf "jetzt" gesetzt.
     */
    public DatabaseWorld(String worldName, UUID creatorUuid, String worldType) {
        this(worldName, creatorUuid, currentTimestamp(), worldType);
    }

    /**
     * Vollständiger Konstruktor (z.B. beim Laden aus der DB).
     */
    public DatabaseWorld(String worldName, UUID creatorUuid,
                         Timestamp dateCreated, String worldType) {
        this.worldName = worldName;
        this.creatorUuid = creatorUuid;
        this.dateCreated = dateCreated;
        this.worldType = worldType;
    }

    // =========================
    // Helper: aktueller Zeitpunkt
    // =========================

    public static Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    // =========================
    // SCHEMA / TABLE-SETUP
    // =========================

    /**
     * Stellt sicher, dass die Tabelle 'worlds' existiert und alle Spalten hat.
     */
    public static void ensureTable() throws SQLException {
        Connection conn = Database.connection;
        if (conn == null) {
            throw new SQLException("Database.connection is null");
        }

        // 1) Tabelle erstellen, falls sie nicht existiert
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS worlds (" +
                            "world_name    VARCHAR(128) NOT NULL PRIMARY KEY," +
                            "creator_uuid  CHAR(36)     NOT NULL," +
                            "date_created  TIMESTAMP    NOT NULL," +
                            "world_type    VARCHAR(32)  NOT NULL" +
                            ")"
            );
        }

        // 2) Sicherstellen, dass alle Spalten existieren
        ensureColumn(conn, "worlds", "world_name",   "VARCHAR(128) NOT NULL PRIMARY KEY");
        ensureColumn(conn, "worlds", "creator_uuid", "CHAR(36)     NOT NULL");
        ensureColumn(conn, "worlds", "date_created", "TIMESTAMP    NOT NULL");
        ensureColumn(conn, "worlds", "world_type",   "VARCHAR(32)  NOT NULL");
    }

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

        String sql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + columnDef;
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    // =========================
    // SPEICHERN / LADEN
    // =========================

    /**
     * Speichert die World. Bei gleichem world_name wird aktualisiert.
     */
    public void save() throws SQLException {
        String sql =
                "INSERT INTO worlds (world_name, creator_uuid, date_created, world_type) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "  creator_uuid = VALUES(creator_uuid), " +
                        // date_created typischerweise nicht überschreiben
                        "  world_type   = VALUES(world_type)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, worldName);
            ps.setString(2, creatorUuid.toString());
            ps.setTimestamp(3, dateCreated);
            ps.setString(4, worldType);
            ps.executeUpdate();
        }
    }

    /**
     * Lädt eine World anhand ihres Namens.
     */
    public static DatabaseWorld loadByName(String worldName) throws SQLException {
        Connection conn = Database.connection;
        if (conn == null) {
            throw new SQLException("Database.connection is null");
        }

        String sql = "SELECT world_name, creator_uuid, date_created, world_type " +
                "FROM worlds WHERE world_name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, worldName);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String wName = rs.getString("world_name");
                UUID creator = UUID.fromString(rs.getString("creator_uuid"));
                Timestamp created = rs.getTimestamp("date_created");
                String type = rs.getString("world_type");

                return new DatabaseWorld(wName, creator, created, type);
            }
        }
    }

    /**
     * Lädt alle Welten aus der DB.
     */
    public static List<DatabaseWorld> getAllWorlds() throws SQLException {
        Connection conn = Database.connection;
        if (conn == null) {
            throw new SQLException("Database.connection is null");
        }

        List<DatabaseWorld> result = new ArrayList<>();

        String sql = "SELECT world_name, creator_uuid, date_created, world_type FROM worlds";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String wName = rs.getString("world_name");
                    UUID creator = UUID.fromString(rs.getString("creator_uuid"));
                    Timestamp created = rs.getTimestamp("date_created");
                    String type = rs.getString("world_type");

                    result.add(new DatabaseWorld(wName, creator, created, type));
                }
            }
        }

        return result;
    }

    // =========================
    // Getter / Setter
    // =========================

    public String getWorldName() {
        return worldName;
    }

    public UUID getCreatorUuid() {
        return creatorUuid;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public String getWorldType() {
        return worldType;
    }

    public void setWorldType(String worldType) {
        this.worldType = worldType;
    }
}
