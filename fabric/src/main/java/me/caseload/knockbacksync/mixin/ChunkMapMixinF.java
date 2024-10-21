//package me.caseload.knockbacksync.mixin;
//
//import me.caseload.knockbacksync.ConfigWrapper;
//import me.caseload.knockbacksync.KnockbackSyncBase;
//import me.caseload.knockbacksync.event.ConfigReloadEvent;
//import me.caseload.knockbacksync.event.KBSyncEventHandler;
//import me.caseload.knockbacksync.util.CustomUpdateIntervals;
//import net.minecraft.Util;
//import net.minecraft.server.level.ChunkMap;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.boss.EnderDragonPart;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Mixin(ChunkMap.class)
//public class ChunkMapMixin {
//
//    @Shadow @Final
//    private Map<Integer, ChunkMap.TrackedEntity> entityMap;
//    @Shadow @Final private ServerLevel level;
//
//    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
//    private void onAddEntity(Entity entity, CallbackInfo ci) {
//        if (!(entity instanceof EnderDragonPart)) {
//            EntityType<?> entityType = entity.getType();
//            int trackingRange = entityType.clientTrackingRange() * 16;
//
//            if (trackingRange != 0) {
//                String entityTypeName = entityType.builtInRegistryHolder().getRegisteredName();
//                ConfigWrapper config = KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper();
//
//                int defaultInterval = entityType.updateInterval();
//                int customInterval = config.getInt("entity_update_intervals." + entityTypeName, defaultInterval);
//
//                if (this.entityMap.containsKey(entity.getId())) {
//                    throw (IllegalStateException) Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
//                } else {
//                    ChunkMap.TrackedEntity trackedEntity = new ChunkMap.TrackedEntity(entity, trackingRange, customInterval, entityType.trackDeltas());
//                    this.entityMap.put(entity.getId(), trackedEntity);
//                    trackedEntity.updatePlayers(this.level.players());
//
//                    if (entity instanceof ServerPlayer serverPlayer) {
//                        this.updatePlayerStatus(serverPlayer, true);
//
//                        for (ChunkMap.TrackedEntity trackedEntity2 : this.entityMap.values()) {
//                            if (trackedEntity2.entity != serverPlayer) {
//                                trackedEntity2.updatePlayer(serverPlayer);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        ci.cancel();
//    }
//
//    @Shadow
//    private void updatePlayerStatus(ServerPlayer player, boolean track) {
//        // This is a shadow method, implementation is provided by Minecraft
//    }
//
//    @KBSyncEventHandler
//    public void onConfigReload(ConfigReloadEvent event) {
//        Map<String, Integer> newIntervals = CustomUpdateIntervals.loadEntityIntervalsFromConfig();
//
//        for (ChunkMap.TrackedEntity trackedEntity : this.entityMap.values()) {
//            Entity entity = trackedEntity.entity;
//            String entityTypeName = entity.getType().builtInRegistryHolder().getRegisteredName();
//            int newInterval = newIntervals.getOrDefault(entityTypeName, entity.getType().updateInterval());
//
//            // Assuming TrackedEntity has a method to update its update interval
//            trackedEntity.setUpdateInterval(newInterval);
//        }
//    }
//}