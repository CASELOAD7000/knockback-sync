package me.caseload.knockbacksync.util;

public class MathUtil {

    private static final double TERMINAL_VELOCITY = 3.92;
    private static final double MULTIPLIER = 0.98;

    public static double calculateDistanceTraveled(double velocity, int time, double acceleration)
    {
        double totalDistance = 0;

        for (int i = 0; i < time; i++)
        {
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
            velocity += acceleration;
            velocity = Math.min(velocity, TERMINAL_VELOCITY);
            velocity *= MULTIPLIER;
            distance -= velocity;
            ticks++;
        }

        return ticks;
    }

    public static int calculateTimeToMaxVelocity(double targetVerticalVelocity, double acceleration) {
        double a = -acceleration * MULTIPLIER;
        double b = acceleration + TERMINAL_VELOCITY * MULTIPLIER;
        double c = -2 * targetVerticalVelocity;

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0)
            return 0;

        double positiveRoot = (-b + Math.sqrt(discriminant)) / (2 * a);
        return (int) Math.ceil(positiveRoot * 20);
    }

    public static double clamp(double num, double min, double max) {
        if (num < min) {
            return min;
        }
        return Math.min(num, max);
    }
}