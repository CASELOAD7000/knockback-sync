package me.caseload.knockbacksync.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import me.caseload.knockbacksync.manager.PingManager;
import org.bukkit.entity.Player;

public class PingSubcommand {

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("ping")
                .withPermission("knockbacksync.ping")
                .withArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    assert target != null;

                    long ping = PingManager.getPingMap().getOrDefault(target.getUniqueId(), (long) target.getPing());
                    sender.sendMessage(target.getName() + "'s last ping packet took " + ping + "ms");
                });
    }
}