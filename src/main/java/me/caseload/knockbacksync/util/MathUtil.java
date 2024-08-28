package me.caseload.knockbacksync.util;

public class MathUtil {

    public static int getFallTime(double initialVelocity, double distance) {
        double terminalVelocity = 3.92;
        double gravity = 0.08;
        double multiplier = 0.98;

        double velocity = Math.abs(initialVelocity);
        int ticks = 0;

        while (distance > 0) {
            velocity += gravity;
            velocity = Math.min(velocity, terminalVelocity);
            velocity *= multiplier;
            distance -= velocity;
            ticks++;
        }

        return ticks;
    }

    public static double getTimeToMaxUpwardSpeed(double targetVerticalVelocity) {
        double terminalVelocity = 3.92;
        double gravity = 0.08;
        double multiplier = 0.98;

        double a = -gravity * multiplier;
        double b = gravity + terminalVelocity * multiplier;
        double c = -2 * targetVerticalVelocity;

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0)
            return 0;

        double positiveRoot = (-b + Math.sqrt(discriminant)) / (2 * a);
        return Math.ceil(positiveRoot * 20);
    }

    public static double getMaxHeight(double targetVerticalVelocity) {
        return targetVerticalVelocity * 2.484875;
    }
}