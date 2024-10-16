package me.caseload.knockbacksync.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.util.ChatUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ReloadSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("reload")
                .requires(source -> KnockbackSyncBase.INSTANCE.getPermissionChecker().hasPermission(source, "knockbacksync.reload", false))
                .executes(context -> {
                    ConfigManager configManager = KnockbackSyncBase.INSTANCE.getConfigManager();
                    configManager.loadConfig(true);

                    String rawReloadMessage = configManager.getReloadMessage();
                    String reloadMessage = ChatUtil.translateAlternateColorCodes('&', rawReloadMessage);

                    // Send the message to the command source (the player or console that executed the command)
                    context.getSource().sendSuccess(() ->
                                    Component.literal(reloadMessage),
                            false
                    );

                    return Command.SINGLE_SUCCESS;
                });
    }
}
