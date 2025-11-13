package at.riemer.feature.worldadmin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class ArkCreateWorldScreen extends Screen {

    private TextFieldWidget nameField;
    private TextFieldWidget seedField;

    public ArkCreateWorldScreen() {
        super(new StringTextComponent("Create World"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.nameField = new TextFieldWidget(
                this.font, centerX - 100, centerY - 30, 200, 20,
                new StringTextComponent("World Name")
        );
        this.children.add(this.nameField);

        this.seedField = new TextFieldWidget(
                this.font, centerX - 100, centerY, 200, 20,
                new StringTextComponent("Seed (optional)")
        );
        this.children.add(this.seedField);

        this.addButton(new Button(
                centerX - 100, centerY + 40, 95, 20,
                new StringTextComponent("Create"),
                (b) -> onCreateClicked()
        ));

        this.addButton(new Button(
                centerX + 5, centerY + 40, 95, 20,
                new StringTextComponent("Cancel"),
                (b) -> this.minecraft.displayGuiScreen(null)
        ));
    }

    private void onCreateClicked() {
        String name = nameField.getText().trim();
        String seed = seedField.getText().trim();

        if (name.isEmpty()) {
            // TODO: Fehlermeldung anzeigen
            return;
        }

        // TODO: Packet an Server schicken (C2SWorldAdminActionPacket mit CREATE_WORLD + Parametern)

        this.minecraft.displayGuiScreen(null);
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        drawCenteredString(ms, this.font, "Create New World", this.width / 2, 40, 0xFFFFFF);
        super.render(ms, mouseX, mouseY, partialTicks);

        this.nameField.render(ms, mouseX, mouseY, partialTicks);
        this.seedField.render(ms, mouseX, mouseY, partialTicks);
    }
}
