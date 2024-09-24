package me.caseload.knockbacksync.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import me.caseload.knockbacksync.KnockbackSync;
import org.bukkit.ChatColor;

public class ToggleSubcommand {

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("toggle")
                .withPermission("knockbacksync.toggle")
                .executes((sender, args) -> {
                    boolean toggledState = !KnockbackSync.getInstance().isToggled();

                    KnockbackSync.getInstance().getConfig().set("enabled", toggledState);
                    KnockbackSync.getInstance().saveConfig();
                    KnockbackSync.getInstance().setToggled(toggledState);

                    String message = ChatColor.translateAlternateColorCodes('&',
                            toggledState ?
                                    KnockbackSync.getInstance().getConfig().getString("enable_message", "&aSuccessfully enabled KnockbackSync.") :
                                    KnockbackSync.getInstance().getConfig().getString("disable_message", "&cSuccessfully disabled KnockbackSync.")
                    );

                    sender.sendMessage(message);
                });
    }
}