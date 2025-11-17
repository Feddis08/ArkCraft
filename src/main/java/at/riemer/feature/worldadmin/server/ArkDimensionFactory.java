package at.riemer.feature.worldadmin.server;

import at.riemer.core.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;

import java.util.Locale;

public final class ArkDimensionFactory {

    private ArkDimensionFactory() {}

    /**
     * Macht aus einem World-Namen (GUI) einen World-Key.
     * - "overworld" / "nether" / "end" → Standarddimensionen
     * - "modid:foo" → direkt so
     * - "foo" → arkcraft:foo
     */
    public static RegistryKey<World> worldKeyFromName(String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) {
            return World.OVERWORLD;
        }

        String trimmed = worldName.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);

        if ("overworld".equals(lower)) {
            return World.OVERWORLD;
        }
        if ("nether".equals(lower)) {
            return World.THE_NETHER;
        }
        if ("end".equals(lower) || "the_end".equals(lower)) {
            return World.THE_END;
        }

        ResourceLocation rl;
        if (trimmed.contains(":")) {
            rl = new ResourceLocation(trimmed);
        } else {
            rl = new ResourceLocation(Main.MOD_ID, lower);
        }

        return RegistryKey.getOrCreateKey(Registry.WORLD_KEY, rl);
    }

    /**
     * Erzeugt ein Dimension-Objekt für eine bestimmte World-Art.
     *
     * Aktuell:
     *  - Nimmt einfach DimensionType + ChunkGenerator der Overworld
     *  - Später kannst du hier je nach worldType unterschiedliche Generatoren wählen.
     */
    public static Dimension createDimensionForType(MinecraftServer server,
                                                   RegistryKey<net.minecraft.world.Dimension> dimensionKey,
                                                   String worldType) {

        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Overworld is null – server not fully started?");
        }

        DimensionType dimensionType = overworld.getDimensionType();
        ChunkGenerator chunkGenerator = overworld.getChunkProvider().getChunkGenerator();

        // TODO: worldType ("flatworld", "normal", ...) auswerten und bei Bedarf anderen ChunkGenerator bauen.
        return new Dimension(() -> dimensionType, chunkGenerator);
    }
}
