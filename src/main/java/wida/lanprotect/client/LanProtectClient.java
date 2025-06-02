package wida.lanprotect.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class LanProtectClient implements ClientModInitializer {

    public static final WhitelistManager playerManager = new WhitelistManager();

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                new LanProtectCommand().register(dispatcher));
    }
}
