package me.caseload.knockbacksync.command.bukkit.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlayerData;
import org.bukkit.entity.Player;

public class PingSubcommand {

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("ping")
                .withPermission("knockbacksync.ping")
                .withArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    assert target != null;

                    PlayerData playerData = PlayerDataManager.getPlayerData(target.getUniqueId());
                    sender.sendMessage(target.getName() + "'s last ping packet took " + playerData.getPing() + "ms.");
                });
    }
}