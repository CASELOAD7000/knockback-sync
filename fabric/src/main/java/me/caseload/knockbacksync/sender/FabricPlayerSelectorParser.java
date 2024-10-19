package me.caseload.knockbacksync.sender;

import me.caseload.knockbacksync.command.AbstractPlayerSelectorParser;
import me.caseload.knockbacksync.command.PlayerSelector;
import org.incendo.cloud.minecraft.modded.parser.VanillaArgumentParsers;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;

public class FabricPlayerSelectorParser<C> extends AbstractPlayerSelectorParser<C> {

    @Override
    public ParserDescriptor<C, PlayerSelector> descriptor() {
        return ParserDescriptor.of(createArgumentParser(), PlayerSelector.class);
    }

    @Override
    public ArgumentParser<C, ?> getPlatformSpecificParser() {
        return VanillaArgumentParsers.<C>singlePlayerSelectorParser().parser();
    }

    @Override
    public PlayerSelector adaptToCommonSelector(Object platformSpecificSelector) {
        return new FabricPlayerSelectorAdapter((org.incendo.cloud.minecraft.modded.data.SinglePlayerSelector) platformSpecificSelector);
    }
}
