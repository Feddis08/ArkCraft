package at.riemer.feature.worldadmin.network;

import at.riemer.feature.worldadmin.server.WorldAdminManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SWorldAdminActionPacket {

    public enum ActionType {
        TELEPORT,
        CREATE_WORLD
        // später: UNLOAD_WORLD, DELETE_WORLD ...
    }

    private final ActionType action;

    // für TELEPORT
    private final String worldId;

    // für CREATE_WORLD
    private final String worldName;
    private final String worldType;
    private final Long seed;

    // --- Konstruktoren ---

    // Teleport
    public C2SWorldAdminActionPacket(ActionType action, String worldId) {
        this(action, worldId, null, null, null);
    }

    // Create-World
    public C2SWorldAdminActionPacket(String worldName, String worldType, Long seed) {
        this(ActionType.CREATE_WORLD, null, worldName, worldType, seed);
    }

    private C2SWorldAdminActionPacket(ActionType action,
                                      String worldId,
                                      String worldName,
                                      String worldType,
                                      Long seed) {
        this.action = action;
        this.worldId = worldId;
        this.worldName = worldName;
        this.worldType = worldType;
        this.seed = seed;
    }

    public ActionType getAction()   { return action; }
    public String getWorldId()      { return worldId; }
    public String getWorldName()    { return worldName; }
    public String getWorldType()    { return worldType; }
    public Long getSeed()           { return seed; }

    // --- static encode/decode ---

    public static void encode(C2SWorldAdminActionPacket msg, PacketBuffer buf) {
        buf.writeEnumValue(msg.action);

        switch (msg.action) {
            case TELEPORT:
                buf.writeString(msg.worldId);
                break;

            case CREATE_WORLD:
                buf.writeString(msg.worldName != null ? msg.worldName : "");
                buf.writeString(msg.worldType != null ? msg.worldType : "");
                buf.writeBoolean(msg.seed != null);
                if (msg.seed != null) {
                    buf.writeLong(msg.seed);
                }
                break;
        }
    }

    public static C2SWorldAdminActionPacket decode(PacketBuffer buf) {
        ActionType action = buf.readEnumValue(ActionType.class);

        switch (action) {
            case TELEPORT: {
                String worldId = buf.readString(32767);
                return new C2SWorldAdminActionPacket(action, worldId);
            }

            case CREATE_WORLD: {
                String worldName = buf.readString(32767);
                String worldType = buf.readString(32767);
                boolean hasSeed = buf.readBoolean();
                Long seed = hasSeed ? buf.readLong() : null;

                if (worldName.isEmpty()) {
                    worldName = null;
                }
                if (worldType.isEmpty()) {
                    worldType = null;
                }

                return new C2SWorldAdminActionPacket(worldName, worldType, seed);
            }

            default:
                // Fallback – sollte nie passieren
                return new C2SWorldAdminActionPacket(ActionType.TELEPORT, "minecraft:overworld");
        }
    }

    public static void handle(C2SWorldAdminActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ServerPlayerEntity player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            if (msg.action == ActionType.TELEPORT) {
                WorldAdminManager.teleportToWorld(player, msg.worldId);
            }
            else if (msg.action == ActionType.CREATE_WORLD) {
                WorldAdminManager.createOrLoadWorld(
                        player,
                        msg.worldName,
                        msg.worldType,
                        msg.seed
                );
            }
        });

        context.setPacketHandled(true);
    }
}
