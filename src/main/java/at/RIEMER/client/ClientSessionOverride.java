package at.RIEMER.client;

import at.RIEMER.core.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.util.Session;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientSessionOverride {

    private static final Logger LOG = LogManager.getLogger("ArkCraft");

    // Wird nur einmal gemacht (beim ersten MainMenu)
    private static boolean applied = false;

    // SRG-Name von Minecraft.session in 1.16.x
    private static final Field SESSION_FIELD =
            ObfuscationReflectionHelper.findField(Minecraft.class, "field_71449_j");

    public static void onGuiOpen(GuiOpenEvent e) {
        if (applied) return;
        if (!(e.getGui() instanceof MainMenuScreen)) return;

        applied = true;
        Minecraft mc = Minecraft.getInstance();
        applyCustomSessionFromConfig(mc);
    }

    private static void applyCustomSessionFromConfig(Minecraft mc) {
        String name = ClientConfig.PlayerName.trim();

        // Offline-kompatible UUID (wie der Server sie berechnet)
        String offlineUuid = UUID
                .nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes())
                .toString()
                .replace("-", "");

        // Token → kann "0" sein, Offlinemode ist egal
        String token = "0";

        // Session-Type → "mojang" oder "legacy"
        // In 1.16 normalerweise "legacy", aber "mojang" funktioniert genauso
        String sessionType = "legacy";

        Session custom = new Session(
                name,
                offlineUuid,
                token,
                sessionType
        );



        try {
            SESSION_FIELD.setAccessible(true);
            SESSION_FIELD.set(mc, custom);
            LOG.info("[ArkCraft] Custom session applied: name='{}', uuid={}", name, offlineUuid);
        } catch (Exception ex) {
            LOG.error("[ArkCraft] Failed to set custom Session", ex);
        }
    }

    private ClientSessionOverride() {}
}
