package at.riemer.client.login.network;

import at.riemer.client.login.ClientTokenStorage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CRegistrationErrorPacket {

    private final String message;

    public S2CRegistrationErrorPacket(String message) {
        this.message = message;
    }

    public static void encode(S2CRegistrationErrorPacket msg, PacketBuffer buf) {
        buf.writeString(msg.message, 32767);
    }

    public static S2CRegistrationErrorPacket decode(PacketBuffer buf) {
        return new S2CRegistrationErrorPacket(buf.readString(32767));
    }

    public static void handle(S2CRegistrationErrorPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> handleClient(msg));
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(S2CRegistrationErrorPacket msg) {
        ClientTokenStorage.removeToken();
    }
}
