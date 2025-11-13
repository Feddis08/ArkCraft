package at.riemer.feature.worldadmin.network;

import at.riemer.feature.worldadmin.server.WorldAdminManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SWorldAdminActionPacket {

    public enum ActionType {
        TELEPORT
        // später: CREATE_WORLD, UNLOAD_WORLD, DELETE_WORLD ...
    }

    private final ActionType action;
    private final String worldId;

    public C2SWorldAdminActionPacket(ActionType action, String worldId) {
        this.action = action;
        this.worldId = worldId;
    }

    // --- static encode/decode ---

    public static void encode(C2SWorldAdminActionPacket msg, PacketBuffer buf) {
        buf.writeEnumValue(msg.action);
        buf.writeString(msg.worldId);
    }

    public static C2SWorldAdminActionPacket decode(PacketBuffer buf) {
        ActionType action = buf.readEnumValue(ActionType.class);
        String worldId = buf.readString(32767);
        return new C2SWorldAdminActionPacket(action, worldId);
    }

    public static void handle(C2SWorldAdminActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player == null) {
            ctx.get().setPacketHandled(true);
            return;
        }

        ctx.get().enqueueWork(() -> {
            if (msg.action == ActionType.TELEPORT) {
                WorldAdminManager.teleportToWorld(player, msg.worldId);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}
