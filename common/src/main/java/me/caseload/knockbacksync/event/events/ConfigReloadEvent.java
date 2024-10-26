package me.caseload.knockbacksync.event.events;

import lombok.Getter;
import me.caseload.knockbacksync.event.Event;
import me.caseload.knockbacksync.manager.ConfigManager;

@Getter
public class ConfigReloadEvent extends Event {
    private final ConfigManager configManager;

    public ConfigReloadEvent(ConfigManager configManager) {
        this.configManager = configManager;
    }

}