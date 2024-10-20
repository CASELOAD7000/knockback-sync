package me.caseload.knockbacksync.permission;

import me.caseload.knockbacksync.player.PlatformPlayer;
import me.caseload.knockbacksync.sender.Sender;

public interface PermissionChecker {
    boolean hasPermission(Object nativeType, String s, boolean defaultIfUnset);

    boolean hasPermission(Sender source, String s, boolean defaultIfUnset);

    boolean hasPermission(PlatformPlayer platform, String s);
}
