package wida.lanprotect.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wida.lanprotect.client.LanProtectClient;
import wida.lanprotect.client.WhitelistManager;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {
    @Inject(method = "onHello", at = @At(value = "HEAD"))
    public void onHello(LoginHelloC2SPacket packet, CallbackInfo ci) {
        ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
        IntegratedServer server = MinecraftClient.getInstance().getServer();
        ServerLoginNetworkHandler handler = (ServerLoginNetworkHandler)(Object)this;

        if (clientPlayer == null) return;
        if (server == null) return;
        if (!server.isRemote()) return;

        WhitelistManager manager = LanProtectClient.playerManager;
        String name = packet.name();

        if (manager.shouldAsk(name)) {
            clientPlayer.sendMessage(
                    Text.literal(name + " is requesting to join ")
                            .append(Text.literal("[v]")
                                    .styled(it -> it.withClickEvent(new ClickEvent.RunCommand("/lanprotect whitelist " + name)))
                            )
                            .append(" ")
                            .append(Text.literal("[x]")
                                    .styled(it -> it.withClickEvent(new ClickEvent.RunCommand("/lanprotect ban " + name)))
                            ),
                    false);
            handler.disconnect(Text.literal("Awaiting host's confirmation"));
            return;
        }

        if (manager.isBanned(name)) {
            handler.disconnect(Text.literal("You're not allowed to join this game."));
            return;
        }
    }
}
