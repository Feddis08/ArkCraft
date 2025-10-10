package at.RIEMER.server;

public class ServerBoot {
    public static Database db;

    public static void boot(){
        db = new Database();
        db.runDB();
    }
}
