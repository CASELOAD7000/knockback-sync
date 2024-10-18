package me.caseload.knockbacksync.permission;

import me.caseload.knockbacksync.command.PlatformSender;
import me.caseload.knockbacksync.player.PlatformPlayer;

public interface PermissionChecker {
    boolean hasPermission(Object CommandSourceStack, String s, boolean defaultIfUnset);

    boolean hasPermission(PlatformSender source, String s, boolean defaultIfUnset);

    boolean hasPermission(PlatformPlayer platform, String s);
}
