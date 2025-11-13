package at.riemer.network;

import at.riemer.core.Main;
import at.riemer.network.packet.C2SAuthHelloPacket;
import at.riemer.network.packet.S2CTokenIssuedPacket;
import at.riemer.network.packet.S2CRegistrationErrorPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class ArkNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static SimpleChannel CHANNEL;

    private ArkNetwork() {}

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(Main.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        int id = 0;

        CHANNEL.registerMessage(
                id++,
                C2SAuthHelloPacket.class,
                C2SAuthHelloPacket::encode,
                C2SAuthHelloPacket::decode,
                C2SAuthHelloPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                S2CTokenIssuedPacket.class,
                S2CTokenIssuedPacket::encode,
                S2CTokenIssuedPacket::decode,
                S2CTokenIssuedPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                S2CRegistrationErrorPacket.class,
                S2CRegistrationErrorPacket::encode,
                S2CRegistrationErrorPacket::decode,
                S2CRegistrationErrorPacket::handle
        );
    }
}
