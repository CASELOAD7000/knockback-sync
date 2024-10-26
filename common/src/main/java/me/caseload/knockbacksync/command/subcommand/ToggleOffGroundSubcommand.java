package me.caseload.knockbacksync.command.subcommand;

import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.ConfigWrapper;
import me.caseload.knockbacksync.command.generic.BuilderCommand;
import me.caseload.knockbacksync.event.KBSyncEventHandler;
import me.caseload.knockbacksync.event.events.ConfigReloadEvent;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.sender.Sender;
import me.caseload.knockbacksync.util.ChatUtil;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.PredicatePermission;

import java.util.function.Predicate;

public class ToggleOffGroundSubcommand implements BuilderCommand {

    private String enableOffGroundSyncMessage;
    private String disableOffGroundSyncMessage;
    private boolean offGroundSyncEnabled;

    public ToggleOffGroundSubcommand() {
        loadConfigSettings();
    }

    public void register(CommandManager<Sender> manager) {
        manager.command(
            manager.commandBuilder("knockbacksync", "kbsync", "kbs")
                .literal("toggleoffground")
                .permission((sender -> {
                    final String permission = "knockbacksync.toggleoffground";
                    Predicate<Sender> senderPredicate = (s) -> {
                        return s.hasPermission(permission, false);
                    };

                    return PredicatePermission.of(senderPredicate).testPermission(sender);
                }))
                .handler(commandContext -> {
                    boolean toggledState = !offGroundSyncEnabled;
                    Base.INSTANCE.getConfigManager().getConfigWrapper().set("enable_offground_synchronization", toggledState);
                    Base.INSTANCE.getConfigManager().saveConfig();
                    String message = ChatUtil.translateAlternateColorCodes('&',
                            toggledState ? enableOffGroundSyncMessage : disableOffGroundSyncMessage);
                    commandContext.sender().sendMessage(message);
                }));
    }

    @KBSyncEventHandler
    public void onConfigReloadEvent(ConfigReloadEvent event) {
        loadConfigSettings();
    }

    private void loadConfigSettings() {
        ConfigWrapper configWrapper = Base.INSTANCE.getConfigManager().getConfigWrapper();
        offGroundSyncEnabled = configWrapper.getBoolean("enable_offground_synchronization", false);
        enableOffGroundSyncMessage = configWrapper.getString("enable_offground_synchronization_message", "&aSuccessfully enabled offground experiment.");
        disableOffGroundSyncMessage = configWrapper.getString("disable_offground_synchronization_message", "&cSuccessfully disabled offground experiment.");
    }
}