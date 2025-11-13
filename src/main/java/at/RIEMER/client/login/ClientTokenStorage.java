package at.RIEMER.client.login;

import at.RIEMER.core.util.FileHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;

public final class ClientTokenStorage {

    private static final Logger LOG = LogManager.getLogger("ArkCraft");
    private static final String TOKEN_FILE = "arkcraft/token.txt";

    private static String cachedToken = null;
    private static boolean loaded = false;

    private ClientTokenStorage() {}

    public static boolean hasToken() {
        ensureLoaded();
        return cachedToken != null && !cachedToken.isEmpty();
    }

    public static String getTokenOrNull() {
        ensureLoaded();
        return cachedToken;
    }
    public static void removeToken() {
        try {
            cachedToken = null;
            loaded = false;
            FileHelper.writeFileUtf8(Paths.get(TOKEN_FILE), "");
            LOG.info("[ArkCraft] Token removed.");
        } catch (IOException e) {
            LOG.error("[ArkCraft] Failed to remove token", e);
        }
    }

    public static void saveToken(String token) {
        cachedToken = token != null ? token.trim() : null;
        try {
            if (cachedToken == null || cachedToken.isEmpty()) {
                // optional: Datei löschen
                return;
            }
            FileHelper.writeFileUtf8(Paths.get(TOKEN_FILE), cachedToken);
            LOG.info("[ArkCraft] Token saved to {}", TOKEN_FILE);
        } catch (IOException e) {
            LOG.error("[ArkCraft] Failed to save token", e);
        }
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        try {
            String txt = FileHelper.readFileUtf8(Paths.get(TOKEN_FILE)).trim();
            if (!txt.isEmpty()) {
                cachedToken = txt;
                LOG.info("[ArkCraft] Loaded token from {}", TOKEN_FILE);
            }
        } catch (IOException e) {
            // Kein Token vorhanden -> ist okay
            cachedToken = null;
        }
    }
}
