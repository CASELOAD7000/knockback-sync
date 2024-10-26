package me.caseload.knockbacksync.util;

import static me.caseload.knockbacksync.player.PlayerData.TICK_RATE;

public class MathUtil {

    private static final double TERMINAL_VELOCITY = 3.92;
    private static final double MULTIPLIER = 0.98;
    private static final int MAX_TICKS = 30;

    public static double getCompensatedVerticalVelocity(double verticalVelocity, double ping) {
        final double gravity = 0.08;
        final double multiplier = 0.98;

        int ticks = (int) Math.ceil(ping * TICK_RATE / 1000);
        while(ticks > 0) {
            verticalVelocity -= gravity;
            verticalVelocity *= multiplier;
            ticks--;
        }
        return verticalVelocity;
    }

    public static double calculateDistanceTraveled(double velocity, int time, double acceleration) {
        double totalDistance = 0;

        for (int i = 0; i < time; i++) {
            totalDistance += velocity;
            velocity = ((velocity - acceleration) * MULTIPLIER);
            velocity = Math.min(velocity, TERMINAL_VELOCITY);
        }

        return totalDistance;
    }

    public static int calculateFallTime(double initialVelocity, double distance, double acceleration) {
        double velocity = Math.abs(initialVelocity);
        int ticks = 0;

        while (distance > 0) {
            if (ticks > MAX_TICKS)
                return -1;

            velocity += acceleration;
            velocity = Math.min(velocity, TERMINAL_VELOCITY);
            velocity *= MULTIPLIER;
            distance -= velocity;
            ticks++;
        }

        return ticks;
    }

    public static int calculateTimeToMaxVelocity(double velocity, double acceleration) {
        int ticks = 0;

        while (velocity > 0) {
            if (ticks > MAX_TICKS)
                return -1;

            velocity -= acceleration;
            velocity = Math.min(velocity, TERMINAL_VELOCITY);
            velocity *= MULTIPLIER;
            ticks++;
        }

        return ticks;
    }

    public static double clamp(double num, double min, double max) {
        if (num < min)
            return min;

        return Math.min(num, max);
    }
}