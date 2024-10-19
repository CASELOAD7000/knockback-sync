package me.caseload.knockbacksync.command.subcommand;

import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.command.generic.BuilderCommand;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.sender.Sender;
import me.caseload.knockbacksync.util.ChatUtil;
import org.incendo.cloud.CommandManager;

public class ReloadCommand implements BuilderCommand {
    public void register(CommandManager<Sender> manager) {
        manager.command(
            manager.commandBuilder("knockbacksync", "kbsync", "kbs")
                .literal("reload")
//                                .requires(source -> KnockbackSyncBase.INSTANCE.getPermissionChecker().hasPermission(source, "knockbacksync.reload", false))
                    .handler(context -> {
                        ConfigManager configManager = KnockbackSyncBase.INSTANCE.getConfigManager();
                        configManager.loadConfig(true);

                        String rawReloadMessage = configManager.getReloadMessage();
                        String reloadMessage = ChatUtil.translateAlternateColorCodes('&', rawReloadMessage);

                        context.sender().sendMessage(reloadMessage);
                    })
        );
    }
}
