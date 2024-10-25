package me.caseload.knockbacksync.command.subcommand;

import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.command.generic.BuilderCommand;
import me.caseload.knockbacksync.event.ConfigReloadEvent;
import me.caseload.knockbacksync.event.KBSyncEventHandler;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.sender.Sender;
import me.caseload.knockbacksync.util.ChatUtil;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.PredicatePermission;

import java.util.function.Predicate;

public class ReloadCommand implements BuilderCommand {

    private final ConfigManager configManager = Base.INSTANCE.getConfigManager();
    private String rawReloadMessage = configManager.getConfigWrapper().getString("reload_message", "&aSuccessfully reloaded KnockbackSync.");

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
                            configManager.loadConfig(true);

                            // Fire the ConfigReloadEvent
                            new ConfigReloadEvent(configManager).post();

                            String reloadMessage = ChatUtil.translateAlternateColorCodes('&', rawReloadMessage);

                            context.sender().sendMessage(reloadMessage);
                        })
        );
        Base.INSTANCE.getSimpleEventBus().registerListeners(this);
    }

    @KBSyncEventHandler
    public void onConfigReload(ConfigReloadEvent event) {
        rawReloadMessage = event.getConfigManager().getConfigWrapper().getString("reload_message", "&aSuccessfully reloaded KnockbackSync.");
    }
}