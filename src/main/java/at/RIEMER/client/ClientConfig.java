package at.RIEMER.client;

public class ClientConfig {
    public static String ServerAddress = "localhost:25565";
    public static String PlayerName = "FelixDEV";
    public static boolean gotToken = false;


    public static void setArgs(){
        System.setProperty("allowInsecureLocalConnections", "true");
        System.setProperty("fml.doNotCheckOnline", "true");
        System.setProperty("user.name", PlayerName);
    }
}
