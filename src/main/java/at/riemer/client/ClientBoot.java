package at.riemer.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


@Mod.EventBusSubscriber(modid = "arkcraft", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientBoot{
    @SubscribeEvent
    public static void onClientSetup(final net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent e) {

        ClientConfig.setArgs();

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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientBoot::onClientSetup);
    }

}
