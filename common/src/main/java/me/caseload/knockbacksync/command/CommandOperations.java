package me.caseload.knockbacksync.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public interface CommandOperations {
    void sendSuccess(CommandSourceStack source, Supplier<Component> messageSupplier, boolean broadcast);
    void sendFailure(CommandSourceStack source, Supplier<Component> messageSupplier);
    Component createComponent(String message);
}
