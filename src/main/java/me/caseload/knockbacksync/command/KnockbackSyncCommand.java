package me.caseload.knockbacksync.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.manager.PlayerData;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

public class KnockbackSyncCommand implements Command<CommandSourceStack> {

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        // Check if the sender is a player
        context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("Does not work"));
//        if (context.getSource().getEntity() instanceof ServerPlayer) {
//            ServerPlayer sender = (ServerPlayer) context.getSource().getEntity();
//            PlayerData playerData = PlayerDataManager.getPlayerData(sender.getUUID());
//            context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Your last ping packet took " + playerData.getPing() + "ms."), false);
//        } else {
//            context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("This command can only be used by players."));
//        }
        return 1;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("knockbacksync")
                .executes(new KnockbackSyncCommand())
                .then(Commands.literal("ping")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> { // Added .executes() here to handle no target
                            if (context.getSource().getEntity() instanceof ServerPlayer) {
                                ServerPlayer sender = (ServerPlayer) context.getSource().getEntity();
                                PlayerData playerData = PlayerDataManager.getPlayerData(sender.getUUID());
                                context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Your last ping packet took " + playerData.getPing() + "ms."), false);
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
                                    context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(target.getDisplayName().getString() + "’s last ping packet took " + playerData.getPing() + "ms."), false);
                                    return 1;
                                })
                        )
                );
    }
}