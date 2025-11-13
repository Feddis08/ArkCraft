package at.riemer.feature.worldadmin.server;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WorldAdminManager {

    public static final class WorldInfo {
        private final String id;   // z.B. "minecraft:overworld"
        private final String name; // Anzeigename

        public WorldInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId()   { return id; }
        public String getName() { return name; }
    }

    private WorldAdminManager() {}

    /**
     * Holt alle verfügbaren Welten (Dimensionen) auf dem Server.
     */
    public static List<WorldInfo> getAvailableWorlds(MinecraftServer server) {
        if (server == null) return Collections.emptyList();

        List<WorldInfo> result = new ArrayList<>();
        for (ServerWorld world : server.getWorlds()) {
            RegistryKey<World> key = world.getDimensionKey();
            String id = key.getLocation().toString();
            String displayName;
            if (world.getDimensionKey() == World.OVERWORLD) {
                displayName = "Overworld";
            } else if (world.getDimensionKey() == World.THE_NETHER) {
                displayName = "Nether";
            } else if (world.getDimensionKey() == World.THE_END) {
                displayName = "The End";
            } else {
                displayName = key.getLocation().toString();
            }

            result.add(new WorldInfo(id, displayName));
        }
        return result;
    }

    /**
     * Teleportiert den Spieler in die angegebene Welt (Dimension).
     */
    public static void teleportToWorld(ServerPlayerEntity player, String dimensionId) {
        if (player == null || dimensionId == null) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        RegistryKey<World> key = RegistryKey.getOrCreateKey(
                Registry.WORLD_KEY,
                new ResourceLocation(dimensionId)
        );
        ServerWorld targetWorld = server.getWorld(key);
        if (targetWorld == null) {
            // TODO: optional: Spieler im Chat informieren, dass die Welt nicht existiert
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
}
