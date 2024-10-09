package me.caseload.knockbacksync.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.PlayerArgument;
import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

public class StatusSubcommand implements Listener {

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("status")
                .withPermission(CommandPermission.NONE)
                .withOptionalArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    ConfigManager configManager = KnockbackSync.getInstance().getConfigManager();
                    Player target = (Player) args.get("target");

                    // Show global status
                    boolean globalStatus = configManager.isToggled();
                    sender.sendMessage(ChatColor.YELLOW + "Global KnockbackSync status: " +
                            (globalStatus ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));

                    // Show player status
                    if (target == null && sender instanceof Player) {
                        target = (Player) sender;
                    }

                    if (target != null) {
                        if (sender.hasPermission("knockbacksync.status.self") ||
                                (sender.hasPermission("knockbacksync.status.other") && !sender.equals(target))) {

                            UUID uuid = target.getUniqueId();
                            boolean playerStatus = !PlayerDataManager.containsPlayerData(uuid);

                            if (!globalStatus) {
                                sender.sendMessage(ChatColor.YELLOW + target.getName() + "'s KnockbackSync status: " +
                                        ChatColor.RED + "Disabled (Global toggle is off)");
                            } else {
                                sender.sendMessage(ChatColor.YELLOW + target.getName() + "'s KnockbackSync status: " +
                                        (playerStatus ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You don't have permission to check the status for " +
                                    (sender.equals(target) ? "yourself" : "other players") + ".");
                        }
                    }
                });
    }
}