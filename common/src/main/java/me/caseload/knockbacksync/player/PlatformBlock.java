package me.caseload.knockbacksync.player;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;

public interface PlatformBlock {
    WrappedBlockState getMaterial();
    // Add more methods as needed
}
