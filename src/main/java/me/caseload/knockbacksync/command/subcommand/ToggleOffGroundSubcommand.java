package me.caseload.knockbacksync.command.subcommand;
import dev.jorel.commandapi.CommandAPICommand;
import me.caseload.knockbacksync.KnockbackSync;
import org.bukkit.ChatColor;

public class ToggleOffGroundSubcommand {
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("toggleoffground")
                .withPermission("knockbacksync.toggleoffground")
                .executes((sender, args) -> {
                    boolean toggledState = !KnockbackSync.INSTANCE.getConfig().getBoolean("toggle_experimental_offground");
                    KnockbackSync.INSTANCE.getConfig().set("toggle_experimental_offground", toggledState);
                    KnockbackSync.INSTANCE.saveConfig();
                    String message = ChatColor.translateAlternateColorCodes('&',
                            toggledState ?
                                    KnockbackSync.INSTANCE.getConfig().getString("enable_message", "&aSuccessfully enabled offground experiment.") :
                                    KnockbackSync.INSTANCE.getConfig().getString("disable_message", "&cSuccessfully disabled offground experiment.")
                    );
                    sender.sendMessage(message);
                });
    }
}