package me.caseload.knockbacksync.command.subcommand;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import me.caseload.knockbacksync.KnockbackSync;
import org.bukkit.ChatColor;

public class OffsetSubcommand {

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("offset")
                .withPermission("knockbacksync.offset")
                .withArguments(new IntegerArgument("offset"))
                .executes((sender, args) -> {
                    Integer offset = (Integer) args.get("offset");
                    assert offset != null;

                    KnockbackSync.getInstance().getConfig().set("ping_offset", offset);
                    KnockbackSync.getInstance().saveConfig();

                    String message = ChatColor.translateAlternateColorCodes('&',
                            KnockbackSync.getInstance().getConfig().getString("configure_offset_message", "&aSuccessfully configured the ping offset.")
                    );

                    sender.sendMessage(message);
                });
    }
}
