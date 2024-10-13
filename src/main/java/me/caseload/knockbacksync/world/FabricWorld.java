package me.caseload.knockbacksync.world;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import me.caseload.knockbacksync.util.BlockFaceUtil;
import me.caseload.knockbacksync.world.raytrace.FluidHandling;
import me.caseload.knockbacksync.world.raytrace.RayTraceResult;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import com.github.retrooper.packetevents.util.Vector3d;

public class FabricWorld implements PlatformWorld {
    private final World world;

    public FabricWorld(World world) {
        this.world = world;
    }

    @Override
    public WrappedBlockState getBlockStateAt(int x, int y, int z) {
        return null;
    }

    @Override
    public WrappedBlockState getBlockStateAt(Vector3d loc) {
        return null;
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
                (Entity) null
        ));

        if (result.getType() == HitResult.Type.MISS) return null;

        BlockPos blockPos = result.getBlockPos();
        return new RayTraceResult(
                new Vector3d(result.getPos().x, result.getPos().y, result.getPos().z),
                BlockFaceUtil.getFrom(result.getSide()),
                new Vector3i(result.getBlockPos().getX(), result.getBlockPos().getY(), result.getBlockPos().getZ()),
                WrappedBlockState.getByString(world.getBlockState(blockPos).getBlock().toString())
        );
    }
}