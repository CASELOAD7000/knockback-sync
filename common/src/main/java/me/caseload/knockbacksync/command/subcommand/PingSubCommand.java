package me.caseload.knockbacksync.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlayerData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PingSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("ping")
                .requires(source -> KnockbackSyncBase.INSTANCE.getPermissionChecker().hasPermission(source, "knockbacksync.ping", true))
                .executes(context -> { // Added .executes() here to handle no target
                    if (context.getSource().getEntity() instanceof ServerPlayer) {
                        ServerPlayer sender = (ServerPlayer) context.getSource().getEntity();
                        PlayerData playerData = PlayerDataManager.getPlayerData(sender.getUUID());
                        context.getSource().sendSuccess(() -> {
                            if (playerData.getPing() == null) {
                                return Component.literal("Pong not received. Your estimated ping is " + playerData.getPlatformPlayer().getPing() + "ms.");
                            } else {
                                return Component.literal("Your last ping packet took " + playerData.getPing() +  "ms. Jitter: " + playerData.getJitter() + "ms.");
                            }
                        }, false);
                    } else {
                        context.getSource().sendFailure(Component.literal("This command can only be used by players."));
                    }
                    return 1;
                })
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> {
                            EntitySelector selector = context.getArgument("target", EntitySelector.class);
                            ServerPlayer target = selector.findSinglePlayer(context.getSource());
                            PlayerData playerData = PlayerDataManager.getPlayerData(target.getUUID());
                            context.getSource().sendSuccess(() -> {
                                if (playerData.getPing() == null) {
                                    return Component.literal("Pong not received. " + target.getDisplayName().getString() + "’s estimated ping is " + playerData.getEstimatedPing() + "ms.");
                                } else {
                                    return Component.literal(target.getDisplayName().getString() + "’s last ping packet took " + playerData.getPing() + "ms. Jitter: " + playerData.getJitter() + "ms.");
                                }
                            }, false);
                            return 1;
                        })
                );
    }
}
