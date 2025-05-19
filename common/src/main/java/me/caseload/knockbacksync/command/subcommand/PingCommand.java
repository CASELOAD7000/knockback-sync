package me.caseload.knockbacksync.command.subcommand;

import com.github.retrooper.packetevents.protocol.player.User;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.ConfigWrapper;
import me.caseload.knockbacksync.command.generic.BuilderCommand;
import me.caseload.knockbacksync.command.generic.PlayerSelector;
import me.caseload.knockbacksync.event.KBSyncEventHandler;
import me.caseload.knockbacksync.event.events.ConfigReloadEvent;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlayerData;
import me.caseload.knockbacksync.sender.Sender;
import me.caseload.knockbacksync.util.ChatUtil;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.PredicatePermission;

import java.util.UUID;
import java.util.function.Predicate;

public class PingCommand implements BuilderCommand {

    private static final ConfigManager configManager = Base.INSTANCE.getConfigManager();

    private String pingSelfAvailableMessage;
    private String pingSelfUnavailableMessage;
    private String pingOtherAvailableMessage;
    private String pingOtherUnavailableMessage;

    private String mustSpecifyPlayerFromConsoleMessage;
    private String knockbacksyncDisabledForYouMessage;
    private String knockbacksyncDisabledForTargetMessage;

    public PingCommand() {
        loadConfig();
    }

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
                .optional("target", Base.INSTANCE.getPlayerSelectorParser().descriptor())
                .handler(context -> {
                    PlayerSelector targetSelector = context.getOrDefault("target", null);

                    if (targetSelector == null) {
                        if (context.sender().isConsole()) {
                            context.sender().sendMessage(ChatUtil.translateAlternateColorCodes('&', mustSpecifyPlayerFromConsoleMessage));
                        } else {
                            context.sender().sendMessage(getPingMessage(context.sender().getUniqueId(), context.sender().getName(), null, null));
                        }
                    } else {
                        context.sender().sendMessage(getPingMessage(context.sender().getUniqueId(), context.sender().getName(), targetSelector.getSinglePlayer().getUUID(), targetSelector.getSinglePlayer().getName()));
                    }
                })
        );
        Base.INSTANCE.getEventBus().registerListeners(this);
    }
    private void loadConfig() {
        ConfigWrapper configWrapper = configManager.getConfigWrapper();
        pingSelfAvailableMessage = configWrapper.getString("messages.ping.self.available",
                "Your real ping is &b%ping%&rms. Jitter: &b%jitter%&rms. Spike: &b%spike%&r. Compensated ping: &b%compensated%&rms.");
        pingSelfUnavailableMessage = configWrapper.getString("messages.ping.self.unavailable",
                "Accurate ping unavailable. Your estimated ping is &b%ping%&rms.");
        pingOtherAvailableMessage = configWrapper.getString("messages.ping.other.available",
                "%player%'s real ping is &b%ping%&rms. Jitter: &b%jitter%&rms. Spike: &b%spike%&r. Compensated ping: &b%compensated%&rms.");
        pingOtherUnavailableMessage = configWrapper.getString("messages.ping.other.unavailable",
                "Accurate ping unavailable. %player%'s estimated ping is &b%ping%&rms.");
        mustSpecifyPlayerFromConsoleMessage = configWrapper.getString("messages.console.must_specify_player",
                "&cYou must specify a player to use the knockbacksync ping command from the console.");
        knockbacksyncDisabledForYouMessage = configWrapper.getString("messages.disabled.self",
                "&cKnockback synchronization is currently disabled for you!");
        knockbacksyncDisabledForTargetMessage = configWrapper.getString("messages.disabled.target",
                "&cKnockback synchronization is currently disabled for the target player.");
    }

    private String getPingMessage(UUID senderUUID, String senderName, UUID targetUUID, String targetName) {
        boolean isSelf = senderUUID.equals(targetUUID) || targetUUID == null;
        String message;
        if (isSelf) {
            message = knockbacksyncDisabledForYouMessage;
            targetUUID = senderUUID;
            targetName = senderName;
        } else {
            message = knockbacksyncDisabledForTargetMessage;
        }

        User targetUser = Base.INSTANCE.getPlatformServer().getPlayer(targetUUID).getUser();
        if (targetUser == null)
            return ChatUtil.translateAlternateColorCodes('&',
                    configManager.getPlayerDisconnectedWhileExecutingCommand()
            ).replace("%player%", targetName);

        PlayerData playerData = PlayerDataManager.getPlayerData(targetUser);
        if (playerData == null)
            return ChatUtil.translateAlternateColorCodes('&', message);

        String rawReturnString;
        if (playerData.getPing() == null) {
            rawReturnString = isSelf ? pingSelfUnavailableMessage : pingOtherUnavailableMessage;
        } else {
            rawReturnString = isSelf ? pingSelfAvailableMessage : pingOtherAvailableMessage;
        }

        rawReturnString = rawReturnString
                .replace("%player%", playerData.getPlatformPlayer().getName())
                .replace("%ping%", playerData.getPing() == null ?
                        String.valueOf(playerData.getPlatformPlayer().getPing()) :
                        String.format("%.3f", playerData.getPing()))
                .replace("%jitter%", String.format("%.3f", playerData.getJitter()))
                .replace("%spike%", String.valueOf(playerData.isSpike()))
                .replace("%compensated%", String.format("%.3f", playerData.getCompensatedPing()));

        return ChatUtil.translateAlternateColorCodes('&', rawReturnString);
    }

    @KBSyncEventHandler
    public void onConfigReload(ConfigReloadEvent event) {
        loadConfig();
    }
}
