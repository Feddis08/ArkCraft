package at.RIEMER.client.guis;

import at.RIEMER.client.guis.ArkPortalScreen;
import at.RIEMER.core.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PortalScreenOpener {


    @SubscribeEvent
    public static void onGuiOpen(GuiOpenEvent e) {
        Screen gui = e.getGui();

        if (gui instanceof ArkPortalScreen) return;


        if (gui instanceof MainMenuScreen
                || gui instanceof MultiplayerScreen
                || gui instanceof WorldSelectionScreen) {

            // override: Portal statt original Screen
            e.setGui(new ArkPortalScreen());
        }



    }

    private PortalScreenOpener() {}
}
