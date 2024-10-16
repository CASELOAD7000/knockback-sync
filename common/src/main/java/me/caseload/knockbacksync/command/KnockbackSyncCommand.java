package me.caseload.knockbacksync.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.command.subcommand.*;
import me.caseload.knockbacksync.permission.PermissionChecker;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class KnockbackSyncCommand implements Command<CommandSourceStack> {

    private static final PermissionChecker permissionChecker = KnockbackSyncBase.INSTANCE.getPermissionChecker();

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
}