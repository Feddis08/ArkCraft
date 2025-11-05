package at.RIEMER.core;

import at.RIEMER.client.ClientInit;
import at.RIEMER.server.ServerBoot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixins;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

@Mod(Main.MOD_ID)
public class Main {
    public static final String MOD_ID = "arkcraft";
    public static final Logger LOGGER = LogManager.getLogger("ArkCraft");

    private static final String MIXIN_EARLY  = "arkcraft-early.mixin.json"; // Splash/Logo
    private static final String MIXIN_NORMAL = "arkcraft.mixin.json";       // Chat/UI etc.

    private static final AtomicBoolean MIXIN_ADDED_EARLY  = new AtomicBoolean(false);
    private static final AtomicBoolean MIXIN_ADDED_NORMAL = new AtomicBoolean(false);

    public Main() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientInit::onClientSetup);
            // Nur harmlose System-Properties im Konstruktor (kein Zugriff auf Minecraft-Klassen!)
            System.setProperty("allowInsecureLocalConnections", "true");
            System.setProperty("fml.doNotCheckOnline", "true");
            System.setProperty("user.name", "Felix");

            // EARLY Mixins können hier hinzugefügt werden
            try {
                if (MIXIN_ADDED_EARLY.compareAndSet(false, true)) {
                    Mixins.addConfiguration(MIXIN_EARLY);
                    LOGGER.info("[ArkCraft] Early mixin loaded: {}", MIXIN_EARLY);
                }
            } catch (Throwable t) {
                LOGGER.error("[ArkCraft] Failed to add early mixin: {}", MIXIN_EARLY, t);
            }
        } else {
            // Dedicated Server headless
            System.setProperty("nogui", "true");
            System.setProperty("forge.server.noGui", "true");
            System.setProperty("java.awt.headless", "true");
            LOGGER.info("[ArkCraft] Headless server mode enabled.");
        }

        // Forge wiring
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);


        LOGGER.info("[ArkCraft] Initialized on dist: {} (production: {})",
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

}




