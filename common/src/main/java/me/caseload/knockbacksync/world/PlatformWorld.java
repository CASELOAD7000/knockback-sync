package me.caseload.knockbacksync.world;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.world.raytrace.FluidHandling;
import me.caseload.knockbacksync.world.raytrace.RayTraceResult;

public interface PlatformWorld {
    WrappedBlockState getBlockStateAt(int x, int y, int z);

    WrappedBlockState getBlockStateAt(Vector3d loc);

    RayTraceResult rayTraceBlocks(Vector3d start, Vector3d direction, double maxDistance, FluidHandling fluidHandling, boolean ignorePassableBlocks);
}
