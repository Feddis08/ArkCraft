package at.RIEMER.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.ServerPinger;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ArkPortalScreen extends Screen {

    private boolean serverOnline = false;
    private boolean pingDone = false;

    private TextFieldWidget nameField;
    private Button connectButton;

    public ArkPortalScreen() {
        super(new StringTextComponent("ArkCraft Portal"));
    }

    @Override
    protected void init() {
        super.init();

        // Server-Ping starten
        pingServerAsync();

        // FALL 1: kein Token → Name-Feld anzeigen
        if (!ClientConfig.gotToken) {
            nameField = new TextFieldWidget(
                    this.font,
                    this.width / 2 - 75,
                    this.height / 2 + 20,
                    150,
                    20,
                    new StringTextComponent("Enter Name")
            );
            this.children.add(nameField);

            connectButton = new Button(
                    this.width / 2 - 50,
                    this.height / 2 + 45,
                    100,
                    20,
                    new StringTextComponent("Connect"),
                    (b) -> onConnectPressed()
            );
            this.addButton(connectButton);
        }
    }

    private void pingServerAsync() {
        new Thread(() -> {
            while(true){
                pingDone = false;
                try {
                    ServerPinger pinger = new ServerPinger();
                    ServerData data = new ServerData("ArkCraft", ClientConfig.ServerAddress, false);
                    pinger.ping(data, () -> {});
                    serverOnline = true;
                } catch (Exception ex) {
                    serverOnline = false;
                }
                pingDone = true;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void onConnectPressed() {
        String name = (nameField != null) ? nameField.getText().trim() : "Unknown";
        // Hier später Token/Account Logik rein
        System.out.println("Connecting as: " + name);
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);

        int y = 40;

        // Titel
        drawCenteredString(ms, this.font, "Willkommen bei Arkcraft", this.width / 2, y, 0xFFFFFF);

        y += 25;

        if (!pingDone) {
            drawCenteredString(ms, this.font, "Checking server status ...", this.width / 2, y, 0x888888);
        } else if (serverOnline) {
            drawCenteredString(ms, this.font, "Game Server is available!", this.width / 2, y, 0x00FF00);
        } else {
            drawCenteredString(ms, this.font, "Server offline", this.width / 2, y, 0xFF0000);
        }

        y += 25;

        if (!ClientConfig.gotToken) {
            drawCenteredString(ms, this.font, "You don't have an account - create one!", this.width / 2, y, 0xFF4444);
        } else {
            drawCenteredString(ms, this.font, "Account OK – Token present", this.width / 2, y, 0x00AAFF);
        }

        super.render(ms, mouseX, mouseY, partialTicks);

        // Texteingabefeld rendern
        if (nameField != null) {
            nameField.render(ms, mouseX, mouseY, partialTicks);
        }
    }
}
