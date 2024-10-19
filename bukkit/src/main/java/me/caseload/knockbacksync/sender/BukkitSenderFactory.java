package me.caseload.knockbacksync.sender;

import me.caseload.knockbacksync.KBSyncBukkitBase;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;

import java.util.UUID;

public class BukkitSenderFactory extends SenderFactory<KBSyncBukkitBase, CommandSender> implements SenderMapper<CommandSender, Sender> {
//    private final BukkitAudiences audiences;

    public BukkitSenderFactory(KBSyncBukkitBase kbSyncBukkitBase) {
        super(kbSyncBukkitBase);
//        this.audiences = BukkitAudiences.create(kbSyncBukkitBase.getLoader());
    }

    @Override
    protected String getName(CommandSender sender) {
        if (sender instanceof Player) {
            return sender.getName();
        }
        return Sender.CONSOLE_NAME;
    }

    @Override
    protected UUID getUniqueId(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getUniqueId();
        }
        return Sender.CONSOLE_UUID;
    }

    @Override
    protected void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

//    @Override
//    protected void sendMessage(CommandSender sender, Component message) {
//        // we can safely send async for players and the console - otherwise, send it sync
//        if (sender instanceof Player || sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
//            this.audiences.sender(sender).sendMessage(message);
//        } else {
//            getPlugin().getBootstrap().getScheduler().executeSync(() -> this.audiences.sender(sender).sendMessage(message));
//        }
//    }

    @Override
    protected boolean hasPermission(CommandSender sender, String node) {
        return sender.hasPermission(node);
    }

    @Override
    protected boolean hasPermission(CommandSender sender, String node, boolean defaultIfUnset) {
        return sender.hasPermission(new Permission(node, defaultIfUnset ? PermissionDefault.TRUE : PermissionDefault.FALSE));
    }

    @Override
    protected void performCommand(CommandSender sender, String command) {
        //
    }

    @Override
    protected boolean isConsole(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender;
    }

    @Override
    public @NonNull Sender map(@NonNull CommandSender base) {
        return this.wrap(base);
    }

    @Override
    public @NonNull CommandSender reverse(@NonNull Sender mapped) {
        return this.unwrap(mapped);
    }

    @Override
    public void close() throws Exception {

    }
}
