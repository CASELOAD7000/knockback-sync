package me.caseload.knockbacksync.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.permission.PermissionChecker;
import me.caseload.knockbacksync.util.ChatUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class StatusSubCommand implements Command<CommandSourceStack> {

    private static final PermissionChecker permissionChecker = KnockbackSyncBase.INSTANCE.getPermissionChecker();

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("status")
                .requires(source -> permissionChecker.hasPermission(source, "knockbacksync.status.self", true)) // Requires at least self permission
                .executes(new StatusSubCommand()) // Execute for self status
                .then(Commands.argument("target", EntityArgument.player())
                        .requires(source -> permissionChecker.hasPermission(source, "knockbacksync.status.other", true)) // Requires other permission for target
                        .executes(context -> {
                            ConfigManager configManager = KnockbackSyncBase.INSTANCE.getConfigManager();
                            EntitySelector selector = context.getArgument("target", EntitySelector.class);
                            ServerPlayer target = selector.findSinglePlayer(context.getSource());
                            ServerPlayer sender = context.getSource().getPlayer();
                            showPlayerStatus(context, sender, target, configManager);
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private static void showPlayerStatus(CommandContext<CommandSourceStack> context, ServerPlayer sender, ServerPlayer target, ConfigManager configManager) {
        boolean globalStatus = configManager.isToggled();
        UUID uuid = target.getUUID();
        boolean playerStatus = PlayerDataManager.containsPlayerData(uuid);

        if (!globalStatus) {
            sendMessage(context, ChatUtil.translateAlternateColorCodes('&',
                    configManager.getConfigWrapper().getString("player_status_message", "&e%player%'s KnockbackSync status: ")
                            .replace("%player%", target.getDisplayName().getString())) +
                    ChatUtil.translateAlternateColorCodes('&',
                            configManager.getConfigWrapper().getString("player_disabled_global_message", "&cDisabled (Global toggle is off)")));
        } else {
            sendMessage(context, ChatUtil.translateAlternateColorCodes('&',
                    configManager.getConfigWrapper().getString("player_status_message", "&e%player%'s KnockbackSync status: ")
                            .replace("%player%", target.getDisplayName().getString())) +
                    (playerStatus
                            ? ChatUtil.translateAlternateColorCodes('&', "&aEnabled")
                            : ChatUtil.translateAlternateColorCodes('&', "&cDisabled")));
        }
    }

    private static void sendMessage(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(Component.literal(message), false);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ConfigManager configManager = KnockbackSyncBase.INSTANCE.getConfigManager();

        // Show global status
        boolean globalStatus = configManager.isToggled();
        sendMessage(context, ChatUtil.translateAlternateColorCodes('&',
                configManager.getConfigWrapper().getString("global_status_message", "&eGlobal KnockbackSync status: ")) +
                (globalStatus
                        ? ChatUtil.translateAlternateColorCodes('&', "&aEnabled")
                        : ChatUtil.translateAlternateColorCodes('&', "&cDisabled")));

        // Show player status for the sender (no target specified)
        if (context.getSource().getEntity() instanceof ServerPlayer sender) {
            showPlayerStatus(context, sender, sender, configManager);
        }

        return Command.SINGLE_SUCCESS;
    }
}
