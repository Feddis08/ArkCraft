package at.RIEMER.client.guis;

import at.RIEMER.client.ClientConfig;
import at.RIEMER.client.login.ClientSessionOverrideHelper;
import at.RIEMER.client.login.ClientTokenStorage;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class ArkPortalScreen extends Screen {

    private TextFieldWidget nameField;
    private Button connectButton;
    private Button removeTokenButton;

    // Merkt sich den letzten Token-Status, um bei Änderungen die UI neu aufzubauen
    private boolean lastHadToken;

    public ArkPortalScreen() {
        super(new StringTextComponent("ArkCraft Portal"));
    }

    @Override
    protected void init() {
        super.init();
        // Initialer Status
        this.lastHadToken = ClientTokenStorage.hasToken();
        rebuildUi();
    }

    /**
     * Baut alle Widgets passend zum aktuellen Token-Status neu auf.
     */
    private void rebuildUi() {
        // Alte Widgets entfernen
        this.buttons.clear();
        this.children.clear();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        boolean hasToken = ClientTokenStorage.hasToken();

        nameField = null;
        removeTokenButton = null;

        if (!hasToken) {
            // Kein Token -> Name-Feld anzeigen
            nameField = new TextFieldWidget(
                    this.font,
                    centerX - 75,
                    centerY,
                    150,
                    20,
                    new StringTextComponent("Name")
            );
            this.children.add(nameField);
        } else {
            // Token vorhanden -> Button zum Entfernen anzeigen
            removeTokenButton = new Button(
                    centerX - 50,
                    centerY - 10,
                    100,
                    20,
                    new StringTextComponent("Token entfernen"),
                    (b) -> {
                        ClientTokenStorage.removeToken();
                        // Token ist jetzt weg -> Status anpassen und UI neu bauen
                        this.lastHadToken = false;
                        rebuildUi();
                    }
            );
            this.addButton(removeTokenButton);
        }

        // Connect-Button ist immer da
        connectButton = new Button(
                centerX - 50,
                centerY + 30,
                100,
                20,
                new StringTextComponent("Connect"),
                (b) -> onConnectPressed()
        );
        this.addButton(connectButton);
    }

    private void onConnectPressed() {
        Minecraft mc = this.minecraft;
        if (mc == null) return;

        if (!ClientTokenStorage.hasToken()) {
            String desired = nameField != null ? nameField.getText().trim() : "";
            if (desired.isEmpty()) {
                // optional: Fehlermeldung anzeigen
                return;
            }
            ClientSessionOverrideHelper.applyTemporarySessionName(desired);
        } else {
            // hier aus dem Token den Namen holen und Session setzen
            ClientSessionOverrideHelper.applyNameFromTokenIfPresent();
        }

        // Danach wie gehabt: Server-Adresse aus Config und ConnectingScreen starten
        String addr = ClientConfig.ServerAddress; // z.B. "localhost:25565"
        String host = addr;
        int port = 25565;
        int idx = addr.lastIndexOf(':');
        if (idx > 0 && idx < addr.length() - 1) {
            host = addr.substring(0, idx);
            try {
                port = Integer.parseInt(addr.substring(idx + 1));
            } catch (NumberFormatException ignored) {}
        }

        mc.displayGuiScreen(
                new net.minecraft.client.gui.screen.ConnectingScreen(
                        new net.minecraft.client.gui.screen.MainMenuScreen(),
                        mc,
                        host,
                        port
                )
        );
    }

    @Override
    public void tick() {
        super.tick();

        // Wenn sich der Token-Status ändert (z.B. neuer Token auftaucht),
        // wird die UI automatisch neu aufgebaut.
        boolean hasTokenNow = ClientTokenStorage.hasToken();
        if (hasTokenNow != this.lastHadToken) {
            this.lastHadToken = hasTokenNow;
            rebuildUi();
        }

        if (nameField != null) {
            nameField.tick();
        }
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);

        drawCenteredString(ms, this.font, "Willkommen bei ArkCraft!", this.width / 2, 40, 0xFFFFFF);

        if (!ClientTokenStorage.hasToken()) {
            drawCenteredString(ms, this.font, "Create your account:", this.width / 2, 60, 0xFFCCCC);
        } else {
            drawCenteredString(ms, this.font, "Account token found - you will auto-authenticate.", this.width / 2, 60, 0xCCFFCC);
        }

        super.render(ms, mouseX, mouseY, partialTicks);

        // Textfeld manuell rendern (da nur in children, nicht in buttons)
        if (nameField != null) {
            nameField.render(ms, mouseX, mouseY, partialTicks);
        }
    }
}
