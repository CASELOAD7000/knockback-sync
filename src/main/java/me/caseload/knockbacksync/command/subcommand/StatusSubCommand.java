package me.caseload.knockbacksync.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.permission.PermissionChecker;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.ChatColor;

import java.util.UUID;

public class StatusSubCommand implements Command<CommandSourceStack> {

    private static final PermissionChecker permissionChecker = KnockbackSyncBase.INSTANCE.getPermissionChecker();

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ConfigManager configManager = KnockbackSyncBase.INSTANCE.getConfigManager();
        ServerPlayer sender = context.getSource().getPlayerOrException(); // Get the sender as a ServerPlayer

        // Show global status
        boolean globalStatus = configManager.isToggled();
        sendMessage(context, ChatColor.YELLOW + "Global KnockbackSync status: " +
                (globalStatus ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));

        // Show player status for the sender (no target specified)
        showPlayerStatus(context, sender, sender);

        return Command.SINGLE_SUCCESS;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("status")
                .requires(source -> permissionChecker.hasPermission(source, "knockbacksync.status.self", true)) // Requires at least self permission
                .executes(new StatusSubCommand()) // Execute for self status
                .then(Commands.argument("target", EntityArgument.player())
                        .requires(source -> permissionChecker.hasPermission(source, "knockbacksync.status.other", true)) // Requires other permission for target
                        .executes(context -> {
                            EntitySelector selector = context.getArgument("target", EntitySelector.class);
                            ServerPlayer target = selector.findSinglePlayer(context.getSource());
                            ServerPlayer sender = context.getSource().getPlayerOrException();
                            showPlayerStatus(context, sender, target);
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private static void showPlayerStatus(CommandContext<CommandSourceStack> context, ServerPlayer sender, ServerPlayer target) {
        ConfigManager configManager = KnockbackSyncBase.INSTANCE.getConfigManager();
        boolean globalStatus = configManager.isToggled();
        UUID uuid = target.getUUID();
        boolean playerStatus = PlayerDataManager.containsPlayerData(uuid);

        if (!globalStatus) {
            sendMessage(context, ChatColor.YELLOW + target.getDisplayName().getString() + "'s KnockbackSync status: " +
                    ChatColor.RED + "Disabled (Global toggle is off)");
        } else {
            sendMessage(context, ChatColor.YELLOW + target.getDisplayName().getString() + "'s KnockbackSync status: " +
                    (playerStatus ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        }
    }

    private static void sendMessage(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), false);
    }
}
