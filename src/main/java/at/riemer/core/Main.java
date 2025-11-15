package at.riemer.core;

import at.riemer.client.mixin.EarlyMixinLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Main.MOD_ID)
public class Main {
    public static final String MOD_ID = "arkcraft";
    public static final Logger LOGGER = LogManager.getLogger("ArkCraft");

    private int nextPacketId = 0;
// Main.java

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        // clientSetup registrieren ist okay – das Event feuert nur auf dem echten Client
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            EarlyMixinLoader.load();
        }

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("[ArkCraft] Initialized on dist: {}", FMLEnvironment.dist);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // COMMON + S2C Packet IDs überall registrieren
        nextPacketId = ArkNetwork.registerCommonPackets();
        ArkNetwork.registerClientPackets(nextPacketId);

        Main.LOGGER.info("[ArkCraft] All packets registered up to ID " + (nextPacketId - 1));
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // hier später Keybinds, Client-Configs etc.
        Main.LOGGER.info("[ArkCraft] Client setup finished");
    }

}


