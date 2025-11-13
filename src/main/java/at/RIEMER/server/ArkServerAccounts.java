package at.RIEMER.server;

import at.RIEMER.core.util.SignedTokenHelper;
import at.RIEMER.network.ArkNetwork;
import at.RIEMER.network.packet.S2CRegistrationErrorPacket;
import at.RIEMER.network.packet.S2CTokenIssuedPacket;
import at.RIEMER.server.database.Database;
import at.RIEMER.server.database.objects.DatabasePlayer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public final class ArkServerAccounts {

    private static final Logger LOG = LogManager.getLogger("ArkCraft");
    private static final String SECRET_KEY = "CHANGE_ME_IN_CONFIG"; // TODO: aus Config laden

    // Simplest possible: nur Namen merken

    private ArkServerAccounts() {}

    public static void handleAuthHello(ServerPlayerEntity player, String tokenOrEmpty) throws SQLException {
        String name = player.getGameProfile().getName();
        DatabasePlayer dbPlayer = DatabasePlayer.loadByName(name);

        if (!tokenOrEmpty.isEmpty()) {
            // TODO: Später: Token prüfen
            try {
                String payload = SignedTokenHelper.verifySignedToken(tokenOrEmpty, SECRET_KEY);
                LOG.info("[ArkCraft] Player {} provided valid token payload={}", name, payload);
                // hier könntest du username aus payload auslesen und verifizieren
                int now = (int) (System.currentTimeMillis() / 1000L); // Sekunden

                dbPlayer.setLastJoined(now);
                dbPlayer.save();

                return;
            } catch (IllegalArgumentException e) {
                LOG.warn("[ArkCraft] Invalid token from {}: {}", name, e.getMessage());
                player.connection.disconnect(new StringTextComponent("Invalid token - please re-register."));
                return;
            }
        }

        // Kein Token → Registrierungsversuch mit aktuellem Namen


        if (dbPlayer != null) {
            ArkNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new S2CRegistrationErrorPacket("Name already in use: " + name)
            );
            return;
        }

        // Name ist frei → registrieren
        int now = (int) (System.currentTimeMillis() / 1000L); // Sekunden
        dbPlayer = new DatabasePlayer(name, player.getUniqueID(), now);

        dbPlayer.save();

        String payload = "username=" + name + ";issuedAt=" + System.currentTimeMillis();
        String token = SignedTokenHelper.createSignedToken(payload, SECRET_KEY);

        ArkNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new S2CTokenIssuedPacket(token)
        );

        LOG.info("[ArkCraft] Registered new player '{}', token issued.", name);
    }
}
