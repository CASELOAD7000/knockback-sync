package me.caseload.knockbacksync.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.BoundingBox;
import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.world.FabricWorld;
import me.caseload.knockbacksync.world.PlatformWorld;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class FabricPlayer implements PlatformPlayer {
    public final ServerPlayerEntity fabricPlayer;
    public final User user;

    private String clientBrand = "vanilla";

    public FabricPlayer(ServerPlayerEntity player) {
        this.fabricPlayer = player;
        this.user = PacketEvents.getAPI().getPlayerManager().getUser(fabricPlayer);
    }

    @Override
    public UUID getUUID() {
        return fabricPlayer.getUuid();
    }

    @Override
    public String getName() {
        return fabricPlayer.getName().getString();
    }

    @Override
    public double getX() {
        return fabricPlayer.getX();
    }

    @Override
    public double getY() {
        return fabricPlayer.getY();
    }

    @Override
    public double getZ() {
        return fabricPlayer.getZ();
    }

    @Override
    public float getPitch() {
        return fabricPlayer.getPitch();
    }

    @Override
    public float getYaw() {
        return fabricPlayer.getYaw();
    }

    @Override
    public boolean isOnGround() {
        return fabricPlayer.isOnGround();
    }

    @Override
    public int getPing() {
        return fabricPlayer.networkHandler.getLatency();
    }

    @Override
    public boolean isGliding() {
        return fabricPlayer.isGliding();
    }

    @Override
    public PlatformWorld getWorld() {
        return new FabricWorld(fabricPlayer.getWorld());
    }

    @Override
    public Vector3d getLocation() {
        return new Vector3d(fabricPlayer.getX(), fabricPlayer.getY(), fabricPlayer.getZ());
    }

    @Override
    public void sendMessage(@NotNull String s) {
        fabricPlayer.sendMessage(Text.literal(s));
    }

    @Override
    public double getAttackCooldown() {
        // this is what paper does I have no idea how this works
        return fabricPlayer.getAttackCooldownProgress(0.5f);
    }

    @Override
    public boolean isSprinting() {
        return fabricPlayer.isSprinting();
    }

    @Override
    public int getMainHandKnockbackLevel() {
        RegistryEntry<Enchantment> knockbackEntry = fabricPlayer.getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.KNOCKBACK);
        return EnchantmentHelper.getLevel(knockbackEntry, fabricPlayer.getMainHandStack());
    }

    @Override
    public @Nullable Integer getNoDamageTicks() {
        return fabricPlayer.timeUntilRegen;
    }

    @Override
    public void setVelocity(Vector3d adjustedVelocity) {
        fabricPlayer.setVelocity(adjustedVelocity.x, adjustedVelocity.y, adjustedVelocity.z);
        // TODO
        // fix paper-ism? for some reason setVelocity() in paper marks the entity as hurt marked every time its called?
        fabricPlayer.velocityModified = true;
    }

    @Override
    public Vector3d getVelocity() {
        final Vec3d fabricVelocity = fabricPlayer.getVelocity();
        return new Vector3d(fabricVelocity.x, fabricVelocity.y, fabricVelocity.z);
    }

    @Override
    public double getJumpPower() {
        double jumpVelocity = 0.42;
        StatusEffectInstance jumpEffect = fabricPlayer.getStatusEffect(StatusEffects.JUMP_BOOST);
        if (jumpEffect != null) {
            int amplifier = jumpEffect.getAmplifier();
            jumpVelocity += (amplifier + 1) * 0.1F;
        }

        return jumpVelocity;
    }
    @Override
    public BoundingBox getBoundingBox() {
        Box boundingBox = fabricPlayer.getBoundingBox();
        return new BoundingBox(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setClientBrand(String brand) {
        this.clientBrand = brand;
    }

    @Override
    public String getClientBrand() {
        return this.clientBrand;
    }
}
