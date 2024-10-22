package me.caseload.knockbacksync.command.subcommand;

import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.command.generic.BuilderCommand;
import me.caseload.knockbacksync.sender.Sender;
import me.caseload.knockbacksync.util.ChatUtil;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.PredicatePermission;

import java.util.function.Predicate;

public class ToggleOffGroundSubcommand implements BuilderCommand {

    @Override
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
                    boolean toggledState = !Base.INSTANCE.getConfigManager().getConfigWrapper().getBoolean("enable_experimental_offground", false);
                    Base.INSTANCE.getConfigManager().getConfigWrapper().set("enable_experimental_offground", toggledState);
                    Base.INSTANCE.getConfigManager().saveConfig();
                    String message = ChatUtil.translateAlternateColorCodes('&',
                            toggledState ?
                                    Base.INSTANCE.getConfigManager().getConfigWrapper().getString("enable_experimental_offground_message", "&aSuccessfully enabled offground experiment.") :
                                    Base.INSTANCE.getConfigManager().getConfigWrapper().getString("disable_experimental_offground_message", "&cSuccessfully disabled offground experiment."));
                    commandContext.sender().sendMessage(message);
                }));
    }
}