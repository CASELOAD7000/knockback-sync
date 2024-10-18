package me.caseload.knockbacksync.command.bukkit.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import me.caseload.knockbacksync.command.PlatformSender;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlayerData;
import me.caseload.knockbacksync.util.ChatUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PingSubcommand {

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("ping")
                .withPermission("knockbacksync.ping")
                .withOptionalArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) {
                        if (sender instanceof ConsoleCommandSender) {
                            sender.sendMessage(ChatColor.RED + "You must specify a player to use this command from the console.");
                        } else if (sender instanceof Player playerSender) {
                            sender.sendMessage(ChatUtil.getPingMessage(playerSender.getUniqueId(), null));
                        }
                    } else {
                        if (sender instanceof ConsoleCommandSender) {
                            sender.sendMessage(ChatUtil.getPingMessage(PlatformSender.CONSOLE_UUID, target.getUniqueId()));
                        } else if (sender instanceof Player playerSender) {
                            sender.sendMessage(ChatUtil.getPingMessage(playerSender.getUniqueId(), target.getUniqueId()));
                        }
                    }
                });
    }
}