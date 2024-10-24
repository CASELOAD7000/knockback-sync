package me.caseload.knockbacksync.mixin;

import me.caseload.knockbacksync.KBSyncFabricLoaderMod;
import me.caseload.knockbacksync.callback.PlayerVelocityEvent;
import me.caseload.knockbacksync.event.ConfigReloadEvent;
import me.caseload.knockbacksync.event.KBSyncEventHandler;
import me.caseload.knockbacksync.util.CustomUpdateIntervals;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.util.Map;
//
//@Mixin(ServerEntity.class)
//public class ServerEntityMixin implements UpdateIntervalAccessor {

//
//    @Shadow
//    @Final
//    private Entity entity;
//
//    // Velocity event
//    @Inject(method = "sendChanges", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;hurtMarked:Z", ordinal = 1), cancellable = true)
//    private void onSendChanges(CallbackInfo ci) {
//        if (this.entity.hurtMarked && this.entity instanceof ServerPlayer player) {
//            Vec3 velocity = player.getDeltaMovement();
//
//            InteractionResult result = PlayerVelocityEvent.EVENT.invoker().onVelocityChange(player, velocity);
//
//            if (result == InteractionResult.FAIL) {
//                this.entity.hurtMarked = false;
//                ci.cancel();
//            } else if (result == InteractionResult.SUCCESS) {
//                // Currently unnecessary since we do this in the handler, will move later
//                player.setDeltaMovement(velocity);
//            }
//        }
//    }
//
    // Custom tick intervals
//    @Shadow
//    private int updateInterval;
//
//    @Accessor("entity")
//    Entity getEntity() {
//        return null;
//    }
//
//    @Invoker("sendChanges")
//    void invokeSendChanges() {
//
//    }
//
//    @Override
//    public void setUpdateInterval(int interval) {
//        this.updateInterval = interval;
//    }
//
//
//    @KBSyncEventHandler
//    public void onConfigReload(ConfigReloadEvent event) {
//        Map<String, Integer> entityIntervals = CustomUpdateIntervals.loadEntityIntervalsFromConfig();
//        for (ServerLevel level : KBSyncFabricLoaderMod.getServer().getAllLevels()) {
//            CustomUpdateIntervals.updateIntervals(level, entityIntervals);
//        }
//    }
//}