package me.caseload.knockbacksync.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class BukkitCommandOperations implements CommandOperations {
    @Override
    public void sendSuccess(CommandSourceStack source, Supplier<Component> messageSupplier, boolean broadcast) {

    }

    @Override
    public void sendFailure(CommandSourceStack source, Supplier<Component> messageSupplier) {

    }

    @Override
    public Component createComponent(String message) {
        return null;
    }
}
