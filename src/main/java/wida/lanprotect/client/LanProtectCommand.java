package wida.lanprotect.client;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import static com.mojang.brigadier.Command.*;



public class LanProtectCommand {
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("lanprotect")
                .then(literal("whitelist").then(argument("name", string()).executes(ctx -> executes(getString(ctx, "name"), Type.WHITELIST))))
                .then(literal("ban").then(argument("name", string()).executes(ctx -> executes(getString(ctx, "name"), Type.BAN))))
        );
    }

    private enum Type {
        BAN, WHITELIST
    }

    private int executes(String name, Type type) {
        IntegratedServer server = MinecraftClient.getInstance().getServer();
        ClientPlayerEntity sender = MinecraftClient.getInstance().player;

        if (server == null) return SINGLE_SUCCESS;
        if (sender == null) return SINGLE_SUCCESS;
        if (!server.isRemote()) return SINGLE_SUCCESS;

        WhitelistManager manager = LanProtectClient.playerManager;
        boolean is = type == Type.BAN ? manager.isBanned(name) : manager.isWhitelisted(name);
        if (is) {
            sender.sendMessage(Text.literal("Already " + type.name() + "ED,"), false);
            return SINGLE_SUCCESS;
        }

        if (type == Type.WHITELIST) manager.whitelist(name);
        else {
            manager.ban(name);
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(name);
            if (player != null)
                player.networkHandler.disconnect(Text.literal("You're not allowed. bozo"));
        }

        sender.sendMessage(Text.literal(type.name().toLowerCase() + " " + name + "."), false);

        return SINGLE_SUCCESS;
    }
}
