package at.riemer.client.login.network;

import at.riemer.client.login.ClientTokenStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CTokenIssuedPacket {

    private final String token;

    public S2CTokenIssuedPacket(String token) {
        this.token = token;
    }

    public static void encode(S2CTokenIssuedPacket msg, PacketBuffer buf) {
        buf.writeString(msg.token, 32767);
    }

    public static S2CTokenIssuedPacket decode(PacketBuffer buf) {
        return new S2CTokenIssuedPacket(buf.readString(32767));
    }

    public static void handle(S2CTokenIssuedPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> handleClient(msg));
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(S2CTokenIssuedPacket msg) {
        ClientTokenStorage.saveToken(msg.token);
        // Optional: Client-Feedback
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendChatMessage("[ArkCraft] Account registered. Token stored.");
        }
    }
}
