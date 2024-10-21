package me.caseload.knockbacksync.command.subcommand;

import me.caseload.knockbacksync.ConfigWrapper;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.command.generic.BuilderCommand;
import me.caseload.knockbacksync.command.generic.PlayerSelector;
import me.caseload.knockbacksync.event.ConfigReloadEvent;
import me.caseload.knockbacksync.event.KBSyncEventHandler;
import me.caseload.knockbacksync.sender.Sender;
import me.caseload.knockbacksync.util.ChatUtil;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.PredicatePermission;

import java.util.function.Predicate;

public class PingCommand implements BuilderCommand {
    public void register(CommandManager<Sender> manager) {
        manager.command(
            manager.commandBuilder("knockbacksync", "kbsync", "kbs")
                .permission((sender -> {
                    final String permission = "knockbacksync.ping";
                    Predicate<Sender> senderPredicate = (s) -> {
                        return s.hasPermission(permission, true);
                    };

                    return PredicatePermission.of(senderPredicate).testPermission(sender);
                }))
                .literal("ping")
                .optional("target", KnockbackSyncBase.INSTANCE.getPlayerSelectorParser().descriptor())
                .handler(context -> {
                    PlayerSelector targetSelector = context.getOrDefault("target", null);

                    if (targetSelector == null) {
                        if (context.sender().isConsole()) {
                            context.sender().sendMessage("You must specify a player to use this command from the console.");
                        } else {
                            context.sender().sendMessage(ChatUtil.getPingMessage(context.sender().getUniqueId(), null));
                        }
                    } else {
                        context.sender().sendMessage(ChatUtil.getPingMessage(context.sender().getUniqueId(), targetSelector.getSinglePlayer().getUUID()));
                    }
                })
        );
    }


}
