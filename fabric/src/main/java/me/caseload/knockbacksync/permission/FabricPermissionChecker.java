package me.caseload.knockbacksync.permission;

import me.caseload.knockbacksync.command.PlatformSender;
import me.caseload.knockbacksync.player.FabricPlayer;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;

public class FabricPermissionChecker implements PermissionChecker {

    public boolean hasPermission(CommandSourceStack source, String permission) {
        return Permissions.check(source, permission);
    }

    @Override
    public boolean hasPermission(Object commandSourceStack, String s, boolean defaultIfUnset) {
        if (commandSourceStack instanceof CommandSourceStack) {
            return Permissions.check((CommandSourceStack) commandSourceStack, s);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public boolean hasPermission(PlatformSender source, String s, boolean defaultIfUnset) {
        return false;
    }

    @Override
    public boolean hasPermission(PlatformPlayer platformPlayer, String s) {
        return Permissions.check(((FabricPlayer) platformPlayer).fabricPlayer, s);
    }
}
