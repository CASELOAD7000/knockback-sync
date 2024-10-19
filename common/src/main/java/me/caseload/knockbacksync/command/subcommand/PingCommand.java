package me.caseload.knockbacksync.command.subcommand;

import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.command.PlayerSelector;
import me.caseload.knockbacksync.sender.Sender;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PermissionResult;

public class PingCommand {
    public void register(CommandManager<Sender> manager) {
        manager.command(
            manager.commandBuilder("knockbacksync", "kbsync", "kbs")
                    .permission("knockbacksync.ping")
//                .permission((sender) -> PermissionResult.of(sender.hasPermission("knockbacksync.ping"), Permission.permission("knockbacksync.ping")))
                .literal("ping")
                .optional("target", KnockbackSyncBase.INSTANCE.getPlayerSelectorParser().descriptor())
                .handler(context -> {
                    context.sender().sendMessage("Their Ping is");
                    PlayerSelector selector = context.getOrDefault("target", null);
                    if (selector != null) {
                        context.sender().sendMessage("Their ping is: " + selector.getSinglePlayer().getPing());
                    }
                })
        );
    }
}
