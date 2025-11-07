package at.RIEMER.server;

import at.RIEMER.core.Main;

public class ServerBoot {
    public static Database db;

    public static void boot(){


        System.setProperty("nogui", "true");
        System.setProperty("forge.server.noGui", "true");
        System.setProperty("java.awt.headless", "true");
        Main.LOGGER.info("[ArkCraft] Headless server mode enabled.");

        db = new Database();
        db.runDB();
    }
}
