package me.caseload.knockbacksync.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.permission.PermissionChecker;
import me.caseload.knockbacksync.player.PlayerData;
import me.caseload.knockbacksync.util.ChatUtil;
import me.caseload.knockbacksync.util.CommandUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

import static me.caseload.knockbacksync.util.CommandUtil.sendFailureMessage;
import static me.caseload.knockbacksync.util.CommandUtil.sendSuccessMessage;

public class ToggleSubCommand implements Command<CommandSourceStack> {

    private static final PermissionChecker permissionChecker = KnockbackSyncBase.INSTANCE.getPermissionChecker();

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("toggle")
                .requires(source -> permissionChecker.hasPermission(source, "knockbacksync.toggle.self", true)) // Requires at least self permission
                .executes(new ToggleSubCommand()) // Execute for self toggle
                .then(Commands.argument("target", EntityArgument.player())
//                        .requires(source -> permissionChecker.hasPermission(source, "knockbacksync.toggle.other", true)) // Requires other permission for target
                        .executes(context -> {
                            ConfigManager configManager = KnockbackSyncBase.INSTANCE.getConfigManager();
                            EntitySelector selector = context.getArgument("target", EntitySelector.class);
                            ServerPlayer target = selector.findSinglePlayer(context.getSource());
                            ServerPlayer sender = context.getSource().getPlayerOrException();
                            if (!target.equals(sender) && permissionChecker.hasPermission(context.getSource(), "knockbacksync.toggle.self", true)) {
                                CommandUtil.sendFailureMessage(context, "You do not have permission to toggle the knockback of other player's.");
                                return 0;
                            }

                            if (!configManager.isToggled()) {
                                sendSuccessMessage(context, ChatUtil.translateAlternateColorCodes('&', "&cKnockbacksync is currently disabled on this server. Contact your server administrator for more information."));
                            } else {
                                togglePlayerKnockback(target, configManager, context);
                            }

                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private static void toggleGlobalKnockback(ConfigManager configManager, CommandContext<CommandSourceStack> context) {
        boolean toggledState = !configManager.isToggled();
        configManager.setToggled(toggledState);

        KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().set("enabled", toggledState);
        KnockbackSyncBase.INSTANCE.getConfigManager().saveConfig();

        String message = ChatUtil.translateAlternateColorCodes('&',
                toggledState ? configManager.getEnableMessage() : configManager.getDisableMessage()
        );
        sendSuccessMessage(context, message);
    }

    private static void togglePlayerKnockback(ServerPlayer target, ConfigManager configManager, CommandContext<CommandSourceStack> context) {
        UUID uuid = target.getUUID();

        if (PlayerDataManager.shouldExempt(uuid)) {
            String message = ChatUtil.translateAlternateColorCodes('&',
                    configManager.getPlayerIneligibleMessage()
            ).replace("%player%", target.getDisplayName().getString());

            sendSuccessMessage(context, message);
            return;
        }

        boolean hasPlayerData = PlayerDataManager.containsPlayerData(uuid);
        if (hasPlayerData)
            PlayerDataManager.removePlayerData(uuid);
        else {
            PlayerDataManager.addPlayerData(uuid, new PlayerData(KnockbackSyncBase.INSTANCE.platformServer.getPlayer(uuid)));
//            switch (KnockbackSyncBase.INSTANCE.platform) {
//                case BUKKIT:
//                case FOLIA:
//                    org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(target.getUUID());
//                    PlayerDataManager.addPlayerData(uuid, new PlayerData(target.getUUID());
//                case FABRIC:
//                    PlayerDataManager.addPlayerData(uuid, new PlayerData(target));
//            }

        }

        String message = ChatUtil.translateAlternateColorCodes('&',
                hasPlayerData ? configManager.getPlayerDisableMessage() : configManager.
                        getPlayerEnableMessage()
        ).replace("%player%", target.getDisplayName().getString());

        sendSuccessMessage(context, message);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ConfigManager configManager = KnockbackSyncBase.INSTANCE.getConfigManager();
        ServerPlayer sender = context.getSource().getPlayerOrException();

        // Global toggle
        if (permissionChecker.hasPermission(context.getSource(), "knockbacksync.toggle.global", false)) {
            toggleGlobalKnockback(configManager, context);
        } else {
            sendSuccessMessage(context, ChatUtil.translateAlternateColorCodes('&', "&cYou don't have permission to toggle the global setting."));
        }

        return Command.SINGLE_SUCCESS;
    }
}