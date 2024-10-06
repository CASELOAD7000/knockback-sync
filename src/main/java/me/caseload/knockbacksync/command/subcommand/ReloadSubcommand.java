package me.caseload.knockbacksync.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

public class ReloadSubcommand implements Listener {

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("reload")
                .withPermission("knockbacksync.reload")
                .executes((sender, args) -> {
                    ConfigManager configManager = KnockbackSync.getInstance().getConfigManager();
                    configManager.loadConfig(true);

                    String reloadMessage = ChatColor.translateAlternateColorCodes('&',
                            configManager.getReloadMessage()
                    );

                    sender.sendMessage(reloadMessage);
                });
    }
}