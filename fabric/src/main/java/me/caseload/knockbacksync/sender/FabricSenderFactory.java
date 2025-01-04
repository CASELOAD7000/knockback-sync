package me.caseload.knockbacksync.sender;

import me.caseload.knockbacksync.FabricBase;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.mixin.CommandStackSourceAccessor;
import me.caseload.knockbacksync.permission.FabricPermissionChecker;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.rcon.RconConsoleSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;

import java.util.UUID;

public class FabricSenderFactory extends SenderFactory<FabricBase, CommandSourceStack> implements SenderMapper<CommandSourceStack, Sender> {
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
    protected UUID getUniqueId(CommandSourceStack commandSource) {
        if (commandSource.getEntity() != null) {
            return commandSource.getEntity().getUUID();
        }
        return Sender.CONSOLE_UUID;
    }

    @Override
    protected String getName(CommandSourceStack commandSource) {
        String name = commandSource.getTextName();
        if (commandSource.getEntity() != null && name.equals("Server")) {
            return Sender.CONSOLE_NAME;
        }
        return name;
    }

    @Override
    protected void sendMessage(CommandSourceStack sender, String message) {
//        final Locale locale;
//        if (sender.getEntity() instanceof ServerPlayer) {
//            String language = ((ServerPlayer) sender.getEntity()).clientInformation().language();
//            locale = language == null ? null : TranslationManager.parseLocale(language);
//        } else {
//            locale = null;
//        }
//        sender.sendFeedback(() -> toNativeText(TranslationManager.render(message, locale)), false);
        sender.sendSuccess(() -> Component.literal(message), false);
    }

    @Override
    protected boolean hasPermission(CommandSourceStack commandSource, String node) {
        return ((FabricPermissionChecker) Base.INSTANCE.getPermissionChecker()).hasPermission(commandSource, node);
    }

    @Override
    protected boolean hasPermission(CommandSourceStack commandSource, String node, boolean defaultIfUnset) {
        return ((FabricPermissionChecker) Base.INSTANCE.getPermissionChecker()).hasPermission(commandSource, node, defaultIfUnset);
    }

    @Override
    protected void performCommand(CommandSourceStack sender, String command) {
//        sender.getServer().getCommandManager().executeWithPrefix(sender, command);
    }

    @Override
    protected boolean isConsole(CommandSourceStack sender) {
        CommandSource output = ((CommandStackSourceAccessor) sender).getSource();
        return output == sender.getServer() || // Console
                output.getClass() == RconConsoleSource.class || // Rcon
                (output == CommandSource.NULL && sender.getTextName().equals("")); // Functions
    }

    @Override
    public @NonNull Sender map(@NonNull CommandSourceStack base) {
        return this.wrap(base);
    }

    @Override
    public @NonNull CommandSourceStack reverse(@NonNull Sender mapped) {
        return this.unwrap(mapped);
    }

    @Override
    public void close() throws Exception {

    }

//    public static Text toNativeText(Component component) {
//        return Text.Serialization.fromJsonTree(GsonComponentSerializer.gson().serializeToTree(component), DynamicRegistryManager.EMPTY);
//    }
}
