package me.caseload.knockbacksync.permission;

import me.caseload.knockbacksync.player.FabricPlayer;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.caseload.knockbacksync.sender.Sender;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;

public class FabricPermissionChecker implements PermissionChecker {

    public boolean hasPermission(CommandSourceStack source, String permission) {
        return Permissions.check(source, permission, false);
    }

    public boolean hasPermission(CommandSourceStack source, String permission, boolean defaultIfUnset) {
        return Permissions.check(source, permission, defaultIfUnset);
    }

    @Override
    public boolean hasPermission(Object nativeType, String s, boolean defaultIfUnset) {
        if (nativeType instanceof CommandSourceStack commandSourceStack) {
            return Permissions.check(commandSourceStack, s, defaultIfUnset);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public boolean hasPermission(Sender source, String s, boolean defaultIfUnset) {
        return source.hasPermission(s);
    }

    @Override
    public boolean hasPermission(PlatformPlayer platformPlayer, String s) {
        return Permissions.check(((FabricPlayer) platformPlayer).fabricPlayer, s);
    }
}
