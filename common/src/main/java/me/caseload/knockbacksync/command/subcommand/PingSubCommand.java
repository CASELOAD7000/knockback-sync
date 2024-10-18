package me.caseload.knockbacksync.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlayerData;
import me.caseload.knockbacksync.util.CommandUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.Callable;

public class PingSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("ping")
                .requires(source -> KnockbackSyncBase.INSTANCE.getPermissionChecker().hasPermission(source, "knockbacksync.ping", true))
                .executes(context -> { // Added .executes() here to handle no target
                    if (context.getSource().getEntity() instanceof ServerPlayer sender) {
                        CommandUtil.sendSuccessMessage(context, getComponent(sender));
                    } else {
                        CommandUtil.sendFailureMessage(context,"This command can only be used by players.");
                    }
                    return 1;
                })
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> {
                            EntitySelector selector = context.getArgument("target", EntitySelector.class);
                            ServerPlayer target = selector.findSinglePlayer(context.getSource());
                            CommandUtil.sendSuccessMessage(context, getComponent(target));
                            return 1;
                        })
                );
    }

    public static String getComponent(ServerPlayer player) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player.getUUID());
        if (playerData.getPing() == null) {
            return "Pong not received. Your estimated ping is " + playerData.getPlatformPlayer().getPing() + "ms.";
        } else {
            return "Your last ping packet took " + playerData.getPing() + "ms. Jitter: " + playerData.getJitter() + "ms.";
        }
    }
}
