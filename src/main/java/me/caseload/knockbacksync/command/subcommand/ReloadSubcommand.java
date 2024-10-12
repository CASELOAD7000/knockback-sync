package me.caseload.knockbacksync.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.KnockbackSyncPlugin;
import me.caseload.knockbacksync.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

public class ReloadSubcommand implements Listener {

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("reload")
                .withPermission("knockbacksync.reload")
                .executes((sender, args) -> {
                    ConfigManager configManager = KnockbackSyncBase.INSTANCE.getConfigManager();
                    configManager.loadConfig(true);

                    String reloadMessage = ChatColor.translateAlternateColorCodes('&',
                            configManager.getReloadMessage()
                    );

                    sender.sendMessage(reloadMessage);
                });
    }
}