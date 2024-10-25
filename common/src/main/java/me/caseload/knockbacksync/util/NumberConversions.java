package me.caseload.knockbacksync.util;

import org.jetbrains.annotations.Nullable;

public class NumberConversions {
    public static long toLong(@Nullable Object object, long defaultValue) {
        if (object instanceof Number) {
            return ((Number) object).longValue();
        } else {
            try {
                return Long.parseLong(object.toString());
            } catch (NumberFormatException | NullPointerException ignored) {
            }

            return defaultValue;
        }
    }

    public static int toInt(@Nullable Object object, int def) {
        if (object instanceof Number) {
            return ((Number) object).intValue();
        } else {
            try {
                return Integer.parseInt(object.toString());
            } catch (NumberFormatException | NullPointerException ignored) {
            }

            return def;
        }
    }
}
