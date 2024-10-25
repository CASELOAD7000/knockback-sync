package me.caseload.knockbacksync.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class CommandUtil {

    public static void sendSuccessMessage(CommandContext<CommandSourceStack> context, String message) {
        sendSuccessMessage(context, () -> getComponent(message), false);
    }

    public static void sendSuccessMessage(CommandContext<CommandSourceStack> context, String message, boolean allowLogging) {
        sendSuccessMessage(context, () -> getComponent(message), allowLogging);
    }

    public static void sendSuccessMessage(CommandContext<CommandSourceStack> context, Component message) {
        sendSuccessMessage(context, () -> message, false);
    }

    public static void sendSuccessMessage(CommandContext<CommandSourceStack> context, Component message, boolean allowLogging) {
        sendSuccessMessage(context, () -> message, allowLogging);
    }

    public static void sendSuccessMessage(CommandContext<CommandSourceStack> context, Supplier<Component> messageSupplier) {
        sendSuccessMessage(context, messageSupplier, false);
    }

    public static void sendSuccessMessage(CommandContext<CommandSourceStack> context, Supplier<Component> messageSupplier, boolean allowLogging) {
        context.getSource().sendSuccess(messageSupplier, allowLogging);
    }

    public static void sendFailureMessage(CommandContext<CommandSourceStack> context, String message) {
        sendFailureMessage(context, getComponent(message));
    }

    public static void sendFailureMessage(CommandContext<CommandSourceStack> context, Component message) {
        context.getSource().sendFailure(message);
    }

    public static Component getComponent(String message) {
        return Component.literal(message);
    }
}