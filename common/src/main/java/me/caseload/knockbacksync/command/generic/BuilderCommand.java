package me.caseload.knockbacksync.command.generic;

import me.caseload.knockbacksync.sender.Sender;
import org.incendo.cloud.CommandManager;

public interface BuilderCommand {

    void register(CommandManager<Sender> manager);
}
