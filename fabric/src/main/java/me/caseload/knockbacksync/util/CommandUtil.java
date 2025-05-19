package me.caseload.knockbacksync.util;

import com.mojang.brigadier.context.CommandContext;
import java.util.function.Supplier;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandUtil {

    public static void sendSuccessMessage(CommandContext<ServerCommandSource> context, String message) {
        sendSuccessMessage(context, () -> getComponent(message), false);
    }

    public static void sendSuccessMessage(CommandContext<ServerCommandSource> context, String message, boolean allowLogging) {
        sendSuccessMessage(context, () -> getComponent(message), allowLogging);
    }

    public static void sendSuccessMessage(CommandContext<ServerCommandSource> context, Text message) {
        sendSuccessMessage(context, () -> message, false);
    }

    public static void sendSuccessMessage(CommandContext<ServerCommandSource> context, Text message, boolean allowLogging) {
        sendSuccessMessage(context, () -> message, allowLogging);
    }

    public static void sendSuccessMessage(CommandContext<ServerCommandSource> context, Supplier<Text> messageSupplier) {
        sendSuccessMessage(context, messageSupplier, false);
    }

    public static void sendSuccessMessage(CommandContext<ServerCommandSource> context, Supplier<Text> messageSupplier, boolean allowLogging) {
        context.getSource().sendFeedback(messageSupplier, allowLogging);
    }

    public static void sendFailureMessage(CommandContext<ServerCommandSource> context, String message) {
        sendFailureMessage(context, getComponent(message));
    }

    public static void sendFailureMessage(CommandContext<ServerCommandSource> context, Text message) {
        context.getSource().sendError(message);
    }

    public static Text getComponent(String message) {
        return Text.literal(message);
    }
}