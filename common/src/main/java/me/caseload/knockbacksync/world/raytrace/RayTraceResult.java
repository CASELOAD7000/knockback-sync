package me.caseload.knockbacksync.world.raytrace;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.Getter;

@Getter
public class RayTraceResult {
    private final Vector3d hitPosition;
    private final BlockFace blockFace;
    private final Vector3i hitBlockPosition;
    private final WrappedBlockState hitBlock;

    public RayTraceResult(Vector3d hitPosition, BlockFace blockFace, Vector3i hitBlockPosition, WrappedBlockState hitBlock) {
        this.hitPosition = hitPosition;
        this.blockFace = blockFace;
        this.hitBlockPosition = hitBlockPosition;
        this.hitBlock = hitBlock;
    }

}