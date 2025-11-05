package at.RIEMER.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "arkcraft", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientInit {
    @SubscribeEvent
    public static void onClientSetup(final net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();

            mc.gameSettings.keyBindChat.setKeyModifierAndCode(
                    net.minecraftforge.client.settings.KeyModifier.NONE,
                    net.minecraft.client.util.InputMappings.Type.KEYSYM.getOrMakeInput(org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN)
            );
            mc.gameSettings.keyBindCommand.setKeyModifierAndCode(
                    net.minecraftforge.client.settings.KeyModifier.NONE,
                    net.minecraft.client.util.InputMappings.Type.KEYSYM.getOrMakeInput(org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN)
            );

            net.minecraft.client.settings.KeyBinding.resetKeyBindingArrayAndHash();
        });
    }
}
