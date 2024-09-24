package me.caseload.knockbacksync.command;

import dev.jorel.commandapi.CommandAPICommand;
import me.caseload.knockbacksync.command.subcommand.PingSubcommand;
import me.caseload.knockbacksync.command.subcommand.ToggleSubcommand;
import org.bukkit.ChatColor;

public class MainCommand {

    public void register() {
        new CommandAPICommand("knockbacksync")
                .withAliases("kbsync")
                .withSubcommand(new PingSubcommand().getCommand())
                .withSubcommand(new ToggleSubcommand().getCommand())
                .executes((sender, args) -> {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes(
                            '&',
                            "&6This server is running the &eKnockbackSync &6plugin. &bhttps://github.com/CASELOAD7000/knockback-sync"
                    ));
                })
                .register();
    }
}