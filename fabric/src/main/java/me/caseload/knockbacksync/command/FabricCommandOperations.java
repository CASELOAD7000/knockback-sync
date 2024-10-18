package me.caseload.knockbacksync.command;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class FabricCommandOperations implements CommandOperations {

    @Override
    public void sendSuccess(CommandSourceStack source, Supplier<Component> messageSupplier, boolean broadcast) {
        source.sendSuccess(messageSupplier, broadcast);
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
