package me.caseload.knockbacksync.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.manager.PlayerData;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

public class ToggleSubcommand implements Listener {

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("toggle")
                .withPermission("knockbacksync.toggle")
                .withOptionalArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    ConfigManager configManager = KnockbackSync.getInstance().getConfigManager();
                    Player target = (Player) args.get("target");
                    String message;

                    if (target == null) {
                        boolean toggledState = !configManager.isToggled();
                        configManager.setToggled(toggledState);

                        KnockbackSync.getInstance().getConfig().set("enabled", toggledState);
                        KnockbackSync.getInstance().saveConfig();

                        message = ChatColor.translateAlternateColorCodes('&',
                                toggledState ? configManager.getEnableMessage() : configManager.getDisableMessage()
                        );
                    }
                    else {
                        UUID uuid = target.getUniqueId();

                        if (PlayerDataManager.shouldExempt(uuid)) {
                            message = ChatColor.translateAlternateColorCodes('&',
                                    configManager.getPlayerIneligibleMessage()
                            ).replace("%player%", target.getName());

                            sender.sendMessage(message);
                            return;
                        }

                        boolean hasPlayerData = PlayerDataManager.containsPlayerData(uuid);
                        if (hasPlayerData)
                            PlayerDataManager.removePlayerData(uuid);
                        else
                            PlayerDataManager.addPlayerData(uuid, new PlayerData(target));

                        message = ChatColor.translateAlternateColorCodes('&',
                                hasPlayerData ? configManager.getPlayerDisableMessage() : configManager.getPlayerEnableMessage()
                        ).replace("%player%", target.getName());
                    }

                    sender.sendMessage(message);
                });
    }
}