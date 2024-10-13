package me.caseload.knockbacksync.command.subcommand;
import dev.jorel.commandapi.CommandAPICommand;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.KnockbackSyncPlugin;
import org.bukkit.ChatColor;

public class ToggleOffGroundSubcommand {
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("toggleoffground")
                .withPermission("knockbacksync.toggleoffground")
                .executes((sender, args) -> {
                    boolean toggledState = !KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().getBoolean("toggle_experimental_offground", false);
                    KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().set("toggle_experimental_offground", toggledState);
                    KnockbackSyncBase.INSTANCE.getConfigManager().saveConfig();
                    String message = ChatColor.translateAlternateColorCodes('&',
                            toggledState ?
                                    KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().getString("enable_message", "&aSuccessfully enabled offground experiment.") :
                                    KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().getString("disable_message", "&cSuccessfully disabled offground experiment.")
                    );
                    sender.sendMessage(message);
                });
    }
}