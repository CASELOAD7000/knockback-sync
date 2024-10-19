package me.caseload.knockbacksync.command;

import me.caseload.knockbacksync.command.generic.BuilderCommand;
import me.caseload.knockbacksync.sender.Sender;
import me.caseload.knockbacksync.util.ChatUtil;
import org.incendo.cloud.CommandManager;

public class MainCommand implements BuilderCommand {
    public void register(CommandManager<Sender> manager) {
        manager.command(
            manager.commandBuilder("knockbacksync", "kbsync", "kbs")
                .handler(context -> {
                    context.sender().sendMessage(
                        ChatUtil.translateAlternateColorCodes(
                                '&',
                                "&6This server is running the &eKnockbackSync &6plugin. &bhttps://github.com/CASELOAD7000/knockback-sync"
                        )
                    );
                })
        );
    }
}
