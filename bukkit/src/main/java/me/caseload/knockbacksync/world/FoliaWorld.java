package me.caseload.knockbacksync.world;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.World;
import org.bukkit.block.BlockState;

public class FoliaWorld extends SpigotWorld {
    public FoliaWorld(World world) {
        super(world);
    }

    @Override
    public WrappedBlockState getBlockStateAt(int x, int y, int z) {
        BlockState block = this.world.getBlockState(x, y, z);
        return SpigotConversionUtil.fromBukkitBlockData(block.getBlockData());
    }
}