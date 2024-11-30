package me.caseload.knockbacksync.world;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import io.github.retrooper.packetevents.util.folia.RegionScheduler;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.BukkitBase;
import me.caseload.knockbacksync.async.AsyncOperation;
import me.caseload.knockbacksync.async.FoliaOperation;
import me.caseload.knockbacksync.world.raytrace.FluidHandling;
import me.caseload.knockbacksync.world.raytrace.RayTraceResult;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.concurrent.CompletableFuture;

public class FoliaWorld extends SpigotWorld {

    private final RegionScheduler scheduler;
    private final JavaPlugin plugin = ((BukkitBase) Base.INSTANCE).getPlugin();

    public FoliaWorld(World world) {
        super(world);
        this.scheduler = FoliaScheduler.getRegionScheduler();
    }

    @Override
    public AsyncOperation<WrappedBlockState> getBlockStateAt(int x, int y, int z) {
        Location location = new Location(super.world, x, y, z);
        CompletableFuture<WrappedBlockState> future = new CompletableFuture<>();

        scheduler.execute(plugin, location, () -> {
            Block block = location.getBlock();
            future.complete(SpigotConversionUtil.fromBukkitBlockData(block.getBlockData()));
        });

        return new FoliaOperation<>(future);
    }

    @Override
    public AsyncOperation<RayTraceResult> rayTraceBlocks(Vector3d start, Vector3d direction, double maxDistance, FluidHandling fluidHandling, boolean ignorePassableBlocks) {
        Location location = new Location(super.world, start.getX(), start.getY(), start.getZ());
        CompletableFuture<RayTraceResult> future = new CompletableFuture<>();

        scheduler.execute(plugin, location, () -> {
            Vector startVec = new Vector(start.getX(), start.getY(), start.getZ());
            Vector directionVec = new Vector(direction.getX(), direction.getY(), direction.getZ());

            FluidCollisionMode fluidMode = (fluidHandling == FluidHandling.NONE) ? FluidCollisionMode.NEVER :
                    (fluidHandling == FluidHandling.SOURCE_ONLY) ? FluidCollisionMode.SOURCE_ONLY :
                            FluidCollisionMode.ALWAYS;

            org.bukkit.util.RayTraceResult result = super.world.rayTraceBlocks(startVec.toLocation(super.world), directionVec, maxDistance, fluidMode, ignorePassableBlocks);

            if (result == null) {
                future.complete(null);
                return;
            }

            future.complete(new RayTraceResult(
                    new Vector3d(result.getHitPosition().getX(), result.getHitPosition().getY(), result.getHitPosition().getZ()),
                    getBlockFaceFrom(result.getHitBlockFace()),
                    new Vector3i(result.getHitBlock().getX(), result.getHitBlock().getY(), result.getHitBlock().getY()),
                    result.getHitBlock() != null ? SpigotConversionUtil.fromBukkitBlockData(result.getHitBlock().getBlockData()) : null
            ));
        });

        return new FoliaOperation<>(future);
    }
}