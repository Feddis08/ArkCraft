package at.riemer.core;

import at.riemer.client.mixin.EarlyMixinLoader;
import at.riemer.server.ServerBoot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

@Mod(Main.MOD_ID)
public class Main {
    public static final String MOD_ID = "arkcraft";
    public static final Logger LOGGER = LogManager.getLogger("ArkCraft");

    public static final String MIXIN_EARLY  = "arkcraft-early.mixin.json"; // Splash/Logo
    public static final String MIXIN_NORMAL = "arkcraft.mixin.json";       // Chat/UI etc.


    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        if (FMLEnvironment.dist == Dist.CLIENT)
            EarlyMixinLoader.load();

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("[ArkCraft] Initialized on dist: {} (production: {})",
                FMLEnvironment.dist, FMLEnvironment.production);
    }
    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            at.riemer.network.ArkNetwork.register();
        });
    }
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) throws SQLException {
        LOGGER.info("ArkCraft: Server starting");
        ServerBoot.boot();
    }
}




