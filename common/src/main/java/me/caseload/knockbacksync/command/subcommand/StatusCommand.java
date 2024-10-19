package me.caseload.knockbacksync.command.subcommand;

import me.caseload.knockbacksync.command.generic.BuilderCommand;
import me.caseload.knockbacksync.sender.Sender;
import org.incendo.cloud.CommandManager;

public class StatusCommand implements BuilderCommand {
    public void register(CommandManager<Sender> manager) {
        manager.command(
            manager.commandBuilder("knockbacksync", "kbsync", "kbs")
                    .literal("status")
                    .handler(context -> {
                        context.sender().sendMessage("Status");
                    })
        );
    }
}
