package me.caseload.knockbacksync.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.command.subcommand.StatusSubCommand;
import me.caseload.knockbacksync.command.subcommand.ToggleOffGroundSubcommand;
import me.caseload.knockbacksync.command.subcommand.ToggleSubCommand;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.manager.PlayerData;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.permission.PermissionChecker;
import me.caseload.knockbacksync.util.ChatUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

public class KnockbackSyncCommand implements Command<CommandSourceStack> {

    private static final PermissionChecker permissionChecker = KnockbackSyncBase.INSTANCE.getPermissionChecker();

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        // Use the builder pattern to create a styled message
        MutableComponent message = Component.literal("This server is running the ")
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFAA00))) // Gold color

                .append(Component.literal("KnockbackSync")
                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFF55)))) // Yellow color

                .append(Component.literal(" plugin. ")
                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFAA00)))) // Gold color

                .append(Component.literal("https://github.com/CASELOAD7000/knockback-sync")
                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x55FFFF)))); // Aqua color

        // Send the styled message
        context.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("knockbacksync")
                .then(Commands.literal("ping")
                        .requires(source -> permissionChecker.hasPermission(source, "knockbacksync.ping", true))
                        .executes(context -> { // Added .executes() here to handle no target
                            if (context.getSource().getEntity() instanceof ServerPlayer) {
                                ServerPlayer sender = (ServerPlayer) context.getSource().getEntity();
                                PlayerData playerData = PlayerDataManager.getPlayerData(sender.getUUID());
                                context.getSource().sendSuccess(() -> {
                                    if (playerData.getPing() == null) {
                                        return Component.literal("your first ping packet has not been processed. Estimated ping is " + playerData.getEstimatedPing() + "ms.");
                                    } else {
                                        return Component.literal("your last ping packet took " + playerData.getPing() + "ms.");
                                    }
                                }, false);
                            } else {
                                context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("This command can only be used by players."));
                            }
                            return 1;
                        })
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> {
                                    EntitySelector selector = context.getArgument("target", EntitySelector.class);
                                    ServerPlayer target = selector.findSinglePlayer(context.getSource());
                                    PlayerData playerData = PlayerDataManager.getPlayerData(target.getUUID());
                                    context.getSource().sendSuccess(() -> {
                                        if (playerData.getPing() == null) {
                                            return Component.literal(target.getDisplayName().getString() + "’s first ping packet has not been processed. Estimated ping is " + playerData.getEstimatedPing() + "ms.");
                                        } else {
                                            return Component.literal(target.getDisplayName().getString() + "’s last ping packet took " + playerData.getPing() + "ms.");
                                        }
                                    }, false);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("reload")
                    .requires(source -> permissionChecker.hasPermission(source, "knockbacksync.reload", false))
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
                    })
                )
                .then(StatusSubCommand.build())
                .then(ToggleSubCommand.build())
                .then(ToggleOffGroundSubcommand.build())
                .executes(new KnockbackSyncCommand());
    }
}