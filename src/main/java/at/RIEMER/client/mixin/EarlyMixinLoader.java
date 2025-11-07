package at.RIEMER.client.mixin;
import at.RIEMER.core.Main;
import org.spongepowered.asm.mixin.Mixins;

import java.util.concurrent.atomic.AtomicBoolean;

public final class EarlyMixinLoader {
    // Deine Konfigs – Namen müssen 1:1 so in resources liegen
    public static final String MIXIN_EARLY  = "arkcraft-early.mixin.json"; // vor Splash/UI
    public static final String MIXIN_NORMAL = "arkcraft.mixin.json";       // restliche Patches

    private static final AtomicBoolean DID = new AtomicBoolean(false);

    private EarlyMixinLoader() {}

    /** So früh wie möglich im @Mod-Konstruktor (CLIENT) aufrufen. */
    public static void load() {
        if (!DID.compareAndSet(false, true)) return;

        try {
            // WICHTIG: Forge 1.16 initialisiert Mixin bereits – kein MixinBootstrap.init() aufrufen!
            Mixins.addConfiguration(MIXIN_EARLY);
            Mixins.addConfiguration(MIXIN_NORMAL);
            Main.LOGGER.info("[ArkCraft] Early mixins loaded: {}, {}", MIXIN_EARLY, MIXIN_NORMAL);
        } catch (Throwable t) {
            Main.LOGGER.error("[ArkCraft] Failed to load early mixins", t);
        }
    }
}