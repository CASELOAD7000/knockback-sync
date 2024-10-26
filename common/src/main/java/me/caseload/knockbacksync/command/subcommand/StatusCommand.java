package me.caseload.knockbacksync.command.subcommand;

import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.ConfigWrapper;
import me.caseload.knockbacksync.command.generic.BuilderCommand;
import me.caseload.knockbacksync.command.generic.PlayerSelector;
import me.caseload.knockbacksync.event.events.ConfigReloadEvent;
import me.caseload.knockbacksync.event.KBSyncEventHandler;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.caseload.knockbacksync.sender.Sender;
import me.caseload.knockbacksync.util.ChatUtil;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.PredicatePermission;

import java.util.UUID;
import java.util.function.Predicate;

public class StatusCommand implements BuilderCommand {

    private static final ConfigManager configManager = Base.INSTANCE.getConfigManager();
    private static final String STATUS_SELF_PERMISSION = "knockbacksync.status.self";
    private static final String STATUS_OTHER_PERMISSION = "knockbacksync.status.other";

    private String globalStatusMessage;
    private String playerStatusMessage;
    private String playerDisabledGlobalMessage;

    public StatusCommand() {
        updateConfigValues();
    }

    public void register(CommandManager<Sender> manager) {
        manager.command(
                manager.commandBuilder("knockbacksync", "kbsync", "kbs")
                        .literal("status")
                        .optional("target", Base.INSTANCE.getPlayerSelectorParser().descriptor())
                        .permission((sender -> {
                            Predicate<Sender> senderPredicate = (s) -> {
                                return s.hasPermission(STATUS_SELF_PERMISSION, true) || sender.hasPermission(STATUS_OTHER_PERMISSION, false);
                            };

                            return PredicatePermission.of(senderPredicate).testPermission(sender);
                        }))
                        .handler(context -> {
                            Sender sender = context.sender();
                            PlayerSelector targetSelector = context.getOrDefault("target", null);
                            if (targetSelector == null) {
                                // Show global status
                                boolean globalStatus = configManager.isToggled();
                                sender.sendMessage(ChatUtil.translateAlternateColorCodes('&', globalStatusMessage) +
                                        (globalStatus
                                                ? ChatUtil.translateAlternateColorCodes('&', "&aEnabled")
                                                : ChatUtil.translateAlternateColorCodes('&', "&cDisabled")));

                                    if (sender.hasPermission(STATUS_SELF_PERMISSION, true)) {
                                        // Show player status for the sender (no target specified)
                                        if (!sender.isConsole()) {
                                            showPlayerStatus(sender, Base.INSTANCE.getPlatformServer().getPlayer(sender.getUniqueId()));
                                        }
                                    } else {
                                        sender.sendMessage(ChatUtil.translateAlternateColorCodes('&', "&cYou do not have permisssion to check your knockbacksync status."));
                                    }
                            } else {
                                if (sender.hasPermission(STATUS_OTHER_PERMISSION, true)) {
                                    PlatformPlayer target = targetSelector.getSinglePlayer();
                                    showPlayerStatus(sender, target);
                                } else {
                                    sender.sendMessage(ChatUtil.translateAlternateColorCodes('&', "&cYou do not have permission to check the status of other players!"));
                                }
                            }
                        })
        );
        Base.INSTANCE.getEventBus().registerListeners(this);
    }

    private void showPlayerStatus(Sender sender, PlatformPlayer target) {
        boolean globalStatus = configManager.isToggled();
        UUID uuid = target.getUUID();
        boolean playerStatus = PlayerDataManager.containsPlayerData(uuid);

        String statusMessage = ChatUtil.translateAlternateColorCodes('&', playerStatusMessage.replace("%player%", target.getName()));

        if (!globalStatus) {
            sender.sendMessage(statusMessage + ChatUtil.translateAlternateColorCodes('&', playerDisabledGlobalMessage));
        } else {
            sender.sendMessage(statusMessage +
                    (playerStatus
                            ? ChatUtil.translateAlternateColorCodes('&', "&aEnabled")
                            : ChatUtil.translateAlternateColorCodes('&', "&cDisabled")));
        }
    }

    @KBSyncEventHandler
    public void onConfigReload(ConfigReloadEvent event) {
        updateConfigValues();
    }

    private void updateConfigValues() {
        ConfigWrapper config = configManager.getConfigWrapper();
        this.globalStatusMessage = config.getString("global_status_message", "&eGlobal KnockbackSync status: ");
        this.playerStatusMessage = config.getString("player_status_message", "&e%player%'s KnockbackSync status: ");
        this.playerDisabledGlobalMessage = config.getString("player_disabled_global_message", "&cDisabled (Global toggle is off)");
    }
}
