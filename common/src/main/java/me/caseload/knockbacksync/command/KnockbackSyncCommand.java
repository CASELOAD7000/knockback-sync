package me.caseload.knockbacksync.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.caseload.knockbacksync.command.subcommand.*;
import me.caseload.knockbacksync.util.ChatUtil;
import me.caseload.knockbacksync.util.CommandUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class KnockbackSyncCommand implements Command<CommandSourceStack> {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("knockbacksync")
                .then(PingSubCommand.build())
                .then(ReloadSubCommand.build())
                .then(StatusSubCommand.build())
                .then(ToggleSubCommand.build())
                .then(ToggleOffGroundSubcommand.build())
                .executes(new KnockbackSyncCommand());
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandUtil.sendSuccessMessage(context, ChatUtil.translateAlternateColorCodes(
                '&',
                "&6This server is running the &eKnockbackSync &6plugin. &bhttps://github.com/CASELOAD7000/knockback-sync"
        ));
        return 1;
    }
}