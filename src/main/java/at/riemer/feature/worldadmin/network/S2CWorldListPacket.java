package at.riemer.feature.worldadmin.network;

import at.riemer.feature.worldadmin.server.WorldAdminManager;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class S2CWorldListPacket {

    public static class WorldEntry {
        public final String id;
        public final String name;

        public WorldEntry(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private final List<WorldEntry> entries;

    public S2CWorldListPacket(List<WorldAdminManager.WorldInfo> infos) {
        this.entries = infos.stream()
                .map(i -> new WorldEntry(i.getId(), i.getName()))
                .collect(Collectors.toList());
    }

    public S2CWorldListPacket(List<WorldEntry> entries, boolean direct) {
        // interner ctor für decode
        this.entries = entries;
    }

    public List<WorldEntry> getEntries() {
        return entries;
    }

    // --- Forge-Style: static encode/decode ---
    public static void encode(S2CWorldListPacket msg, PacketBuffer buf) {
        try {
            System.out.println("S2CWorldListPacket.encode: START");
            buf.writeVarInt(msg.getEntries().size());
            System.out.println("S2CWorldListPacket.encode: size = " + msg.getEntries().size());

            int i = 0;
            for (WorldEntry e : msg.getEntries()) {
                System.out.println("S2CWorldListPacket.encode: entry[" + i + "] id=" + e.id + " name=" + e.name);
                buf.writeString(e.id);
                buf.writeString(e.name);
                i++;
            }

            System.out.println("S2CWorldListPacket.encode: SUCCESS");
        }
        catch (Exception ex) {
            System.out.println("S2CWorldListPacket.encode: ERROR!!!");
            ex.printStackTrace();
            throw ex;
        }
    }

    public static S2CWorldListPacket decode(PacketBuffer buf) {
        try {
            System.out.println("S2CWorldListPacket.decode: START");

            int size = buf.readVarInt();
            System.out.println("S2CWorldListPacket.decode: list size = " + size);

            List<WorldEntry> list = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                String id = buf.readString(32767);
                String name = buf.readString(32767);
                System.out.println("S2CWorldListPacket.decode: entry[" + i + "] id=" + id + " name=" + name);
                list.add(new WorldEntry(id, name));
            }

            System.out.println("S2CWorldListPacket.decode: SUCCESS");
            return new S2CWorldListPacket(list, true);
        }
        catch (Exception ex) {
            System.out.println("S2CWorldListPacket.decode: ERROR!!!");
            ex.printStackTrace();
            // return empty list so the handler at least doesn't explode
            return new S2CWorldListPacket(new ArrayList<>(), true);
        }
    }
}
