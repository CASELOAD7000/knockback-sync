package me.caseload.knockbacksync.world;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import me.caseload.knockbacksync.world.raytrace.FluidHandling;
import me.caseload.knockbacksync.world.raytrace.RayTraceResult;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class FabricWorld implements PlatformWorld {
    private final World world;

    public FabricWorld(World world) {
        this.world = world;
    }

    @Override
    public WrappedBlockState getBlockStateAt(int x, int y, int z) {
        BlockState blockState = this.world.getBlockState(new BlockPos(x, y, z));
        return WrappedBlockState.getByGlobalId(Block.STATE_IDS.getRawId(blockState));
    }

    @Override
    public WrappedBlockState getBlockStateAt(Vector3d loc) {
        return getBlockStateAt((int) Math.floor(loc.x), (int) Math.floor(loc.x), (int) Math.floor(loc.x));
    }

    @Override
    public RayTraceResult rayTraceBlocks(Vector3d start, Vector3d direction, double maxDistance, FluidHandling fluidHandling, boolean ignorePassableBlocks) {
        Vec3d startVec = new Vec3d(start.getX(), start.getY(), start.getZ());
        Vec3d endVec = startVec.add(direction.getX() * maxDistance, direction.getY() * maxDistance, direction.getZ() * maxDistance);

        BlockHitResult result = world.raycast(new RaycastContext(
                startVec,
                endVec,
                RaycastContext.ShapeType.OUTLINE,
                fluidHandling == FluidHandling.NONE ? RaycastContext.FluidHandling.NONE :
                        fluidHandling == FluidHandling.SOURCE_ONLY ? RaycastContext.FluidHandling.SOURCE_ONLY :
                                RaycastContext.FluidHandling.ANY,
                ShapeContext.absent()
        ));

        if (result.getType() == HitResult.Type.MISS) return null;

        Vec3d hitLocation = result.getPos();
        BlockPos blockPos = result.getBlockPos();
        return new RayTraceResult(
                new Vector3d(hitLocation.x, hitLocation.y, hitLocation.z),
                BlockFace.getBlockFaceByValue(result.getSide().ordinal()),
                new Vector3i(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                getBlockStateAt(blockPos.getX(), blockPos.getY(), blockPos.getZ())
        );
    }
}