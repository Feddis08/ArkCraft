package at.RIEMER.client;

import at.RIEMER.core.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PortalScreenOpener {

    private static boolean opened = false;

    @SubscribeEvent
    public static void onGuiOpen(GuiOpenEvent e) {
        // Nur beim ersten Öffnen des Hauptmenüs
        if (opened) return;
        if (!(e.getGui() instanceof MainMenuScreen)) return;

        opened = true;

        // Original-Hauptmenü merken, damit wir später zurück können
        Minecraft mc = Minecraft.getInstance();
        // e.getGui() ist das MainMenuScreen-Objekt
        ArkPortalScreen portal = new ArkPortalScreen();

        // Ersatz-GUI setzen
        e.setGui(portal);
    }

    private PortalScreenOpener() {}
}
