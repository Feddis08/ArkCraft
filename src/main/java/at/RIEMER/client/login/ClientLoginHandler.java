package at.RIEMER.client.login;

import at.RIEMER.network.ArkNetwork;
import at.RIEMER.network.packet.C2SAuthHelloPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientLoginHandler {

    @SubscribeEvent
    public static void onClientLoggedIn(ClientPlayerNetworkEvent.LoggedInEvent e) {
        String token = ClientTokenStorage.getTokenOrNull();
        if (token == null) token = "";

        ArkNetwork.CHANNEL.sendToServer(new C2SAuthHelloPacket(token));
    }

    private ClientLoginHandler() {}
}
