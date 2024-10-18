package me.caseload.knockbacksync.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.permission.PermissionChecker;
import me.caseload.knockbacksync.util.ChatUtil;
import me.caseload.knockbacksync.util.CommandUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import static me.caseload.knockbacksync.util.CommandUtil.sendSuccessMessage;

public class ToggleOffGroundSubcommand implements Command<CommandSourceStack> {

    private static final PermissionChecker permissionChecker = KnockbackSyncBase.INSTANCE.getPermissionChecker();

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("toggleoffground")
                .requires(source -> permissionChecker.hasPermission(source, "knockbacksync.toggleoffground", false))
                .executes(new ToggleOffGroundSubcommand());
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        boolean toggledState = !KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().getBoolean("enable_experimental_offground", false);
        KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().set("enable_experimental_offground", toggledState);
        KnockbackSyncBase.INSTANCE.getConfigManager().saveConfig();
        String message = ChatUtil.translateAlternateColorCodes('&',
                toggledState ?
                        KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().getString("enable_experimental_offground_message", "&aSuccessfully enabled offground experiment.") :
                        KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().getString("disable_experimental_offground_message", "&cSuccessfully disabled offground experiment.")
        );
        CommandUtil.sendSuccessMessage(context, message);
        return Command.SINGLE_SUCCESS;
    }
}