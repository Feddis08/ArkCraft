package at.riemer.feature.worldadmin.server;

import at.riemer.core.Main;
import at.riemer.feature.worldadmin.network.S2CWorldListPacket;
import at.riemer.core.ArkNetwork;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.List;

@Mod.EventBusSubscriber(modid = Main.MOD_ID)
public final class WorldCommandRegistration {

    private WorldCommandRegistration() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSource> node = Commands.literal("world")
                .requires(source -> source.hasPermissionLevel(2))  // OP-Check
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().asPlayer();
                    MinecraftServer server = player.getServer();

// DEBUG

                    List<WorldAdminManager.WorldInfo> worlds = WorldAdminManager.getAvailableWorlds(server);
                    System.out.println("WorldCommand: Found worlds = " + worlds.size());
                    for (WorldAdminManager.WorldInfo w : worlds) {
                        System.out.println("WorldCommand: world id=" + w.getId() + " name=" + w.getName());
                    }
                    ArkNetwork.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new S2CWorldListPacket(worlds)
                    );

                    return 1;
                });

        event.getDispatcher().register(node);
    }
}
