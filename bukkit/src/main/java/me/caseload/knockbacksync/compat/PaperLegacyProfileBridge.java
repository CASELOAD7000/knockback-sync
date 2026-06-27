package me.caseload.knockbacksync.compat;

import java.lang.reflect.Method;
import org.bukkit.entity.Player;

/**
 * Detects whether a player is on a LEGACY combat profile in a Paper fork that
 * carries the Carbon DETAILED knockback engine. KnockbackSync's clientside
 * recalculation conflicts with the Carbon-style knockback (it would smooth out
 * the carefully tuned vertical motion the legacy engine produced), so the
 * listeners short-circuit when this returns {@code true}.
 *
 * <p>The bridge is fully reflective so KnockbackSync continues to work on
 * vanilla Paper / Spigot / forks without the combat-profile API: when the
 * Paper-fork classes are not on the classpath, {@link #isLegacy(Player)} returns
 * {@code false} and KnockbackSync behaves exactly as before.
 */
public final class PaperLegacyProfileBridge {

    private static final boolean AVAILABLE;
    private static final Method PROFILE_OF;       // ProfileResolver.profileOf(net.minecraft.world.entity.Entity)
    private static final Method PROFILE_MODE;     // CombatProfile.mode()
    private static final Object LEGACY_MODE;      // CombatMode.LEGACY
    private static final Method GET_HANDLE;       // CraftEntity.getHandle()

    static {
        boolean available = false;
        Method profileOf = null;
        Method profileMode = null;
        Object legacyMode = null;
        Method getHandle = null;
        try {
            final Class<?> nmsEntity = Class.forName("net.minecraft.world.entity.Entity");
            final Class<?> resolver = Class.forName("io.papermc.paper.combat.engine.ProfileResolver");
            final Class<?> profile = Class.forName("io.papermc.paper.combat.CombatProfile");
            @SuppressWarnings({"rawtypes", "unchecked"})
            final Class<? extends Enum> modeClass = (Class<? extends Enum>) Class.forName("io.papermc.paper.combat.CombatMode");
            // CraftEntity lives in the obc.<version>.entity package on legacy servers and
            // org.bukkit.craftbukkit.entity on Paper 1.20.6+. Resolve dynamically so we
            // don't add a CraftBukkit compile dependency to the plugin.
            final Class<?> craftEntity = Class.forName("org.bukkit.craftbukkit.entity.CraftEntity");

            profileOf = resolver.getMethod("profileOf", nmsEntity);
            profileMode = profile.getMethod("mode");
            @SuppressWarnings("unchecked")
            final Object legacy = Enum.valueOf(modeClass, "LEGACY");
            legacyMode = legacy;
            getHandle = craftEntity.getMethod("getHandle");
            available = true;
        } catch (final Throwable ignored) {
            // Paper API not present — disable bridge cleanly.
        }
        AVAILABLE = available;
        PROFILE_OF = profileOf;
        PROFILE_MODE = profileMode;
        LEGACY_MODE = legacyMode;
        GET_HANDLE = getHandle;
    }

    private PaperLegacyProfileBridge() {
    }

    /**
     * @return {@code true} when the player is running on a Paper fork with the
     *         Carbon DETAILED combat engine AND their active combat profile is
     *         in LEGACY mode. Always {@code false} on platforms without the API.
     */
    public static boolean isLegacy(final Player player) {
        if (!AVAILABLE || player == null) {
            return false;
        }
        try {
            final Object handle = GET_HANDLE.invoke(player);
            final Object profile = PROFILE_OF.invoke(null, handle);
            if (profile == null) {
                return false;
            }
            return PROFILE_MODE.invoke(profile) == LEGACY_MODE;
        } catch (final Throwable ignored) {
            return false;
        }
    }
}
