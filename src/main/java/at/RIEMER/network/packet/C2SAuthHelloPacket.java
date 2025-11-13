package at.RIEMER.network.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.sql.SQLException;
import java.util.function.Supplier;

public class C2SAuthHelloPacket {

    private final String tokenOrEmpty;

    public C2SAuthHelloPacket(String tokenOrEmpty) {
        this.tokenOrEmpty = tokenOrEmpty != null ? tokenOrEmpty : "";
    }

    public static void encode(C2SAuthHelloPacket msg, PacketBuffer buf) {
        buf.writeString(msg.tokenOrEmpty, 32767);
    }

    public static C2SAuthHelloPacket decode(PacketBuffer buf) {
        return new C2SAuthHelloPacket(buf.readString(32767));
    }

    public static void handle(C2SAuthHelloPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();
            if (player == null) return;

            String token = msg.tokenOrEmpty.trim();
            // Hier delegieren wir an unsere Server-Logik:
            try {
                at.RIEMER.server.ArkServerAccounts.handleAuthHello(player, token);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        ctx.setPacketHandled(true);
    }
}
