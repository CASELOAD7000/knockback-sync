package me.caseload.knockbacksync.permission;

import me.caseload.knockbacksync.player.BukkitPlayer;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.caseload.knockbacksync.sender.Sender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
    public boolean hasPermission(Object nativeType, String s, boolean defaultIfUnset) {
        if (nativeType instanceof CommandSender) {
            return ((CommandSender) nativeType).hasPermission(s);
        }
        throw new IllegalArgumentException("Attempted to check permission of an object that wasn't a platformplayer or Sender. This should never happen on Bukkit!");
    }

    @Override
    public boolean hasPermission(Sender source, String s, boolean defaultIfUnset) {
        return source.hasPermission(s, defaultIfUnset);
    }

    @Override
    public boolean hasPermission(PlatformPlayer platformPlayer, String s) {
        return ((BukkitPlayer) platformPlayer).bukkitPlayer.hasPermission(s);
    }
}
