package me.caseload.knockbacksync.command.subcommand;
import dev.jorel.commandapi.CommandAPICommand;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.KnockbackSyncPlugin;
import org.bukkit.ChatColor;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.permission.PermissionChecker;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.bukkit.ChatColor;

public class ToggleOffGroundSubcommand implements Command<CommandSourceStack> {

    private static final PermissionChecker permissionChecker = KnockbackSyncBase.INSTANCE.getPermissionChecker();

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        boolean toggledState = !KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().getBoolean("toggle_experimental_offground", false);
        KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().set("toggle_experimental_offground", toggledState);
        KnockbackSyncBase.INSTANCE.getConfigManager().saveConfig();
        String message = ChatColor.translateAlternateColorCodes('&',
                toggledState ?
                        KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().getString("enable_message", "&aSuccessfully enabled offground experiment.") :
                        KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().getString("disable_message", "&cSuccessfully disabled offground experiment.")
        );
        sendMessage(context, message);
        return Command.SINGLE_SUCCESS;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("toggleoffground")
                .requires(source -> permissionChecker.hasPermission(source, "knockbacksync.toggleoffground", false))
                .executes(new ToggleOffGroundSubcommand());
    }

    private static void sendMessage(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), false);
    }
}