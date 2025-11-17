package at.riemer.feature.worldadmin.server;

import at.riemer.core.Main;
import at.riemer.server.database.objects.DatabaseWorld;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WorldAdminManager {

    // =========================
    // WorldInfo DTO
    // =========================

    public static final class WorldInfo {
        private final String id;   // hier: worldName oder ID-String
        private final String name; // Anzeigename in der GUI

        public WorldInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId()   { return id; }
        public String getName() { return name; }
    }

    private WorldAdminManager() {}

    // =========================
    // Helper: Name/ID → RegistryKey<World>
    // =========================

    private static RegistryKey<World> resolveWorldKey(String idOrName) {
        if (idOrName == null || idOrName.trim().isEmpty()) {
            return World.OVERWORLD;
        }

        String trimmed = idOrName.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);

        // Shorthands
        if (lower.equals("overworld")) {
            return World.OVERWORLD;
        }
        if (lower.equals("nether")) {
            return World.THE_NETHER;
        }
        if (lower.equals("the_end") || lower.equals("end")) {
            return World.THE_END;
        }

        // Wenn schon ein Namespace drin ist, direkt als ResourceLocation nutzen
        ResourceLocation rl;
        if (trimmed.contains(":")) {
            rl = new ResourceLocation(trimmed);
        } else {
            // sonst unter deinem Mod-Namespace
            rl = new ResourceLocation(Main.MOD_ID, lower);
        }

        return RegistryKey.getOrCreateKey(Registry.WORLD_KEY, rl);
    }

    // =========================
    // Verfügbare Welten aus der DB
    // =========================

    /**
     * Holt alle verfügbaren Welten aus der Datenbank.
     * Fällt zurück auf ein paar Standardwelten, wenn DB leer ist.
     */
    public static List<WorldInfo> getAvailableWorlds(MinecraftServer server) {
        List<WorldInfo> result = new ArrayList<>();

        try {
            List<DatabaseWorld> dbWorlds = DatabaseWorld.getAllWorlds();
            for (DatabaseWorld db : dbWorlds) {
                String worldName = db.getWorldName();
                String displayName = worldName + " [" + db.getWorldType() + "]";
                result.add(new WorldInfo(worldName, displayName));
            }
        } catch (SQLException e) {
            System.err.println("[WorldAdmin] Fehler beim Laden der Welten aus der DB:");
            e.printStackTrace();
        }

        // Falls noch nichts in der DB ist → ein paar Default-Einträge anzeigen
        if (result.isEmpty()) {
            result.add(new WorldInfo("overworld", "Overworld (default)"));
            result.add(new WorldInfo("nether", "Nether (default)"));
            result.add(new WorldInfo("end", "The End (default)"));
        }

        return result;
    }

    // =========================
    // Einfacher Teleport (mit dynamic create)
    // =========================

    public static void teleportToWorld(ServerPlayerEntity player, String worldId) {
        if (player == null || worldId == null) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        // worldId ist hier der Name/Key, den du in der GUI verwendest
        RegistryKey<World> worldKey = ArkDimensionFactory.worldKeyFromName(worldId);

        ServerWorld targetWorld = ArkDynamicDimensionHelper.getOrCreateWorld(
                server,
                worldKey,
                (srv, dimKey) -> {
                    // worldType im Zweifel aus der DB nachladen
                    String type = "default";
                    try {
                        DatabaseWorld dbWorld = DatabaseWorld.loadByName(worldId);
                        if (dbWorld != null) {
                            type = dbWorld.getWorldType();
                        }
                    } catch (Exception ignored) {}

                    return ArkDimensionFactory.createDimensionForType(srv, dimKey, type);
                }
        );

        if (targetWorld == null) {
            System.out.println("[WorldAdmin] teleportToWorld: Zielwelt '" + worldId + "' (" + worldKey.getLocation() + ") konnte nicht erzeugt werden.");
            return;
        }

        BlockPos spawn = targetWorld.getSpawnPoint();
        player.teleport(
                targetWorld,
                spawn.getX() + 0.5D,
                spawn.getY() + 1.0D,
                spawn.getZ() + 0.5D,
                player.rotationYaw,
                player.rotationPitch
        );
    }

    // =========================
    // Create / Load über DatabaseWorld
    // =========================

    /**
     * Create-or-Load anhand WorldName/Type/Seed:
     *  - legt einen Eintrag in der DB an (falls noch nicht vorhanden)
     *  - updatet ggf. world_type
     *  - versucht dann, eine Dimension zu holen / zu erzeugen und teleportiert dorthin
     */
    public static void createOrLoadWorld(ServerPlayerEntity player,
                                         String worldName,
                                         String worldType,
                                         Long seed) {
        if (player == null) return;
        if (worldName == null || worldName.trim().isEmpty()) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        worldName = worldName.trim();

        if (worldType == null || worldType.trim().isEmpty()) {
            worldType = "NORMAL";
        }
        // >>> HIER: final Kopie für Lambda
        final String finalWorldType = worldType;

        // --- DB: DatabaseWorld-Eintrag anlegen / updaten ---
        try {
            DatabaseWorld dbWorld = DatabaseWorld.loadByName(worldName);
            if (dbWorld == null) {
                dbWorld = new DatabaseWorld(worldName, player.getUniqueID(), finalWorldType);
                dbWorld.save();
                System.out.println("[WorldAdmin] Created DB entry for world '" + worldName + "' type=" + finalWorldType);
            } else {
                if (!finalWorldType.equals(dbWorld.getWorldType())) {
                    dbWorld.setWorldType(finalWorldType);
                    dbWorld.save();
                    System.out.println("[WorldAdmin] Updated DB entry for world '" + worldName + "' -> type=" + finalWorldType);
                }
            }
        } catch (SQLException e) {
            System.err.println("[WorldAdmin] Failed to save/load DatabaseWorld for '" + worldName + "'");
            e.printStackTrace();
        }

        // --- Zuerst versuchen: existierende Welt auf dem Server finden ---
        RegistryKey<World> existingKey = resolveWorldKey(worldName);
        ServerWorld targetWorld = server.getWorld(existingKey);

        if (targetWorld == null) {
            // World-Key für unsere neue Dimension, z.B. arkcraft:test
            RegistryKey<World> worldKey = ArkDimensionFactory.worldKeyFromName(worldName);

            System.out.println("[WorldAdmin] Target world '" + worldName + "' (" + worldKey.getLocation() + ") existiert noch nicht. Erzeuge dynamische Dimension...");

            ServerWorld created = ArkDynamicDimensionHelper.getOrCreateWorld(
                    server,
                    worldKey,
                    (srv, dimKey) -> ArkDimensionFactory.createDimensionForType(srv, dimKey, finalWorldType)
            );

            if (created == null) {
                System.out.println("[WorldAdmin] Dynamic dimension for '" + worldName + "' konnte nicht erzeugt werden.");
                if (seed != null) {
                    System.out.println("[WorldAdmin] Seed-Wunsch war: " + seed);
                }
                return;
            }

            System.out.println("[WorldAdmin] Dynamic dimension created for '" + worldName + "': " + created.getDimensionKey().getLocation());

            // Direkt teleportieren
            BlockPos spawn = created.getSpawnPoint();
            player.teleport(
                    created,
                    spawn.getX() + 0.5D,
                    spawn.getY() + 1.0D,
                    spawn.getZ() + 0.5D,
                    player.rotationYaw,
                    player.rotationPitch
            );
            return;
        }

        // Wenn Welt existiert → Teleport zum Spawn
        BlockPos spawn = targetWorld.getSpawnPoint();
        player.teleport(
                targetWorld,
                spawn.getX() + 0.5D,
                spawn.getY() + 1.0D,
                spawn.getZ() + 0.5D,
                player.rotationYaw,
                player.rotationPitch
        );
    }


}
