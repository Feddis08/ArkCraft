package at.riemer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.TickEvent;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = "arkcraft", value = Dist.CLIENT)
public class ChatFix {
    private static final Minecraft MC = Minecraft.getInstance();

    private static String queuedPrefix = null;
    private static boolean swallowNextCharOnce = false;

    @SubscribeEvent
    public static void onKey(InputEvent.KeyInputEvent e) {
        if (MC.currentScreen != null) return;
        if (e.getAction() != GLFW.GLFW_PRESS) return;

        if (e.getKey() == GLFW.GLFW_KEY_T || e.getKey() == GLFW.GLFW_KEY_SLASH) {
            if (MC.gameSettings.chatVisibility != ChatVisibility.FULL) {
                MC.gameSettings.chatVisibility = ChatVisibility.FULL;
                MC.gameSettings.saveOptions();
            }

            // "Verbrauche" Vanilla-Keybinds
            InputMappings.Input keyChat = MC.gameSettings.keyBindChat.getKey();
            InputMappings.Input keyCmd  = MC.gameSettings.keyBindCommand.getKey();
            KeyBinding.setKeyBindState(keyChat, false);
            KeyBinding.setKeyBindState(keyCmd, false);
            MC.gameSettings.keyBindChat.setPressed(false);
            MC.gameSettings.keyBindCommand.setPressed(false);

            // pressTime über Reflection auf 0 setzen
            try {
                Field pressTimeField = KeyBinding.class.getDeclaredField("pressTime");
                pressTimeField.setAccessible(true);
                pressTimeField.setInt(MC.gameSettings.keyBindChat, 0);
                pressTimeField.setInt(MC.gameSettings.keyBindCommand, 0);
            } catch (Exception ignored) {
                // sollte nie crashen; im schlimmsten Fall bleibt Meldung bestehen
            }

            queuedPrefix = (e.getKey() == GLFW.GLFW_KEY_SLASH) ? "/" : "";
            swallowNextCharOnce = true;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (queuedPrefix != null && MC.currentScreen == null) {
            MC.displayGuiScreen(new ChatScreen(queuedPrefix));
            queuedPrefix = null;
        }
    }

    @SubscribeEvent
    public static void onCharTyped(GuiScreenEvent.KeyboardCharTypedEvent.Pre e) {
        if (swallowNextCharOnce && e.getGui() instanceof ChatScreen) {
            e.setCanceled(true);
            swallowNextCharOnce = false;
        }
    }
}
