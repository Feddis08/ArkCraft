package at.RIEMER.client.guis;

import at.RIEMER.client.guis.ArkPortalScreen;
import at.RIEMER.core.Main;
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
        if (opened) return;
        if (!(e.getGui() instanceof MainMenuScreen)) return;

        opened = true;
        e.setGui(new ArkPortalScreen());
    }

    private PortalScreenOpener() {}
}
