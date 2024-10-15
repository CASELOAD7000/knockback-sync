package me.caseload.knockbacksync.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public class ChatUtil {
    public static @NotNull String translateAlternateColorCodes(char altColorChar, @NotNull String textToTranslate) {
        Preconditions.checkArgument(textToTranslate != null, "Cannot translate null text");
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }
}
