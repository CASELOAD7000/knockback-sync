package me.caseload.knockbacksync.sender;

import me.caseload.knockbacksync.command.AbstractPlayerSelectorParser;
import me.caseload.knockbacksync.command.PlayerSelector;
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;

public class BukkitPlayerSelectorParser<C> extends AbstractPlayerSelectorParser<C> {

    @Override
    public ParserDescriptor<C, PlayerSelector> descriptor() {
        return ParserDescriptor.of(createArgumentParser(), PlayerSelector.class);
    }

    @Override
    public ArgumentParser<C, ?> getPlatformSpecificParser() {
        return SinglePlayerSelectorParser.<C>singlePlayerSelectorParser().parser();
    }

    @Override
    public PlayerSelector adaptToCommonSelector(Object platformSpecificSelector) {
        return new BukkitPlayerSelectorAdapter((org.incendo.cloud.bukkit.data.SinglePlayerSelector) platformSpecificSelector);
    }
}