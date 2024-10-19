package me.caseload.knockbacksync.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.command.generic.BuilderCommand;
import me.caseload.knockbacksync.permission.PermissionChecker;
import me.caseload.knockbacksync.sender.Sender;
import me.caseload.knockbacksync.util.ChatUtil;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.PredicatePermission;

import java.util.function.Predicate;

public class ToggleOffGroundSubcommand implements BuilderCommand {

    private static final PermissionChecker permissionChecker = KnockbackSyncBase.INSTANCE.getPermissionChecker();

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
                    boolean toggledState = !KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().getBoolean("enable_experimental_offground", false);
                    KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().set("enable_experimental_offground", toggledState);
                    KnockbackSyncBase.INSTANCE.getConfigManager().saveConfig();
                    String message = ChatUtil.translateAlternateColorCodes('&',
                            toggledState ?
                                    KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().getString("enable_experimental_offground_message", "&aSuccessfully enabled offground experiment.") :
                                    KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().getString("disable_experimental_offground_message", "&cSuccessfully disabled offground experiment."));
                    commandContext.sender().sendMessage(message);
                }));
    }
}