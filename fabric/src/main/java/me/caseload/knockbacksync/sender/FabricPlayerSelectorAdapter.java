package me.caseload.knockbacksync.sender;

import me.caseload.knockbacksync.command.generic.PlayerSelector;
import me.caseload.knockbacksync.player.FabricPlayer;
import me.caseload.knockbacksync.player.PlatformPlayer;

import java.util.Collection;
import java.util.Collections;

public class FabricPlayerSelectorAdapter implements PlayerSelector {
    private final org.incendo.cloud.minecraft.modded.data.SinglePlayerSelector fabricSelector;

    public FabricPlayerSelectorAdapter(org.incendo.cloud.minecraft.modded.data.SinglePlayerSelector fabricSelector) {
        this.fabricSelector = fabricSelector;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public PlatformPlayer getSinglePlayer() {
        return new FabricPlayer(fabricSelector.single()); // Assuming your ServerPlayer can be cast to Player
    }

    @Override
    public Collection<PlatformPlayer> getPlayers() {
        return Collections.singletonList(new FabricPlayer(fabricSelector.single())); // Assuming your ServerPlayer can be cast to Player
    }

    @Override
    public String inputString() {
        return fabricSelector.inputString();
    }
}
