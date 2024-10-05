package me.caseload.knockbacksync.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.config.KnockbackSyncConfigReloadEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ToggleSubcommand implements Listener {

    private String enableMessage = KnockbackSync.getInstance().getConfig().getString("enable_message", "&aSuccessfully enabled KnockbackSync.");
    private String disableMessage = KnockbackSync.getInstance().getConfig().getString("disable_message", "&cSuccessfully disabled KnockbackSync.");

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("toggle")
                .withPermission("knockbacksync.toggle")
                .executes((sender, args) -> {
                    boolean toggledState = !KnockbackSync.getInstance().isToggled();

                    KnockbackSync.getInstance().getConfig().set("enabled", toggledState);
                    KnockbackSync.getInstance().saveConfig();
                    KnockbackSync.getInstance().setToggled(toggledState);

                    String message = ChatColor.translateAlternateColorCodes('&',
                            toggledState ? enableMessage : disableMessage
                    );

                    sender.sendMessage(message);
                });
    }

    @EventHandler
    public void onConfigReload(KnockbackSyncConfigReloadEvent event) {
      enableMessage = KnockbackSync.getInstance().getConfig().getString("enable_message", "&aSuccessfully enabled KnockbackSync.");
      disableMessage = KnockbackSync.getInstance().getConfig().getString("disable_message", "&cSuccessfully disabled KnockbackSync.");
    }
}