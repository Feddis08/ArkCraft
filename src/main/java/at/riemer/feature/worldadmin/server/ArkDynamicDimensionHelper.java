package at.riemer.feature.worldadmin.server;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Dimension;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class ArkDynamicDimensionHelper {

    // private Felder aus MinecraftServer via Reflection
    public static final Function<MinecraftServer, IChunkStatusListenerFactory> CHUNK_STATUS_LISTENER_FACTORY_FIELD =
            getInstanceField(MinecraftServer.class, "field_213220_d");
    public static final Function<MinecraftServer, Executor> BACKGROUND_EXECUTOR_FIELD =
            getInstanceField(MinecraftServer.class, "field_213217_au");
    public static final Function<MinecraftServer, SaveFormat.LevelSave> LEVEL_SAVE_FIELD =
            getInstanceField(MinecraftServer.class, "field_71310_m");

    private ArkDynamicDimensionHelper() {}

    /**
     * Holt eine Welt; wenn sie noch nicht existiert, wird sie erstellt und registriert.
     */
    public static ServerWorld getOrCreateWorld(
            MinecraftServer server,
            RegistryKey<World> worldKey,
            BiFunction<MinecraftServer, RegistryKey<net.minecraft.world.Dimension>, Dimension> dimensionFactory
    ) {
        @SuppressWarnings("deprecation")
        Map<RegistryKey<World>, ServerWorld> worldMap = server.forgeGetWorldMap();

        ServerWorld existing = worldMap.get(worldKey);
        if (existing != null) {
            return existing;
        }

        return createAndRegisterWorld(server, worldMap, worldKey, dimensionFactory);
    }

    /**
     * Spieler in eine bestimmte ServerWorld teleportieren (Chunk vorher laden).
     */
    public static void sendPlayerToWorld(ServerWorld targetWorld, ServerPlayerEntity player, Vector3d targetPos) {
        targetWorld.getChunk(new BlockPos(targetPos));
        player.teleport(
                targetWorld,
                targetPos.x, targetPos.y, targetPos.z,
                player.rotationYaw, player.rotationPitch
        );
    }

    @SuppressWarnings("deprecation")
    private static ServerWorld createAndRegisterWorld(
            MinecraftServer server,
            Map<RegistryKey<World>, ServerWorld> worldMap,
            RegistryKey<World> worldKey,
            BiFunction<MinecraftServer, RegistryKey<net.minecraft.world.Dimension>, Dimension> dimensionFactory
    ) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);

        // Dimension-Key (für Dimension-Registry) ist 1:1 der World-Key
        RegistryKey<net.minecraft.world.Dimension> dimensionKey =
                RegistryKey.getOrCreateKey(Registry.DIMENSION_KEY, worldKey.getLocation());

        Dimension dimension = dimensionFactory.apply(server, dimensionKey);

        IChunkStatusListenerFactory listenerFactory = CHUNK_STATUS_LISTENER_FACTORY_FIELD.apply(server);
        IChunkStatusListener chunkListener = listenerFactory.create(11);
        Executor executor = BACKGROUND_EXECUTOR_FIELD.apply(server);
        SaveFormat.LevelSave levelSave = LEVEL_SAVE_FIELD.apply(server);

        IServerConfiguration serverConfig = server.getServerConfiguration();
        DimensionGeneratorSettings dimSettings = serverConfig.getDimensionGeneratorSettings();

        // Dimension in Registry eintragen
        dimSettings.func_236224_e_().register(dimensionKey, dimension, Lifecycle.experimental());

        DerivedWorldInfo derivedWorldInfo = new DerivedWorldInfo(serverConfig, serverConfig.getServerWorldInfo());

        boolean isDebugWorld = false;

        ServerWorld newWorld = new ServerWorld(
                server,
                executor,
                levelSave,
                derivedWorldInfo,
                worldKey,
                dimension.getDimensionType(),
                chunkListener,
                dimension.getChunkGenerator(),
                isDebugWorld,
                BiomeManager.getHashedSeed(dimSettings.getSeed()),
                ImmutableList.of(),
                false
        );

        // Worldborder koppeln
        overworld.getWorldBorder().addListener(new IBorderListener.Impl(newWorld.getWorldBorder()));

        // Welt im Server registrieren
        worldMap.put(worldKey, newWorld);
        server.markWorldsDirty();

        // Forge-Event feuern
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(newWorld));

        return newWorld;
    }

    /**
     * Kleine Reflection-Hilfe für private Felder.
     */
    @SuppressWarnings("unchecked")
    private static <OWNER, TYPE> Function<OWNER, TYPE> getInstanceField(Class<OWNER> ownerClass, String fieldName) {
        Field field = ObfuscationReflectionHelper.findField(ownerClass, fieldName);
        return instance -> {
            try {
                return (TYPE) field.get(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
