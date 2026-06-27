package me.caseload.knockbacksync.listener;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.HorizontalKnockbackMode;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.caseload.knockbacksync.player.PlayerData;

public abstract class PlayerKnockbackListener {

    private static final double VANILLA_FIXED_FLOOR_RATIO = 0.7;

    public void onPlayerVelocity(PlatformPlayer victim, Vector3d velocity) {
        if (!Base.INSTANCE.getConfigManager().isToggled())
            return;

        User user = victim.getUser();
        if (user == null) return; // Prevent errors with players disconnecting while this is running (or with fake player?)

        PlayerData victimPlayerData = PlayerDataManager.getPlayerData(user);
        if (victimPlayerData == null)
            return;

        WrappedBlockState blockState = victim.getWorld().getBlockStateAt(victim.getLocation());
        if (victim.isGliding() ||
                blockState.getType() == StateTypes.WATER ||
                blockState.getType() == StateTypes.LAVA ||
                blockState.getType() == StateTypes.COBWEB ||
                blockState.getType() == StateTypes.SCAFFOLDING)
            return;

        double newX = velocity.getX();
        double newY = velocity.getY();
        double newZ = velocity.getZ();
        boolean modified = false;

        // Horizontal (X/Z) magnitude + direction fix.
        // Runs independently of the ping-gate below: vanilla's low-KB and
        // wrong-direction bugs affect all players, not just high-ping ones.
        Vector3d cachedKB = victimPlayerData.getHorizontalKnockback();
        HorizontalKnockbackMode mode = Base.INSTANCE.getConfigManager().getHorizontalKnockbackMode();
        if (cachedKB != null && mode != HorizontalKnockbackMode.DISABLED) {
            switch (mode) {
                case SMOOTH:
                    newX = cachedKB.getX();
                    newZ = cachedKB.getZ();
                    modified = true;
                    break;
                case VANILLA_FIXED:
                    newX = clampMagnitude(velocity.getX(), cachedKB.getX(), VANILLA_FIXED_FLOOR_RATIO);
                    newZ = clampMagnitude(velocity.getZ(), cachedKB.getZ(), VANILLA_FIXED_FLOOR_RATIO);
                    modified = true;
                    break;
                default:
                    break;
            }
        }
        // Single-shot cache: clear so a later non-attack velocity event
        // (mob, explosion, fishing rod) does not reuse this vector.
        victimPlayerData.setHorizontalKnockback(null);

        // Vertical (Y) ping-compensated re-projection (existing behavior).
        if (victimPlayerData.getNotNullPing() >= PlayerData.PING_OFFSET) {
            double distanceToGround = victimPlayerData.getDistanceToGround();
            if (distanceToGround > 0) {
                if (victimPlayerData.isOnGroundClientSide(velocity.getY(), distanceToGround)) {
                    Integer damageTicks = victimPlayerData.getLastDamageTicks();
                    if (damageTicks == null || damageTicks <= 8) {
                        Double vv = victimPlayerData.getVerticalVelocity();
                        if (vv != null) {
                            newY = vv;
                            modified = true;
                        }
                    }
                } else if (victimPlayerData.isOffGroundSyncEnabled()) {
                    newY = victimPlayerData.getCompensatedOffGroundVelocity();
                    modified = true;
                }
            }
        }

        if (modified) {
            victim.setVelocity(new Vector3d(newX, newY, newZ));
        }
    }

    /**
     * If {@code actual} already points in the same direction as {@code intended} with
     * at least {@code floorRatio} of its magnitude, return {@code actual} unchanged.
     * Otherwise, return {@code intended * floorRatio} (preserving the sign of {@code intended}).
     */
    private static double clampMagnitude(double actual, double intended, double floorRatio) {
        double floor = intended * floorRatio;
        if (intended >= 0) {
            return actual >= floor ? actual : floor;
        } else {
            return actual <= floor ? actual : floor;
        }
    }
}
