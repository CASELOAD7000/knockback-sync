package me.caseload.knockbacksync.permission;

import me.caseload.knockbacksync.player.PlatformPlayer;
import net.minecraft.commands.CommandSourceStack;

public interface PermissionChecker {
    boolean hasPermission(CommandSourceStack source, String s, boolean defaultIfUnset);

    boolean hasPermission(PlatformPlayer platform, String s);
}
