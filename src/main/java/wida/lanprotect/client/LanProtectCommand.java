package wida.lanprotect.client;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import static com.mojang.brigadier.Command.*;



public class LanProtectCommand {
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("lanprotect")
                .then(literal("whitelist").then(argument("name", string()).executes(ctx -> executes(getString(ctx, "name"), Type.WHITELIST))))
                .then(literal("ban").then(argument("name", string()).executes(ctx -> executes(getString(ctx, "name"), Type.BANNED))))
        );
    }

    private enum Type {
        BANNED, WHITELIST
    }

    private int executes(String name, Type type) {
        IntegratedServer server = MinecraftClient.getInstance().getServer();
        ClientPlayerEntity sender = MinecraftClient.getInstance().player;

        if (server == null) return SINGLE_SUCCESS;
        if (sender == null) return SINGLE_SUCCESS;
        if (!server.isRemote()) return SINGLE_SUCCESS;

        WhitelistManager manager = LanProtectClient.playerManager;
        boolean is = type == Type.BANNED ? manager.isBanned(name) : manager.isWhitelisted(name);
        if (is) {
            sender.sendMessage(Text.translatable("wida.lanprotect."+ type.name().toLowerCase() + ".already", name).styled(it -> it.withColor(Formatting.RED)), false);
            return SINGLE_SUCCESS;
        }

        if (type == Type.WHITELIST) manager.whitelist(name);
        else {
            manager.ban(name);
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(name);
            if (player != null)
                player.networkHandler.disconnect(Text.translatable("wida.lanprotect.banned"));
        }

        sender.sendMessage(Text.translatable("wida.lanprotect."+ type.name().toLowerCase() + ".success", name).styled(it -> it.withColor(Formatting.GREEN)), false);

        return SINGLE_SUCCESS;
    }
}
