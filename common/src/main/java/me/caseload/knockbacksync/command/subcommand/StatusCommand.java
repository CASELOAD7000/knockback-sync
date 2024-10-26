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

    private String globalStatusEnabledMessage;
    private String globalStatusDisabledMessage;
    private String globalOffGroundStatusEnabledMessage;
    private String globalOffGroundStatusDisabledMessage;
    private String playerStatusEnabledMessage;
    private String playerStatusDisabledMessage;
    private String playerStatusGlobalDisabledMessage;
    private String noSelfPermissionMessage;
    private String noOtherPermissionMessage;

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
                                sender.sendMessage(ChatUtil.translateAlternateColorCodes('&',
                                        (globalStatus ? globalStatusEnabledMessage : globalStatusDisabledMessage)
                                        + "\n"
                                        + (globalStatus ? globalOffGroundStatusEnabledMessage : globalOffGroundStatusDisabledMessage)
                                ));

                                    if (sender.hasPermission(STATUS_SELF_PERMISSION, true)) {
                                        // Show player status for the sender (no target specified)
                                        if (!sender.isConsole()) {
                                            showPlayerStatus(sender, Base.INSTANCE.getPlatformServer().getPlayer(sender.getUniqueId()));
                                        }
                                    } else {
                                        sender.sendMessage(ChatUtil.translateAlternateColorCodes('&', noSelfPermissionMessage));
                                    }
                            } else {
                                if (sender.hasPermission(STATUS_OTHER_PERMISSION, true)) {
                                    PlatformPlayer target = targetSelector.getSinglePlayer();
                                    showPlayerStatus(sender, target);
                                } else {
                                    sender.sendMessage(ChatUtil.translateAlternateColorCodes('&', noOtherPermissionMessage));
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

        String statusMessage;
        if (!globalStatus) {
            statusMessage = playerStatusGlobalDisabledMessage.replace("%player%", target.getName());
        } else {
           statusMessage = (playerStatus ? this.playerStatusEnabledMessage : this.playerStatusDisabledMessage).replace("%player%", target.getName());
        }
        sender.sendMessage(ChatUtil.translateAlternateColorCodes('&', statusMessage));
    }

    @KBSyncEventHandler
    public void onConfigReload(ConfigReloadEvent event) {
        updateConfigValues();
    }

    private void updateConfigValues() {
        ConfigWrapper config = configManager.getConfigWrapper();
        this.globalStatusEnabledMessage = config.getString("messages.status.global.enabled",
                "&KnockbackSync global status: &aEnabled");
        this.globalStatusDisabledMessage = config.getString("messages.status.global.disabled",
                "&KnockbackSync global status: &cDisabled");
        this.globalOffGroundStatusEnabledMessage = config.getString("messages.status.offground.enabled",
                "&eKnockbackSync off-ground status: &aEnabled");
        this.globalOffGroundStatusDisabledMessage = config.getString("messages.status.offground.disabled",
                "&eKnockbackSync off-ground status: &cDisabled");
        this.playerStatusEnabledMessage = config.getString("messages.status.player.enabled",
                "&e%player%'s KnockbackSync status: &aEnabled");
        this.playerStatusDisabledMessage = config.getString("messages.status.player.disabled",
                "&e%player%'s KnockbackSync status: &cDisabled");
        this.playerStatusGlobalDisabledMessage = config.getString("messages.status.player.global_disabled",
                "&e%player%'s KnockbackSync status: &cDisabled (Global toggle is off)");
        this.noSelfPermissionMessage = config.getString("messages.status.permission.no_self",
                "&cYou do not have permission to check your knockbacksync status.");
        this.noOtherPermissionMessage = config.getString("messages.status.permission.no_other",
                "&cYou do not have permission to check the status of other players!");
    }
}
