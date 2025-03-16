package me.caseload.knockbacksync.listener.packetevents;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.event.UserLoginEvent;
import com.google.common.base.Preconditions;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.caseload.knockbacksync.player.PlayerData;
import me.caseload.knockbacksync.util.ChatUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PacketPlayerJoinQuit extends PacketListenerAbstract {
    @Override
    public void onUserLogin(UserLoginEvent event) {
        Object nativePlayerObject = event.getPlayer();
        Preconditions.checkArgument(nativePlayerObject != null);

        @NotNull PlatformPlayer platformPlayer = Base.INSTANCE.getPlatformServer().getPlayer(nativePlayerObject);
        onPlayerJoin(new PlayerData(platformPlayer));
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        onPlayerQuit(event.getUser().getUUID());
    }

    public void onPlayerJoin(PlayerData player) {
        PlayerDataManager.addPlayerData(player.getUuid(), player);
        PlatformPlayer platformPlayer = player.getPlatformPlayer();

        if (Base.INSTANCE.getConfigManager().isUpdateAvailable() && Base.INSTANCE.getConfigManager().isNotifyUpdate() && Base.INSTANCE.getPermissionChecker().hasPermission(platformPlayer, "knockbacksync.update"))
            platformPlayer.sendMessage(ChatUtil.translateAlternateColorCodes(
                    '&',
                    "&6An updated version of &eKnockbackSync &6is now available for download at: &bhttps://github.com/CASELOAD7000/knockback-sync/releases/latest"
            ));
    }

    public void onPlayerQuit(UUID uuid) {
        PlayerData playerData = PlayerDataManager.getPlayerData(uuid);
        if (playerData == null)
            return;

        playerData.quitCombat(true);
        PlayerDataManager.removePlayerData(uuid);
    }
}
