package at.riemer.network;

import at.riemer.core.Main;
import at.riemer.feature.worldadmin.client.WorldAdminClientHandler;
import at.riemer.feature.worldadmin.network.C2SWorldAdminActionPacket;
import at.riemer.feature.worldadmin.network.S2CWorldListPacket;
import at.riemer.network.packet.C2SAuthHelloPacket;
import at.riemer.network.packet.S2CRegistrationErrorPacket;
import at.riemer.network.packet.S2CTokenIssuedPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;

public final class ArkNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static SimpleChannel CHANNEL;

    private ArkNetwork() {}

    // --------------------------------------------------
    // COMMON PACKETS (werden auf Client UND Server registriert)
    // --------------------------------------------------
    public static int registerCommonPackets() {

        System.out.println("[ArkNetwork] COMMON PACKET REGISTRATION");

        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(Main.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        int id = 0;

        // ---- existing packets ----

        CHANNEL.registerMessage(id++, C2SAuthHelloPacket.class,
                C2SAuthHelloPacket::encode,
                C2SAuthHelloPacket::decode,
                C2SAuthHelloPacket::handle);

        CHANNEL.registerMessage(id++, S2CTokenIssuedPacket.class,
                S2CTokenIssuedPacket::encode,
                S2CTokenIssuedPacket::decode,
                S2CTokenIssuedPacket::handle);

        CHANNEL.registerMessage(id++, S2CRegistrationErrorPacket.class,
                S2CRegistrationErrorPacket::encode,
                S2CRegistrationErrorPacket::decode,
                S2CRegistrationErrorPacket::handle);

        // Client → Server
        CHANNEL.registerMessage(id++, C2SWorldAdminActionPacket.class,
                C2SWorldAdminActionPacket::encode,
                C2SWorldAdminActionPacket::decode,
                C2SWorldAdminActionPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        // return next free id
        System.out.println("[ArkNetwork] COMMON PACKETS up to ID " + (id - 1));
        return id;
    }

    // --------------------------------------------------
    // CLIENT-ONLY PACKETS
    // --------------------------------------------------
    public static void registerClientPackets(int startId) {

        System.out.println("[ArkNetwork] CLIENT PACKET REGISTRATION starting at " + startId);

        int id = startId;

        CHANNEL.registerMessage(id++, S2CWorldListPacket.class,
                S2CWorldListPacket::encode,
                S2CWorldListPacket::decode,
                (msg, ctxSupplier) -> {
                    NetworkEvent.Context ctx = ctxSupplier.get();

                    // Auf den Main-Thread schedulen und NUR auf dem echten Client den GUI-Handler ausführen
                    ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                            Dist.CLIENT,
                            () -> () -> WorldAdminClientHandler.handleWorldList(msg, ctxSupplier)
                    ));

                    ctx.setPacketHandled(true);
                },
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        System.out.println("[ArkNetwork] CLIENT PACKETS up to ID " + (id - 1));
    }
}
