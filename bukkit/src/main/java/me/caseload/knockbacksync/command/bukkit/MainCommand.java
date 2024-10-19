package me.caseload.knockbacksync.command.bukkit;

import dev.jorel.commandapi.CommandAPICommand;
import me.caseload.knockbacksync.KBSyncBukkitBase;
import me.caseload.knockbacksync.command.bukkit.subcommand.PingSubcommand;
import me.caseload.knockbacksync.command.bukkit.subcommand.ReloadSubcommand;
import me.caseload.knockbacksync.command.bukkit.subcommand.StatusSubcommand;
import me.caseload.knockbacksync.command.bukkit.subcommand.ToggleSubcommand;
import me.caseload.knockbacksync.sender.Sender;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MainCommand {

    protected KBSyncBukkitBase kbSyncBukkitBase;

    public MainCommand(KBSyncBukkitBase kbSyncBukkitBase) {
        this.kbSyncBukkitBase = kbSyncBukkitBase;
    }

    public boolean onCommand(CommandSender sender, String[] args) {
        Sender wraapped = this.kbSyncBukkitBase.getSenderFactory().wrap(sender);
        return true;
    }

    public void register() {
        new CommandAPICommand("knockbacksync")
                .withAliases("kbsync")
                .withSubcommand(new PingSubcommand().getCommand())
                .withSubcommand(new ToggleSubcommand().getCommand())
                .withSubcommand(new ReloadSubcommand().getCommand())
                .withSubcommand(new StatusSubcommand().getCommand())
                .executes((sender, args) -> {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes(
                            '&',
                            "&6This server is running the &eKnockbackSync &6plugin. &bhttps://github.com/CASELOAD7000/knockback-sync"
                    ));
                })
                .register();
    }
}