package at.riemer.feature.worldadmin.client;

import at.riemer.core.ArkNetwork;
import at.riemer.feature.worldadmin.network.C2SWorldAdminActionPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class ArkCreateWorldScreen extends Screen {

    private final Screen parent;

    private TextFieldWidget nameField;
    private TextFieldWidget typeField;
    private TextFieldWidget seedField;

    private String errorMessage;

    public ArkCreateWorldScreen(Screen parent) {
        super(new StringTextComponent("Create World"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.buttons.clear();
        this.children.clear();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Name-Feld (ohne Label im Feld)
        this.nameField = new TextFieldWidget(
                this.font,
                centerX - 100,
                centerY - 40,
                200,
                20,
                StringTextComponent.EMPTY
        );
        this.nameField.setMaxStringLength(128);
        this.nameField.setText("");
        this.children.add(this.nameField);

        // World-Type-Feld
        this.typeField = new TextFieldWidget(
                this.font,
                centerX - 100,
                centerY - 10,
                200,
                20,
                StringTextComponent.EMPTY
        );
        this.typeField.setMaxStringLength(32);
        this.typeField.setText("");
        this.children.add(this.typeField);

        // Seed-Feld
        this.seedField = new TextFieldWidget(
                this.font,
                centerX - 100,
                centerY + 20,
                200,
                20,
                StringTextComponent.EMPTY
        );
        this.seedField.setMaxStringLength(64);
        this.seedField.setText("");
        this.children.add(this.seedField);

        // Generate-Button
        this.addButton(new Button(
                centerX - 100,
                centerY + 60,
                95,
                20,
                new StringTextComponent("Generate"),
                (b) -> onGenerateClicked()
        ));

        // Cancel → zurück zum WorldManager
        this.addButton(new Button(
                centerX + 5,
                centerY + 60,
                95,
                20,
                new StringTextComponent("Cancel"),
                (b) -> this.minecraft.displayGuiScreen(parent)
        ));
    }

    private void onGenerateClicked() {
        String name = nameField.getText().trim();
        String type = typeField.getText().trim();
        String seedText = seedField.getText().trim();

        if (name.isEmpty()) {
            errorMessage = "World name darf nicht leer sein!";
            return;
        }

        if (type.isEmpty()) {
            type = "NORMAL"; // Default-Welt-Typ
        }

        Long seed = null;
        if (!seedText.isEmpty()) {
            try {
                seed = Long.parseLong(seedText);
            } catch (NumberFormatException ex) {
                // Text → Hash als Seed
                seed = (long) seedText.hashCode();
            }
        }

        // Packet an Server schicken: CREATE_WORLD + Name/Type/Seed
        ArkNetwork.CHANNEL.sendToServer(
                new C2SWorldAdminActionPacket(name, type, seed)
        );

        // zurück zum World-Manager
        this.minecraft.displayGuiScreen(parent);
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        drawCenteredString(ms, this.font, "Create / Load World", this.width / 2, 40, 0xFFFFFF);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Labels über den Feldern
        this.font.drawString(ms, "Name:", centerX - 110, centerY - 35, 0xA0A0A0);
        this.font.drawString(ms, "Type:", centerX - 110, centerY - 5, 0xA0A0A0);
        this.font.drawString(ms, "Seed:", centerX - 110, centerY + 25, 0xA0A0A0);

        super.render(ms, mouseX, mouseY, partialTicks);

        this.nameField.render(ms, mouseX, mouseY, partialTicks);
        this.typeField.render(ms, mouseX, mouseY, partialTicks);
        this.seedField.render(ms, mouseX, mouseY, partialTicks);

        if (errorMessage != null) {
            drawCenteredString(ms, this.font, errorMessage, this.width / 2, centerY + 90, 0xFF5555);
        }
    }
}
