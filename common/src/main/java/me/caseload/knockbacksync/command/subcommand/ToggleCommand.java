package me.caseload.knockbacksync.command.subcommand;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.command.generic.BuilderCommand;
import me.caseload.knockbacksync.command.generic.PlayerSelector;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.permission.PermissionChecker;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.caseload.knockbacksync.player.PlayerData;
import me.caseload.knockbacksync.sender.Sender;
import me.caseload.knockbacksync.util.ChatUtil;
import org.incendo.cloud.CommandManager;

import java.util.UUID;

public class ToggleCommand implements BuilderCommand {

    private static final ConfigManager configManager = KnockbackSyncBase.INSTANCE.getConfigManager();
    private static final PermissionChecker permissionChecker = KnockbackSyncBase.INSTANCE.getPermissionChecker();

    @Override
    public void register(CommandManager<Sender> manager) {
        manager.command(
            manager.commandBuilder("knockbacksync", "kbsync", "kbs")
                .literal("toggle")
                .optional("target", KnockbackSyncBase.INSTANCE.getPlayerSelectorParser().descriptor())
                .handler(context -> {
                    Sender sender = context.sender();
                    PlayerSelector targetSelector = context.getOrDefault("target", null);
                    if (targetSelector == null) {
                        // Global toggle
                        if (permissionChecker.hasPermission(sender, "knockbacksync.toggle.global", false)) {
                            toggleGlobalKnockback(configManager, sender);
                        } else {
                            sender.sendMessage(ChatUtil.translateAlternateColorCodes('&', "&cYou don't have permission to toggle the global setting."));
                        }
                    } else {
                        PlatformPlayer target = targetSelector.getSinglePlayer();
                        boolean senderIsTarget = sender.getUniqueId() == target.getUUID();
                        if (!senderIsTarget && !permissionChecker.hasPermission(sender, "knockbacksync.toggle.other", false)) {
                            sender.sendMessage("You do not have permission to toggle the knockback of other player's.");
                            return;
                        } else if (senderIsTarget && !permissionChecker.hasPermission(sender, "knockbacksync.toggle.self", true)) {
                            sender.sendMessage("You do not have permission to toggle your knockback.");
                            return;
                        }

                        if (!configManager.isToggled()) {
                            sender.sendMessage(ChatUtil.translateAlternateColorCodes('&', "&cKnockbacksync is currently disabled on this server. Contact your server administrator for more information."));
                        } else {
                            togglePlayerKnockback(target, configManager, sender);
                        }
                    }
                })
        );
    }


    private static void toggleGlobalKnockback(ConfigManager configManager, Sender sender) {
        boolean toggledState = !configManager.isToggled();
        configManager.setToggled(toggledState);

        KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().set("enabled", toggledState);
        KnockbackSyncBase.INSTANCE.getConfigManager().saveConfig();

        String message = ChatUtil.translateAlternateColorCodes('&',
                toggledState ? configManager.getEnableMessage() : configManager.getDisableMessage()
        );
        sender.sendMessage(ChatUtil.translateAlternateColorCodes('&', message));
    }

    private static void togglePlayerKnockback(PlatformPlayer target, ConfigManager configManager, Sender sender) {
        UUID uuid = target.getUUID();

        if (PlayerDataManager.shouldExempt(uuid)) {
            String message = ChatUtil.translateAlternateColorCodes('&',
                    configManager.getPlayerIneligibleMessage()
            ).replace("%player%", target.getName());

            sender.sendMessage(message);
            return;
        }

        boolean hasPlayerData = PlayerDataManager.containsPlayerData(uuid);
        if (hasPlayerData)
            PlayerDataManager.removePlayerData(uuid);
        else {
            PlayerDataManager.addPlayerData(uuid, new PlayerData(KnockbackSyncBase.INSTANCE.platformServer.getPlayer(uuid)));
        }

        String message = ChatUtil.translateAlternateColorCodes('&',
                hasPlayerData ? configManager.getPlayerDisableMessage() : configManager.
                        getPlayerEnableMessage()
        ).replace("%player%", target.getName());

        sender.sendMessage(message);
    }
}