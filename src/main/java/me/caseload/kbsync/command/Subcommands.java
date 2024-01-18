package me.caseload.kbsync.command;

import me.caseload.kbsync.KbSync;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Subcommands implements CommandExecutor, TabCompleter {

    private final Map<UUID, Integer> ping;

    public Subcommands(Map<UUID, Integer> ping) {
        this.ping = ping;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !sender.hasPermission("kbsync.admin")) {
            sender.sendMessage(ChatColor.GOLD + "This server is running the " + ChatColor.YELLOW + "KbSync " + ChatColor.GOLD + "plugin, " + ChatColor.DARK_AQUA + "Version: " + ChatColor.AQUA + KbSync.getInstance().getDescription().getVersion() + ChatColor.DARK_AQUA + ", Author: " + ChatColor.AQUA + "caseload" + ChatColor.DARK_AQUA + ". " + ChatColor.GRAY + "https://github.com/CASELOAD7000/knockback-sync");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "ping":
                if (args.length >= 2) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player != null && player.isOnline()) {
                        long estimatedPing = ping.get(player.getUniqueId()) == null ? player.getPing() : ping.get(player.getUniqueId());
                        sender.sendMessage(ChatColor.GOLD + player.getName() + "'s latest ping: " + ChatColor.YELLOW + estimatedPing);
                    }
                } else {
                    Player player = (Player) sender;
                    long estimatedPing = ping.get(player.getUniqueId()) == null ? player.getPing() : ping.get(player.getUniqueId());
                    player.sendMessage(ChatColor.GOLD + "Your latest ping: " + ChatColor.YELLOW + estimatedPing);
                }
                break;
            default:
                sender.sendMessage(ChatColor.GOLD + "This server is running the " + ChatColor.YELLOW + "KbSync " + ChatColor.GOLD + "plugin, " + ChatColor.DARK_AQUA + "Version: " + ChatColor.AQUA + KbSync.getInstance().getDescription().getVersion() + ChatColor.DARK_AQUA + ", Author: " + ChatColor.AQUA + "caseload" + ChatColor.DARK_AQUA + ". " + ChatColor.GRAY + "https://github.com/CASELOAD7000/knockback-sync");        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("ping");
        } else if (args.length == 2) {
            for (Player onlinePlayer : sender.getServer().getOnlinePlayers()) {
                completions.add(onlinePlayer.getName());
            }
        }

        return completions;
    }
}
