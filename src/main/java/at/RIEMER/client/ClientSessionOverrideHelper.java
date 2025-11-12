package at.RIEMER.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class ClientSessionOverrideHelper {

    private static final Logger LOG = LogManager.getLogger("ArkCraft");

    // SRG-Name des 'session'-Feldes in Minecraft (1.16.x)
    // private Session session;
    private static final Field SESSION_FIELD =
            ObfuscationReflectionHelper.findField(Minecraft.class, "field_71449_j");

    private ClientSessionOverrideHelper() {}

    /**
     * Setzt den Spielernamen des aktuellen Clients zur Laufzeit um.
     * Erzeugt eine Offline-UUID basierend auf dem Namen und übernimmt
     * Token und Session-Typ aus der alten Session.
     */
    public static void applyTemporarySessionName(String newName) {
        if (newName == null) return;
        newName = newName.trim();
        if (newName.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            LOG.warn("[ArkCraft] Cannot override session: Minecraft instance is null.");
            return;
        }

        Session old = mc.getSession();
        if (old == null) {
            LOG.warn("[ArkCraft] Cannot override session: old session is null.");
            return;
        }

        try {
            // Offline-UUID im selben Stil, wie der Server sie benutzt:
            // UUID.nameUUIDFromBytes("OfflinePlayer:" + name)
            String uuid = UUID
                    .nameUUIDFromBytes(("OfflinePlayer:" + newName).getBytes(StandardCharsets.UTF_8))
                    .toString()
                    .replace("-", "");

// Du kannst hier auch einfach "0" nehmen, dann versucht Realms gar nicht erst JWT zu parsen,
// oder der alte Token ist dir egal – offline sowieso wurscht.
            String token = "0";

// Session-Type: "legacy" reicht völlig im Offlinemode
            String sessionType = "legacy";

            Session custom = new Session(
                    newName,
                    uuid,
                    token,
                    sessionType
            );


            SESSION_FIELD.setAccessible(true);
            SESSION_FIELD.set(mc, custom);

            LOG.info("[ArkCraft] Session overridden: '{}' -> '{}' (uuid={})",
                    old.getUsername(), newName, uuid);
        } catch (Exception e) {
            LOG.error("[ArkCraft] Failed to override client Session", e);
        }
    }
    public static void applyNameFromTokenIfPresent() {
        String token = at.RIEMER.client.ClientTokenStorage.getTokenOrNull();
        if (token == null || token.isEmpty()) return;

        String username = at.RIEMER.client.TokenPayloadHelper.extractUsernameFromToken(token);
        if (username == null || username.isEmpty()) return;

        applyTemporarySessionName(username);
    }

}
