package me.caseload.knockbacksync.util;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import net.minecraft.core.Direction;

public class BlockFaceUtil {
    public static BlockFace getFrom(Direction direction) {
        switch (direction) {
            case NORTH:
                return BlockFace.NORTH;
            case SOUTH:
                return BlockFace.SOUTH;
            case EAST:
                return BlockFace.EAST;
            case WEST:
                return BlockFace.WEST;
            case UP:
                return BlockFace.UP;
            case DOWN:
                return BlockFace.DOWN;
            default:
                return null;
        }
    }

    public static BlockFace getFrom(org.bukkit.block.BlockFace direction) {
        switch (direction) {
            case NORTH:
                return BlockFace.NORTH;
            case SOUTH:
                return BlockFace.SOUTH;
            case EAST:
                return BlockFace.EAST;
            case WEST:
                return BlockFace.WEST;
            case UP:
                return BlockFace.UP;
            case DOWN:
                return BlockFace.DOWN;
            default:
                return null;
        }
    }
}
