package at.RIEMER.core;

import at.RIEMER.server.ServerBoot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// mixin
import org.spongepowered.asm.mixin.Mixins;

import java.util.concurrent.atomic.AtomicBoolean;

@Mod(Main.MOD_ID)
public class Main {
    public static final String MOD_ID = "arkcraft";
    private static final Logger LOGGER = LogManager.getLogger("ArkCraft");

    // avoid adding the same mixin config twice (e.g., in hot-reload/dev)
    private static final AtomicBoolean MIXIN_ADDED = new AtomicBoolean(false);
    private static final String MIXIN_CONFIG = "arkcraft.mixin.json";

    public Main() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            // force client to be non-demo, enable multiplayer
            try {
                Class<?> mcClass = Class.forName("net.minecraft.client.Minecraft");
                java.lang.reflect.Field demoField = mcClass.getDeclaredField("field_71362_n"); // isDemo
                demoField.setAccessible(true);
                demoField.set(null, false);
                LOGGER.info("[ArkCraft] Forced Minecraft out of demo mode.");
            } catch (Exception e) {
                LOGGER.warn("[ArkCraft] Could not disable demo mode: {}", e.toString());
            }

            System.setProperty("allowInsecureLocalConnections", "true");
            System.setProperty("fml.doNotCheckOnline", "true");
            System.setProperty("user.name", "Felix");
            Mixins.addConfiguration("arkcraft.mixin.json");
            LOGGER.info("[ArkCraft] Client mixins loaded and multiplayer unlocked.");
        } else {
            // Dedicated server headless
            System.setProperty("nogui", "true");
            System.setProperty("forge.server.noGui", "true");
            System.setProperty("java.awt.headless", "true");
            LOGGER.info("[ArkCraft] Headless server mode enabled.");
        }

        LOGGER.info("ArkCraft initialized on dist: {}", FMLEnvironment.dist);


        // Always: common setup listener
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

        // Client-only: register client listeners + mixin config
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSide::init);

        // Forge event bus for server/common events
        MinecraftForge.EVENT_BUS.register(this);

        // Log basic environment info to help diagnose side issues
        LOGGER.info("[ArkCraft] Loaded on {} (production: {})",
                FMLEnvironment.dist, FMLEnvironment.production);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("ArkCraft: Common setup");
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        LOGGER.info("ArkCraft: Server starting");
        ServerBoot.boot();
    }

    /** All client-only wiring lives here so it never touches a dedicated server. */
    private static final class ClientSide implements DistExecutor.SafeRunnable {
        static void init() {
            // client lifecycle listener
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSide::clientSetup);

            // defensively add mixin config on client only
            try {
                if (MIXIN_ADDED.get()) {
                    LOGGER.debug("[ArkCraft] Mixin config already added; skipping.");
                    return;
                }

                if (Main.class.getClassLoader().getResource(MIXIN_CONFIG) != null) {
                    Mixins.addConfiguration(MIXIN_CONFIG);
                    MIXIN_ADDED.set(true);
                    LOGGER.info("[ArkCraft] Registered mixin config: {}", MIXIN_CONFIG);
                } else {
                    LOGGER.error("[ArkCraft] Mixin config NOT found on classpath: {}", MIXIN_CONFIG);
                }
            } catch (Throwable t) {
                // never crash the game if mixin registration has an issue
                LOGGER.error("[ArkCraft] Failed to add mixin configuration: {}", MIXIN_CONFIG, t);
            }
        }

        private static void clientSetup(final net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
            LOGGER.info("ArkCraft: Client setup");
        }

        @Override
        public void run() {
            init();
        }
    }
}
