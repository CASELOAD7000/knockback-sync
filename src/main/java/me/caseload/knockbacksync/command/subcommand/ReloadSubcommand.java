package me.caseload.knockbacksync.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.config.KnockbackSyncConfigReloadEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ReloadSubcommand implements Listener {

    private String message;

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("reload")
                .withPermission("knockbacksync.reload")
                .executes((sender, args) -> {
                  KnockbackSync.getInstance().reloadConfig();
                  sender.sendMessage(message);
                  Bukkit.getPluginManager().callEvent(new KnockbackSyncConfigReloadEvent());
                });
    }

  @EventHandler
  public void onConfigReload(KnockbackSyncConfigReloadEvent event) {
    message = ChatColor.translateAlternateColorCodes('&',
        KnockbackSync.getInstance().getConfig().getString("reload_message", "&aSuccessfully reloaded KnockbackSync config.")
    );
  }
}