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

                        boolean isExempt = PlayerDataManager.isExempt(uuid);
                        PlayerDataManager.setExempt(uuid, !isExempt);

                        message = ChatColor.translateAlternateColorCodes('&',
                                isExempt ? configManager.getPlayerEnableMessage() : configManager.getPlayerDisableMessage()
                        ).replace("%player%", target.getName());
                    }

                    sender.sendMessage(message);
                });
    }
}