package me.caseload.knockbacksync.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.PlayerArgument;
import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.util.permissions.CommandPermissions;

import java.util.UUID;

public class ToggleSubcommand implements Listener {

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("toggle")
                .withPermission(CommandPermission.NONE)
                .withOptionalArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    ConfigManager configManager = KnockbackSync.getInstance().getConfigManager();
                    Player target = (Player) args.get("target");
                    String message;

                    if (target == null) {
                        // Global toggle
                        if (sender.hasPermission("knockbacksync.toggle.global")) {
                            boolean toggledState = !configManager.isToggled();
                            configManager.setToggled(toggledState);

                            KnockbackSync.getInstance().getConfig().set("enabled", toggledState);
                            KnockbackSync.getInstance().saveConfig();

                            message = ChatColor.translateAlternateColorCodes('&',
                                    toggledState ? configManager.getEnableMessage() : configManager.getDisableMessage()
                            );
                        } else {
                            message = ChatColor.RED + "You don't have permission to toggle the global setting.";
                        }
                        sender.sendMessage(message);
                    } else {
                        // Player-specific toggle
                        if (!configManager.isToggled()) {
                            message = ChatColor.RED + "Knockbacksync is currently disabled on this server. Contact your server administrator for more information.";
                            sender.sendMessage(message);
                        } else if (sender instanceof Player && sender.equals(target) && sender.hasPermission("knockbacksync.toggle.self")) {
                            togglePlayerKnockback(target, configManager, sender);
                        } else if (sender.hasPermission("knockbacksync.toggle.other")) {
                            togglePlayerKnockback(target, configManager, sender);
                        } else {
                            message = ChatColor.RED + "You don't have permission to toggle knockback for " +
                                    (sender.equals(target) ? "yourself" : "other players") + ".";
                            sender.sendMessage(message);
                        }
                    }
                });
    }

    private void togglePlayerKnockback(Player target, ConfigManager configManager, CommandSender sender) {
        UUID uuid = target.getUniqueId();
        boolean isExempt = PlayerDataManager.isExempt(uuid);
        PlayerDataManager.setExempt(uuid, !isExempt);

        String message = ChatColor.translateAlternateColorCodes('&',
                isExempt ? configManager.getPlayerEnableMessage() : configManager.getPlayerDisableMessage()
        ).replace("%player%", target.getName());

        sender.sendMessage(message);
    }
}