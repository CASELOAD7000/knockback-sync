package me.caseload.knockbacksync.permission;

import lombok.SneakyThrows;
import me.caseload.knockbacksync.command.PlatformSender;
import me.caseload.knockbacksync.player.BukkitPlayer;
import me.caseload.knockbacksync.player.PlatformPlayer;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;

public class PluginPermissionChecker implements PermissionChecker {

//    Method getBukkitSenderMethod;
//
//    public PluginPermissionChecker() {
//        try {
//            getBukkitSenderMethod = CommandSourceStack.class.getMethod("getBukkitSender");
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }
//    }

    // Currently shouldn't be called
    @Override
    public boolean hasPermission(Object CommandSourceStack, String s, boolean defaultIfUnset) {
        return false;
    }

    @Override
    public boolean hasPermission(PlatformSender source, String s, boolean defaultIfUnset) {
        return false;
    }

    @Override
    public boolean hasPermission(PlatformPlayer platformPlayer, String s) {
        return ((BukkitPlayer) platformPlayer).bukkitPlayer.hasPermission(s);
    }
}
