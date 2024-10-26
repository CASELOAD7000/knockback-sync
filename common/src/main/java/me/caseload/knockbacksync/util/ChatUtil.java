package me.caseload.knockbacksync.util;

import com.google.common.base.Preconditions;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlayerData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChatUtil {
    public static @NotNull String translateAlternateColorCodes(char altColorChar, @NotNull String textToTranslate) {
        Preconditions.checkArgument(textToTranslate != null, "Cannot translate null text");
        char[] b = textToTranslate.toCharArray();

        for (int i = 0; i < b.length - 1; ++i) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    public static String getPingMessage(UUID senderUUID, UUID targetUUID) {

        String noun = "";
        PlayerData playerData = null;
        if (senderUUID == null) {
            senderUUID = targetUUID;
        }

        if (senderUUID.equals(targetUUID) || targetUUID == null) {
            playerData = PlayerDataManager.getPlayerData(senderUUID);
            if (playerData == null) {
                return ChatUtil.translateAlternateColorCodes('&', "&cKnockback synchronization is currently disabled for you!");
            }
            noun = "Your";
        } else {
            playerData = PlayerDataManager.getPlayerData(targetUUID);
            if (playerData == null) {
                return ChatUtil.translateAlternateColorCodes('&', "&cKnockback synchronization is currently disabled for the target player.");
            }
            noun = playerData.getPlatformPlayer().getName() + "'s";
        }

        String rawReturnString = null;
        if (playerData.getPing() == null) {
            rawReturnString = String.format("Accurate ping unavailable. %s estimated ping is &b" + playerData.getPlatformPlayer().getPing() + "&rms.", noun);
        } else {
            rawReturnString = String.format("%s real ping is &b%.3f&rms. Jitter: &b%.3f&rms. Spike: &b%s&r. Compensated ping: &b%.3f&rms.", noun, playerData.getPing(), playerData.getJitter(), playerData.isSpike(), playerData.getCompensatedPing());
        }
        return ChatUtil.translateAlternateColorCodes('&', rawReturnString);
    }
}
