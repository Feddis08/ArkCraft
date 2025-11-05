package at.RIEMER.client;

import at.RIEMER.core.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DevAutoBoot {
    private static final Logger LOG = LogManager.getLogger("ArkCraft");

    // Steuerung per VM-Args (bevorzugt):
    //   -Darkcraft.skipMenu=true
    //   -Darkcraft.autoJoin=localhost:25565
    // Alternativ auch mit "arkcraft.*" (Fallback unten).
    private static boolean pending = false;
    private static HostPort target = null;

    @SubscribeEvent
    public static void onGuiOpen(GuiOpenEvent e) {
        // Nur reagieren, wenn wirklich das Hauptmenü geöffnet wird
        if (!(e.getGui() instanceof MainMenuScreen)) return;
        if (pending) return; // schon scharf geschaltet

        // Shift gedrückt? -> Auto-Connect auslassen (praktisch fürs Debuggen)
        final Minecraft mc = Minecraft.getInstance();
        long win = mc.getMainWindow().getHandle();
        boolean shiftDown =
                GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                        GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (shiftDown) {
            LOG.info("[ArkCraft] Auto-Connect bypassed (Shift held).");
            return;
        }

        // Properties lesen (beide Namen unterstützt)
        String skipProp = System.getProperty("Darkcraft.skipMenu",
                System.getProperty("arkcraft.skipMenu", "true"));
        String joinProp = System.getProperty("Darkcraft.autoJoin",
                System.getProperty("arkcraft.autoJoin", "localhost:25565"));

        boolean skipMenu = Boolean.parseBoolean(skipProp);
        if (!skipMenu || joinProp == null || joinProp.isEmpty()) return;

        target = parseHostPort(joinProp);
        pending = true; // im nächsten Client-Tick verbinden
        LOG.info("[ArkCraft] Will auto-connect to {}:{}", target.host, target.port);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (!pending || e.phase != TickEvent.Phase.END) return;

        final Minecraft mc = Minecraft.getInstance();
        // Warten, bis das Hauptmenü wirklich sichtbar ist
        if (!(mc.currentScreen instanceof MainMenuScreen)) return;

        pending = false; // genau einmal ausführen
        try {
            mc.displayGuiScreen(new ConnectingScreen(new MainMenuScreen(), mc, target.host, target.port));
            LOG.info("[ArkCraft] Auto-connecting now…");
        } catch (Throwable t) {
            pending = false; // nicht erneut versuchen
            LOG.error("[ArkCraft] Auto-boot failed: {}", t.toString(), t);
        }
    }

    // ---------------- helpers ----------------

    private static HostPort parseHostPort(String in) {
        // [IPv6]:port  |  host:port  |  host
        if (in.startsWith("[")) {
            int end = in.indexOf(']');
            String host = (end > 0) ? in.substring(1, end) : in;
            int port = 25565;
            if (end > 0 && end + 1 < in.length() && in.charAt(end + 1) == ':') {
                try { port = Integer.parseInt(in.substring(end + 2)); } catch (NumberFormatException ignored) {}
            }
            return new HostPort(host, port);
        }
        int idx = in.lastIndexOf(':');
        if (idx > 0) {
            try { return new HostPort(in.substring(0, idx), Integer.parseInt(in.substring(idx + 1))); }
            catch (NumberFormatException ignored) { /* fallthrough */ }
        }
        return new HostPort(in, 25565);
    }

    private static final class HostPort {
        final String host; final int port;
        HostPort(String h, int p) { this.host = h; this.port = p; }
    }
}
