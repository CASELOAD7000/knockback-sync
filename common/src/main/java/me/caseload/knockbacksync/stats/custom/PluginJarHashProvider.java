package me.caseload.knockbacksync.stats.custom;

import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;

public class PluginJarHashProvider {

    protected URL jarURL;

    public PluginJarHashProvider(URL jarURL) {
        this.jarURL = jarURL;
    }

    public String getPluginJarHash() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = jarURL.openStream()) {
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
}