package me.caseload.knockbacksync.stats;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.KnockbackSyncPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

public class BuildTypePie extends SimplePie {

    private static final String RELEASES_FILE = "releases.txt";
    private static final String DEV_BUILDS_FILE = "dev-builds.txt";
    private static final File dataFolder = KnockbackSyncBase.INSTANCE.getDataFolder();
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
            String currentHash = getPluginJarHash();
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
        GHRelease latestRelease = gitHub.getRepository("Axionize/knockback-sync")
                .getLatestRelease();
        List<GHAsset> assets = latestRelease.listAssets().toList();
        for (GHAsset asset : assets) {
            if (asset.getName().equals(RELEASES_FILE) || asset.getName().equals(DEV_BUILDS_FILE)) {
                KnockbackSyncBase.INSTANCE.getLogger().info("Downloading: " + asset.getName());

                String jsonContent = readStringFromURL(asset.getUrl().toString());
                JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();
                String downloadUrl = jsonObject.get("browser_download_url").getAsString();

                try (InputStream inputStream = new URL(downloadUrl).openStream();
                     FileOutputStream outputStream = new FileOutputStream(new File(dataFolder, asset.getName()))) {
                    inputStream.transferTo(outputStream);
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

    private static String getPluginJarHash() throws Exception {
        URL jarUrl = null;
        switch (KnockbackSyncBase.INSTANCE.platform) {
            case BUKKIT:
            case FOLIA:
                // Will give path to remapped jar on paper forks, actual jar on Spigot
                jarUrl = Bukkit.getPluginManager().getPlugin("KnockbackSync").getClass().getProtectionDomain().getCodeSource().getLocation();
                break;
            case FABRIC:
                Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer("knockbacksync");
                if (modContainer.isPresent()) {
                    String jarPath = modContainer.get().getRootPath().getFileSystem().toString();
//                    jarPath = jarPath.replaceAll("^jar:", "").replaceAll("!/$", "");
                    jarUrl = new File(jarPath).toURI().toURL();
                }
                break;
        }
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

    private static String readStringFromURL(String urlString) throws IOException {
        try (InputStream inputStream = new URL(urlString).openStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
