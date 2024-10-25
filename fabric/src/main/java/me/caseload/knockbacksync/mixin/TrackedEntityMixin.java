//package me.caseload.knockbacksync.mixin;
//
//import me.caseload.knockbacksync.ConfigWrapper;
//import me.caseload.knockbacksync.KnockbackSyncBase;
//import net.minecraft.core.SectionPos;
//import net.minecraft.server.level.ChunkMap;
//import net.minecraft.server.level.ServerEntity;
//import net.minecraft.world.entity.Entity;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.gen.Accessor;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(ChunkMap.TrackedEntity.class)
//public class TrackedEntityMixin {
//    @Shadow
//    ServerEntity serverEntity;
//    Entity entity;
//    int range;
//    SectionPos lastSectionPos;
//
//    @Inject(method = "<init>", at = @At("HEAD"))
//    private void onInit(ChunkMap outer, Entity entity, int range, int updateInterval, boolean trackDelta, CallbackInfo ci) {
//
//        String entityTypeName = entity.getType().builtInRegistryHolder().getRegisteredName();
//        ConfigWrapper config = KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper();
//        int customInterval = config.getInt("entity_update_intervals." + entityTypeName, updateInterval);
//
//        this.serverEntity = new ServerEntity(ChunkMap.this.level, entity, customInterval, trackDelta, this::broadcast);
//        this.entity = entity;
//        this.range = range;
//        this.lastSectionPos = SectionPos.of(entity);
//    }
//
////    @Accessor("updateInterval")
////    public void setUpdateInterval(int interval) {
//        // This method will be implemented by the mixin system
////    }
//}