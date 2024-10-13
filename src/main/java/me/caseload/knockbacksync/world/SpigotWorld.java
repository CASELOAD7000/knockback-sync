package me.caseload.knockbacksync.world;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import me.caseload.knockbacksync.util.BlockFaceUtil;
import me.caseload.knockbacksync.world.raytrace.FluidHandling;
import me.caseload.knockbacksync.world.raytrace.RayTraceResult;
import org.bukkit.FluidCollisionMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class SpigotWorld implements PlatformWorld {
    private final World world;

    public SpigotWorld(World world) {
        this.world = world;
    }

    @Override
    public WrappedBlockState getBlockStateAt(int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        return WrappedBlockState.getByString(block.getType().name());
    }

    @Override
    public WrappedBlockState getBlockStateAt(Vector3d loc) {
        return getBlockStateAt((int) Math.floor(loc.x), (int) Math.floor(loc.x), (int) Math.floor(loc.x));
    }

    @Override
    public RayTraceResult rayTraceBlocks(Vector3d start, Vector3d direction, double maxDistance, FluidHandling fluidHandling, boolean ignorePassableBlocks) {
        Vector startVec = new Vector(start.getX(), start.getY(), start.getZ());
        Vector directionVec = new Vector(direction.getX(), direction.getY(), direction.getZ());

        FluidCollisionMode fluidMode = (fluidHandling == FluidHandling.NONE) ? FluidCollisionMode.NEVER :
                (fluidHandling == FluidHandling.SOURCE_ONLY) ? FluidCollisionMode.SOURCE_ONLY :
                        FluidCollisionMode.ALWAYS;

        org.bukkit.util.RayTraceResult result = world.rayTraceBlocks(startVec.toLocation(world), directionVec, maxDistance, fluidMode, ignorePassableBlocks);

        if (result == null) return null;

        return new RayTraceResult(
                new Vector3d(result.getHitPosition().getX(), result.getHitPosition().getY(), result.getHitPosition().getZ()),
                BlockFaceUtil.getFrom(result.getHitBlockFace()),
                new Vector3i(result.getHitBlock().getX(),result.getHitBlock().getY(),result.getHitBlock().getZ()),
                result.getHitBlock() != null ? WrappedBlockState.getByString(result.getHitBlock().getType().name()) : null
        );
    }
}