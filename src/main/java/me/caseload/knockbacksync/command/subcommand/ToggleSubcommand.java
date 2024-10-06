package me.caseload.knockbacksync.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

public class ToggleSubcommand implements Listener {

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("toggle")
                .withPermission("knockbacksync.toggle")
                .executes((sender, args) -> {
                    ConfigManager configManager = KnockbackSync.getInstance().getConfigManager();

                    boolean toggledState = !configManager.isToggled();
                    configManager.setToggled(toggledState);

                    KnockbackSync.getInstance().getConfig().set("enabled", toggledState);
                    KnockbackSync.getInstance().saveConfig();

                    String message = ChatColor.translateAlternateColorCodes('&',
                            toggledState ? configManager.getEnableMessage() : configManager.getDisableMessage()
                    );

                    sender.sendMessage(message);
                });
    }
}