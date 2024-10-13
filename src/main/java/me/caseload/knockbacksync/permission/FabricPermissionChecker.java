package me.caseload.knockbacksync.permission;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;

public class FabricPermissionChecker implements PermissionChecker {
    @Override
    public boolean hasPermission(CommandSourceStack source, String s, boolean defaultIfUnset) {
        return true;
//        return Permissions.check(source, s, defaultIfUnset);
    }
}
