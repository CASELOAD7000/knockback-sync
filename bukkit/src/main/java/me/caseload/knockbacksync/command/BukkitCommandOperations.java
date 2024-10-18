package me.caseload.knockbacksync.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.bukkit.Bukkit;

import java.util.function.Supplier;

public class BukkitCommandOperations implements CommandOperations {

    public BukkitCommandOperations() {
//        Bukkit.getServer().getClass().getDeclaredMethod("getServer").invoke(Bukkit.getServer());
    }

    @Override
    public void sendSuccess(CommandSourceStack source, Supplier<Component> messageSupplier, boolean broadcast) {
        source.sendSuccess(messageSupplier.get(), broadcast);
    }

    @Override
    public void sendFailure(CommandSourceStack source, Supplier<Component> messageSupplier) {
        source.sendFailure(messageSupplier.get());
    }

    @Override
    public Component createComponent(String message) {
        return Component.literal(message);
    }
}
