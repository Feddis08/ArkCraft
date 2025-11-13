package at.riemer.feature.worldadmin.client;

import at.riemer.feature.worldadmin.network.S2CWorldListPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public final class WorldAdminClientHandler {

    private WorldAdminClientHandler() {}

    public static void handleWorldList(S2CWorldListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            mc.displayGuiScreen(new ArkWorldAdminScreen(msg.getEntries()));
        });
        context.setPacketHandled(true);
    }
}
