package me.caseload.knockbacksync.world;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import me.caseload.knockbacksync.world.raytrace.FluidHandling;
import me.caseload.knockbacksync.world.raytrace.RayTraceResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FabricWorld implements PlatformWorld {
    private final Level world;

    public FabricWorld(Level world) {
        this.world = world;
    }

    @Override
    public WrappedBlockState getBlockStateAt(int x, int y, int z) {
        BlockState blockState = this.world.getBlockState(new BlockPos(x, y, z));
        return WrappedBlockState.getByGlobalId(Block.BLOCK_STATE_REGISTRY.getId(blockState));
    }

    @Override
    public WrappedBlockState getBlockStateAt(Vector3d loc) {
        return getBlockStateAt((int) Math.floor(loc.x), (int) Math.floor(loc.x), (int) Math.floor(loc.x));
    }

    @Override
    public RayTraceResult rayTraceBlocks(Vector3d start, Vector3d direction, double maxDistance, FluidHandling fluidHandling, boolean ignorePassableBlocks) {
        Vec3 startVec = new Vec3(start.getX(), start.getY(), start.getZ());
        Vec3 endVec = startVec.add(direction.getX() * maxDistance, direction.getY() * maxDistance, direction.getZ() * maxDistance);

        BlockHitResult result = world.clip(new ClipContext(
                startVec,
                endVec,
                ClipContext.Block.OUTLINE,
                fluidHandling == FluidHandling.NONE ? ClipContext.Fluid.NONE :
                        fluidHandling == FluidHandling.SOURCE_ONLY ? ClipContext.Fluid.SOURCE_ONLY :
                                ClipContext.Fluid.ANY,
                (Entity) null
        ));

        if (result.getType() == HitResult.Type.MISS) return null;

        Vec3 hitLocation = result.getLocation();
        BlockPos blockPos = result.getBlockPos();
        return new RayTraceResult(
                new Vector3d(hitLocation.x, hitLocation.y, hitLocation.z),
                BlockFace.getBlockFaceByValue(result.getDirection().ordinal()),
                new Vector3i(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                getBlockStateAt(blockPos.getX(), blockPos.getY(), blockPos.getZ())
        );
    }
}