package me.caseload.knockbacksync.command.subcommand;

import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.command.generic.BuilderCommand;
import me.caseload.knockbacksync.command.generic.PlayerSelector;
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

    private static final ConfigManager configManager = KnockbackSyncBase.INSTANCE.getConfigManager();
    private static final String STATUS_SELF_PERMISSION = "knockbacksync.status.self";
    private static final String STATUS_OTHER_PERMISSION = "knockbacksync.status.other";

    public void register(CommandManager<Sender> manager) {
        manager.command(
                manager.commandBuilder("knockbacksync", "kbsync", "kbs")
                        .literal("status")
                        .optional("target", KnockbackSyncBase.INSTANCE.getPlayerSelectorParser().descriptor())
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
                                        sender.sendMessage(ChatUtil.translateAlternateColorCodes('&',
                                                configManager.getConfigWrapper().getString("global_status_message", "&eGlobal KnockbackSync status: ")) +
                                                (globalStatus
                                                        ? ChatUtil.translateAlternateColorCodes('&', "&aEnabled")
                                                        : ChatUtil.translateAlternateColorCodes('&', "&cDisabled")));

                                    if (sender.hasPermission(STATUS_SELF_PERMISSION, true)) {
                                        // Show player status for the sender (no target specified)
                                        if (!sender.isConsole()) {
                                            showPlayerStatus(sender, KnockbackSyncBase.INSTANCE.platformServer.getPlayer(sender.getUniqueId()), configManager);
                                        }
                                    } else {
                                        sender.sendMessage(ChatUtil.translateAlternateColorCodes('&', "&cYou do not have permisssion to check your knockbacksync status."));
                                    }
                            } else {
                                if (sender.hasPermission(STATUS_OTHER_PERMISSION)) {
                                    PlatformPlayer target = targetSelector.getSinglePlayer();
                                    showPlayerStatus(sender, target, configManager);
                                } else {
                                    sender.sendMessage(ChatUtil.translateAlternateColorCodes('&', "&cYou do not have permisssion to check the status of other players!"));
                                }
                            }
                        })
        );
    }

    private static void showPlayerStatus(Sender sender, PlatformPlayer target, ConfigManager configManager) {
        boolean globalStatus = configManager.isToggled();
        UUID uuid = target.getUUID();
        boolean playerStatus = PlayerDataManager.containsPlayerData(uuid);

        if (!globalStatus) {
            sender.sendMessage(ChatUtil.translateAlternateColorCodes('&',
                    configManager.getConfigWrapper().getString("player_status_message", "&e%player%'s KnockbackSync status: ")
                            .replace("%player%", target.getName()) +
                    ChatUtil.translateAlternateColorCodes('&',
                            configManager.getConfigWrapper().getString("player_disabled_global_message", "&cDisabled (Global toggle is off)"))));
        } else {
            sender.sendMessage(ChatUtil.translateAlternateColorCodes('&',
                    configManager.getConfigWrapper().getString("player_status_message", "&e%player%'s KnockbackSync status: ")
                            .replace("%player%", target.getName()) +
                    (playerStatus
                            ? ChatUtil.translateAlternateColorCodes('&', "&aEnabled")
                            : ChatUtil.translateAlternateColorCodes('&', "&cDisabled"))));
        }
    }
}
