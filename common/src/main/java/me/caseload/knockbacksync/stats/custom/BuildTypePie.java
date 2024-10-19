package me.caseload.knockbacksync.stats.custom;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.stats.SimplePie;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GitHub;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class BuildTypePie extends SimplePie {

    private static final String RELEASES_FILE = "releases.txt";
    private static final String DEV_BUILDS_FILE = "dev-builds.txt";
    private static final File dataFolder = KnockbackSyncBase.INSTANCE.getDataFolder();
    public static URL jarUrl;
    private static String cachedBuildType = null;

    public BuildTypePie() {
        super("build_type", BuildTypePie::determineBuildType);
    }

    public static String determineBuildType() {
        if (cachedBuildType == null) {
            cachedBuildType = calculateBuildType();
        }
        return cachedBuildType;
    }

    private static String calculateBuildType() {
        try {
            String currentHash = KnockbackSyncBase.INSTANCE.getPluginJarHashProvider().getPluginJarHash();
            downloadBuildFiles();

            if (isHashInFile(currentHash, new File(dataFolder, RELEASES_FILE))) {
                return "release";
            } else if (isHashInFile(currentHash, new File(dataFolder, DEV_BUILDS_FILE))) {
                return "dev";
            } else {
                return "fork";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    private static void downloadBuildFiles() throws IOException {
        GitHub gitHub = GitHub.connectAnonymously();
        GHRelease latestRelease = gitHub.getRepository("CASELOAD7000/knockback-sync")
                .getLatestRelease();
        List<GHAsset> assets = latestRelease.listAssets().toList();
        for (GHAsset asset : assets) {
            if (asset.getName().equals(RELEASES_FILE) || asset.getName().equals(DEV_BUILDS_FILE)) {
                KnockbackSyncBase.INSTANCE.getLogger().info("Downloading: " + asset.getName());

                String jsonContent = readStringFromURL(asset.getUrl().toString());
                JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();
                String downloadUrl = jsonObject.get("browser_download_url").getAsString();

                try (InputStream inputStream = new URL(downloadUrl).openStream(); FileOutputStream outputStream = new FileOutputStream(new File(dataFolder, asset.getName()))) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                KnockbackSyncBase.INSTANCE.getLogger().info("Downloaded: " + asset.getName());
            }
        }
    }

    private static boolean isHashInFile(String hash, File file) throws IOException {
        if (!file.exists()) {
            return false;
        }
        List<String> lines = Files.readAllLines(Paths.get(file.getPath()));
        return lines.contains(hash);
    }

    private static String readStringFromURL(String urlString) throws IOException {
        try (InputStream inputStream = new URL(urlString).openStream()) {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, bytesRead);
            }
            return result.toString("UTF-8");
        }
    }
}
