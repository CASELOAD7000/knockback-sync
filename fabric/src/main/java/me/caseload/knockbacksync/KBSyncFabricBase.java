package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.fabric.FabricPacketEventsBuilder;
import lombok.Getter;
import me.caseload.knockbacksync.listener.fabric.FabricPlayerDamageListener;
import me.caseload.knockbacksync.listener.fabric.FabricPlayerJoinQuitListener;
import me.caseload.knockbacksync.listener.fabric.FabricPlayerKnockbackListener;
import me.caseload.knockbacksync.listener.fabric.FabricTickRateChangeListener;
import me.caseload.knockbacksync.permission.FabricPermissionChecker;
import me.caseload.knockbacksync.permission.PermissionChecker;
import me.caseload.knockbacksync.scheduler.FabricSchedulerAdapter;
import me.caseload.knockbacksync.sender.FabricPlayerSelectorParser;
import me.caseload.knockbacksync.sender.FabricSenderFactory;
import me.caseload.knockbacksync.stats.custom.FabricStatsManager;
import me.caseload.knockbacksync.stats.custom.PluginJarHashProvider;
import me.caseload.knockbacksync.world.FabricServer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import java.util.logging.Logger;

public class KBSyncFabricBase extends KnockbackSyncBase {

    private final Logger logger = Logger.getLogger(KBSyncFabricLoaderMod.class.getName());
    private final FabricPermissionChecker permissionChecker = new FabricPermissionChecker();
    @Getter
    private final FabricSenderFactory fabricSenderFactory = new FabricSenderFactory(this);

    public KBSyncFabricBase() {
        super.playerSelectorParser = new FabricPlayerSelectorParser<>();
        super.commandManager = new FabricServerCommandManager<>(
                ExecutionCoordinator.simpleCoordinator(),
                fabricSenderFactory
        );
        super.statsManager = new FabricStatsManager();
        super.platformServer = new FabricServer();
        super.pluginJarHashProvider = new PluginJarHashProvider(getJarURL());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public File getDataFolder() {
        return FabricLoader.getInstance().getConfigDir().toFile();
    }

    @Override
    public InputStream getResource(String filename) {
        return getClass().getResourceAsStream("/config.yml");
    }

    @Override
    public void load() {
        PacketEvents.setAPI(FabricPacketEventsBuilder.build("knockbacksync"));
        PacketEvents.getAPI().load();
    }

    @Override
    public void initializeScheduler() {
        scheduler = new FabricSchedulerAdapter();
    }

    @Override
    protected void registerPlatformListeners() {
        new FabricPlayerJoinQuitListener().register();
        new FabricPlayerDamageListener().register();
        new FabricPlayerKnockbackListener().register();
        new FabricTickRateChangeListener().register();
    }

    @Override
    protected String getVersion() {
        return FabricLoader.getInstance().getModContainer("knockbacksync")
                .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    @Override
    public void saveDefaultConfig() {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream inputStream = getClass().getResourceAsStream("/config.yml")) {
                if (inputStream != null) {
                    Files.copy(inputStream, configFile.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public PermissionChecker getPermissionChecker() {
        return permissionChecker;
    }

    private URL getJarURL() {
        URL jarUrl = null;
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer("knockbacksync");
        if (modContainer.isPresent()) {
            String jarPath = modContainer.get().getRootPath().getFileSystem().toString();
            jarPath = jarPath.replaceAll("^jar:", "").replaceAll("!/$", "");
            try {
                jarUrl = new File(jarPath).toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return jarUrl;
    }
}
