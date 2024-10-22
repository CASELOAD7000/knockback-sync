package me.caseload.knockbacksync.command.subcommand;

import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.command.generic.BuilderCommand;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.sender.Sender;
import me.caseload.knockbacksync.util.ChatUtil;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.PredicatePermission;

import java.util.function.Predicate;

public class ReloadCommand implements BuilderCommand {
    public void register(CommandManager<Sender> manager) {
        manager.command(
            manager.commandBuilder("knockbacksync", "kbsync", "kbs")
                .literal("reload")
                .permission((sender -> {
                    final String permission = "knockbacksync.reload";
                    Predicate<Sender> senderPredicate = (s) -> {
                        return s.hasPermission(permission, false);
                    };

                    return PredicatePermission.of(senderPredicate).testPermission(sender);
                }))
                .handler(context -> {
                    ConfigManager configManager = Base.INSTANCE.getConfigManager();
                    configManager.loadConfig(true);

                    String rawReloadMessage = configManager.getReloadMessage();
                    String reloadMessage = ChatUtil.translateAlternateColorCodes('&', rawReloadMessage);

                    context.sender().sendMessage(reloadMessage);
                })
        );
    }
}
