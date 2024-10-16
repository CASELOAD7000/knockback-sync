package me.caseload.knockbacksync.permission;

import lombok.SneakyThrows;
import me.caseload.knockbacksync.player.BukkitPlayer;
import me.caseload.knockbacksync.player.PlatformPlayer;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;

public class PluginPermissionChecker implements PermissionChecker {

    Method getBukkitSenderMethod;

    public PluginPermissionChecker() {
        try {
            getBukkitSenderMethod = CommandSourceStack.class.getMethod("getBukkitSender");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public boolean hasPermission(CommandSourceStack source, String s, boolean defaultIfUnset) {
        return ((CommandSender) getBukkitSenderMethod.invoke(source)).hasPermission(s);
    }

    @Override
    public boolean hasPermission(PlatformPlayer platformPlayer, String s) {
        return ((BukkitPlayer) platformPlayer).bukkitPlayer.hasPermission(s);
    }
}
