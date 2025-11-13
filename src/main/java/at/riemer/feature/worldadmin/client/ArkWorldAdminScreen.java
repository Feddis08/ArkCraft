package at.riemer.feature.worldadmin.client;

import at.riemer.feature.worldadmin.network.C2SWorldAdminActionPacket;
import at.riemer.feature.worldadmin.network.S2CWorldListPacket;
import at.riemer.network.ArkNetwork;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class ArkWorldAdminScreen extends Screen {

    private final List<S2CWorldListPacket.WorldEntry> worlds;
    private final List<Button> worldButtons = new ArrayList<>();

    public ArkWorldAdminScreen(List<S2CWorldListPacket.WorldEntry> worlds) {
        super(new StringTextComponent("ArkCraft World Manager"));
        this.worlds = worlds;
    }

    @Override
    protected void init() {
        super.init();
        this.buttons.clear();
        this.children.clear();
        this.worldButtons.clear();

        int centerX = this.width / 2;
        int startY = 60;
        int index = 0;

        for (S2CWorldListPacket.WorldEntry entry : worlds) {
            int y = startY + index * 24;

            Button btn = new Button(
                    centerX - 80,
                    y,
                    160,
                    20,
                    new StringTextComponent(entry.name + " (" + entry.id + ")"),
                    (b) -> onTeleportClicked(entry)
            );
            this.addButton(btn);
            this.worldButtons.add(btn);

            index++;
        }

        // Close-Button
        this.addButton(new Button(
                centerX - 40,
                this.height - 30,
                80,
                20,
                new StringTextComponent("Close"),
                (b) -> this.minecraft.displayGuiScreen(null)
        ));
    }

    private void onTeleportClicked(S2CWorldListPacket.WorldEntry entry) {
        ArkNetwork.CHANNEL.sendToServer(
                new C2SWorldAdminActionPacket(
                        C2SWorldAdminActionPacket.ActionType.TELEPORT,
                        entry.id
                )
        );
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        drawCenteredString(ms, this.font, "ArkCraft World Manager", this.width / 2, 20, 0xFFFFFF);
        drawCenteredString(ms, this.font, "Select a world to teleport to:", this.width / 2, 40, 0xAAAAAA);

        super.render(ms, mouseX, mouseY, partialTicks);
    }
}
