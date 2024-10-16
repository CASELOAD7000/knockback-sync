package me.caseload.knockbacksync.player;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

public class JitterCalculator {
    private final int SAMPLE_SIZE = 15;
    private final Queue<Long> pings = new LinkedList<>();
    @Getter
    private long sequenceNumber = 0;

    public void addPing(long pingTime, long receivedSequence) {
        pings.offer(pingTime);
        if (pings.size() > SAMPLE_SIZE) {
            pings.poll();
        }

        // Check for out-of-order packets
        if (receivedSequence < sequenceNumber) {
            // Handle out-of-order packet
        }
        sequenceNumber = receivedSequence;
    }

    public double calculateJitter() {
        if (pings.size() < 2) return 0;

        List<Long> sortedPings = new ArrayList<>(pings);
        Collections.sort(sortedPings);

        // Calculate IQR
        int q1Index = sortedPings.size() / 4;
        int q3Index = q1Index * 3;
        long q1 = sortedPings.get(q1Index);
        long q3 = sortedPings.get(q3Index);
        long iqr = q3 - q1;

        // Filter outliers
        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;
        List<Long> filteredPings = sortedPings.stream()
                .filter(p -> p >= lowerBound && p <= upperBound)
                .toList();

        // Calculate standard deviation
        double mean = filteredPings.stream().mapToLong(Long::longValue).average().orElse(0);
        double variance = filteredPings.stream()
                .mapToDouble(p -> Math.pow(p - mean, 2))
                .average().orElse(0);
        double stdDev = Math.sqrt(variance);

        // Calculate mean jitter
        double meanJitter = 0;
        Long prevPing = null;
        for (Long ping : filteredPings) {
            if (prevPing != null) {
                meanJitter += Math.abs(ping - prevPing);
            }
            prevPing = ping;
        }
        meanJitter /= (filteredPings.size() - 1);

        // You can return different jitter metrics based on your needs
        return stdDev; // or meanJitter, or both
    }
}
