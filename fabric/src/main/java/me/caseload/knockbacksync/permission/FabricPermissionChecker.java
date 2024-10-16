package me.caseload.knockbacksync.permission;

import me.caseload.knockbacksync.player.FabricPlayer;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;

public class FabricPermissionChecker implements PermissionChecker {
    @Override
    public boolean hasPermission(CommandSourceStack source, String s, boolean defaultIfUnset) {
        return Permissions.check(source, s, defaultIfUnset);
    }

    @Override
    public boolean hasPermission(PlatformPlayer platformPlayer, String s) {
        return Permissions.check(((FabricPlayer) platformPlayer).fabricPlayer, s);
    }
}
