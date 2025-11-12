package at.RIEMER.server;

import at.RIEMER.core.util.SignedTokenHelper;
import at.RIEMER.network.ArkNetwork;
import at.RIEMER.network.packet.S2CRegistrationErrorPacket;
import at.RIEMER.network.packet.S2CTokenIssuedPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public final class ArkServerAccounts {

    private static final Logger LOG = LogManager.getLogger("ArkCraft");
    private static final String SECRET_KEY = "CHANGE_ME_IN_CONFIG"; // TODO: aus Config laden

    // Simplest possible: nur Namen merken
    private static final Set<String> registeredNames = new HashSet<>();

    private ArkServerAccounts() {}

    public static void handleAuthHello(ServerPlayerEntity player, String tokenOrEmpty) {
        String name = player.getGameProfile().getName();

        if (!tokenOrEmpty.isEmpty()) {
            // TODO: Später: Token prüfen
            try {
                String payload = SignedTokenHelper.verifySignedToken(tokenOrEmpty, SECRET_KEY);
                LOG.info("[ArkCraft] Player {} provided valid token payload={}", name, payload);
                // hier könntest du username aus payload auslesen und verifizieren
                return;
            } catch (IllegalArgumentException e) {
                LOG.warn("[ArkCraft] Invalid token from {}: {}", name, e.getMessage());
                ArkNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new S2CRegistrationErrorPacket("Invalid token - please re-register.")
                );
                return;
            }
        }

        // Kein Token → Registrierungsversuch mit aktuellem Namen
        if (registeredNames.contains(name)) {
            ArkNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new S2CRegistrationErrorPacket("Name already in use: " + name)
            );
            return;
        }

        // Name ist frei → registrieren
        registeredNames.add(name);

        String payload = "username=" + name + ";issuedAt=" + System.currentTimeMillis();
        String token = SignedTokenHelper.createSignedToken(payload, SECRET_KEY);

        ArkNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new S2CTokenIssuedPacket(token)
        );

        LOG.info("[ArkCraft] Registered new player '{}', token issued.", name);
    }
}
