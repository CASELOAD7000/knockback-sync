package me.caseload.knockbacksync.command;

import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;

public abstract class AbstractPlayerSelectorParser<C> {

    public abstract ParserDescriptor<C, PlayerSelector> descriptor();

    protected abstract ArgumentParser<C, ?> getPlatformSpecificParser();

    protected abstract PlayerSelector adaptToCommonSelector(Object platformSpecificSelector);

    // Helper method to create the ArgumentParser
    protected ArgumentParser<C, PlayerSelector> createArgumentParser() {
        return (context, input) -> {
            // I know I'm using futures wrong but to bad!
            ArgumentParseResult<?> result = getPlatformSpecificParser().parseFuture(context, input).resultNow();
            if (result.failure().isPresent()) {
                return ArgumentParseResult.failure(result.failure().get());
            } else {
                return result.mapSuccess(this::adaptToCommonSelector);
            }
        };
    }
}
