package me.caseload.knockbacksync.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.command.FabricSenderFactory;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlayerData;
import me.caseload.knockbacksync.util.ChatUtil;
import me.caseload.knockbacksync.util.CommandUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

public class PingSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("ping")
                .requires(source -> KnockbackSyncBase.INSTANCE.getPermissionChecker().hasPermission(
                        KnockbackSyncBase.INSTANCE.getSenderFactory(),
                        "knockbacksync.ping", true))
                .executes(context -> { // Added .executes() here to handle no target
                    if (context.getSource().getEntity() instanceof ServerPlayer sender) {
                        CommandUtil.sendSuccessMessage(context, getPingMessage(sender));
                    } else {
                        CommandUtil.sendFailureMessage(context,"This command can only be used by players.");
                    }
                    return 1;
                })
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> {
                            EntitySelector selector = context.getArgument("target", EntitySelector.class);
                            ServerPlayer target = selector.findSinglePlayer(context.getSource());
                            CommandUtil.sendSuccessMessage(context, getPingMessage(target));
                            return 1;
                        })
                );
    }

    public static String getPingMessage(ServerPlayer player) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player.getUUID());
        if (playerData.getPing() == null) {
            return "Pong not received. Your estimated ping is " + ChatUtil.translateAlternateColorCodes('&', "&b" + playerData.getPlatformPlayer().getPing() + "&rms.");
        } else {
            return "Your last ping packet took &b" + ChatUtil.translateAlternateColorCodes('&', String.format("%.3f", playerData.getPing()) + "&rms. Jitter: &b" + String.format("%.3f", playerData.getJitter()) + "&rms.");
        }
    }
}
