package me.caseload.knockbacksync.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import org.bukkit.ChatColor;

public class TestSubcommand {

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("test")
                .executesPlayer((player, args) -> {
                    player.sendMessage(String.valueOf(PlayerDataManager.isExempt(player.getUniqueId())));
                });
    }
}
