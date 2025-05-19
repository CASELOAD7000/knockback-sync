package me.caseload.knockbacksync.sender;

import me.caseload.knockbacksync.FabricBase;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.permission.FabricPermissionChecker;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.rcon.RconCommandOutput;
import net.minecraft.text.Text;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;

import java.util.UUID;

public class FabricSenderFactory extends SenderFactory<FabricBase, ServerCommandSource> implements SenderMapper<ServerCommandSource, Sender> {
    private final FabricBase plugin;

    public FabricSenderFactory(FabricBase kbSyncFabricBase) {
        super(kbSyncFabricBase);
        this.plugin = kbSyncFabricBase;
    }

    @Override
    protected FabricBase getPlugin() {
        return this.plugin;
    }

    @Override
    protected UUID getUniqueId(ServerCommandSource commandSource) {
        if (commandSource.getEntity() != null) {
            return commandSource.getEntity().getUuid();
        }
        return Sender.CONSOLE_UUID;
    }

    @Override
    protected String getName(ServerCommandSource commandSource) {
        String name = commandSource.getName();
        if (commandSource.getEntity() != null && name.equals("Server")) {
            return Sender.CONSOLE_NAME;
        }
        return name;
    }

    @Override
    protected void sendMessage(ServerCommandSource sender, String message) {
//        final Locale locale;
//        if (sender.getEntity() instanceof ServerPlayer) {
//            String language = ((ServerPlayer) sender.getEntity()).clientInformation().language();
//            locale = language == null ? null : TranslationManager.parseLocale(language);
//        } else {
//            locale = null;
//        }
//        sender.sendFeedback(() -> toNativeText(TranslationManager.render(message, locale)), false);
        sender.sendFeedback(() -> Text.literal(message), false);
    }

    @Override
    protected boolean hasPermission(ServerCommandSource commandSource, String node) {
        return ((FabricPermissionChecker) Base.INSTANCE.getPermissionChecker()).hasPermission(commandSource, node);
    }

    @Override
    protected boolean hasPermission(ServerCommandSource commandSource, String node, boolean defaultIfUnset) {
        return ((FabricPermissionChecker) Base.INSTANCE.getPermissionChecker()).hasPermission(commandSource, node, defaultIfUnset);
    }

    @Override
    protected void performCommand(ServerCommandSource sender, String command) {
//        sender.getServer().getCommandManager().executeWithPrefix(sender, command);
    }

    @Override
    protected boolean isConsole(ServerCommandSource sender) {
        CommandOutput output = sender.output;
        return output == sender.getServer() || // Console
                output.getClass() == RconCommandOutput.class || // Rcon
                (output == CommandOutput.DUMMY && sender.getName().equals("")); // Functions
    }

    @Override
    public @NonNull Sender map(@NonNull ServerCommandSource base) {
        return this.wrap(base);
    }

    @Override
    public @NonNull ServerCommandSource reverse(@NonNull Sender mapped) {
        return this.unwrap(mapped);
    }

    @Override
    public void close() throws Exception {

    }

//    public static Text toNativeText(Component component) {
//        return Text.Serialization.fromJsonTree(GsonComponentSerializer.gson().serializeToTree(component), DynamicRegistryManager.EMPTY);
//    }
}
