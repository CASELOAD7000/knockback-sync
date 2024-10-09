package me.caseload.knockbacksync.stats;

import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;

import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Scanner;

public class BuildTypePie extends SimplePie {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/Axionize/knockback-sync/releases/latest";
    private static final String RELEASES_FILE = "releases.txt";
    private static final String DEV_BUILDS_FILE = "dev-builds.txt";

    public BuildTypePie() {
        super("build_type", BuildTypePie::determineBuildType);
    }

    private static String determineBuildType() {
        try {
            String currentHash = getPluginJarHash();
            System.out.println("Current Hash: " + currentHash);
            String latestReleaseUrl = getLatestReleaseUrl();

            if (isHashInFile(currentHash, latestReleaseUrl + RELEASES_FILE)) {
                return "release";
            } else if (isHashInFile(currentHash, latestReleaseUrl + DEV_BUILDS_FILE)) {
                return "dev";
            } else {
                return "fork";
            }
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String getLatestReleaseUrl() throws Exception {
        URL url = new URL(GITHUB_API_URL);
        try (Scanner scanner = new Scanner(url.openStream())) {
            String response = scanner.useDelimiter("\\A").next();
            // Simple parsing, you might want to use a JSON library for more robust parsing
            int index = response.indexOf("\"browser_download_url\"");
            if (index != -1) {
                int start = response.indexOf("\"", index + 23) + 1;
                int end = response.indexOf("\"", start);
                return response.substring(start, end);
            }
        }
        throw new Exception("Could not find latest release URL");
    }

    private static String getPluginJarHash() throws Exception {
        URL jarUrl = Bukkit.getPluginManager().getPlugin("KnockbackSync").getClass().getProtectionDomain().getCodeSource().getLocation();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = jarUrl.openStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }


    private static boolean isHashInFile(String hash, String fileUrl) throws Exception {
        URL url = new URL(fileUrl);
        try (Scanner scanner = new Scanner(url.openStream())) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith(hash)) {
                    return true;
                }
            }
        }
        return false;
    }
}
