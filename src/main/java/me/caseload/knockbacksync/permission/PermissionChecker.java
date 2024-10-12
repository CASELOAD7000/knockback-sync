package me.caseload.knockbacksync.permission;

import net.minecraft.commands.CommandSourceStack;

public interface PermissionChecker {
    boolean hasPermission(CommandSourceStack source, String s, boolean defaultIfUnset);
}
