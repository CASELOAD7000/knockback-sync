package me.caseload.knockbacksync.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import me.caseload.knockbacksync.KnockbackSync;
import org.bukkit.ChatColor;

public class ToggleSubcommand {

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("toggle")
                .withPermission("knockbacksync.toggle")
                .executes((sender, args) -> {
                    boolean toggledState = !KnockbackSync.getInstance().getConfig().getBoolean("enabled");

                    KnockbackSync.getInstance().getConfig().set("enabled", toggledState);
                    KnockbackSync.getInstance().saveConfig();

                    String message = ChatColor.translateAlternateColorCodes('&',
                            toggledState ?
                                    KnockbackSync.getInstance().getConfig().getString("enable_message", "&aSuccessfully enabled old potion physics.") :
                                    KnockbackSync.getInstance().getConfig().getString("disable_message", "&cSuccessfully disabled old potion physics.")
                    );

                    sender.sendMessage(message);
                });
    }
}