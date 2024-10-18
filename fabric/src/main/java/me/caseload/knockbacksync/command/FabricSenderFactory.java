package me.caseload.knockbacksync.command;

import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.mixin.CommandStackSourceAccessor;
import me.caseload.knockbacksync.permission.FabricPermissionChecker;
import me.caseload.knockbacksync.world.KBSyncFabricBase;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.rcon.RconConsoleSource;

import java.util.UUID;

public class FabricSenderFactory extends SenderFactory<KBSyncFabricBase, CommandSourceStack> {
    private final KBSyncFabricBase plugin;

    public FabricSenderFactory(KBSyncFabricBase kbSyncFabricBase) {
        super(kbSyncFabricBase);
        this.plugin = kbSyncFabricBase;
    }

    @Override
    protected KBSyncFabricBase getPlugin() {
        return this.plugin;
    }

    @Override
    protected UUID getUniqueId(CommandSourceStack commandSource) {
        if (commandSource.getEntity() != null) {
            return commandSource.getEntity().getUUID();
        }
        return PlatformSender.CONSOLE_UUID;
    }

    @Override
    protected String getName(CommandSourceStack commandSource) {
        String name = commandSource.getTextName();
        if (commandSource.getEntity() != null && name.equals("Server")) {
            return PlatformSender.CONSOLE_NAME;
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
        return ((FabricPermissionChecker) KnockbackSyncBase.INSTANCE.getPermissionChecker()).hasPermission(commandSource, node);
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

//    public static Text toNativeText(Component component) {
//        return Text.Serialization.fromJsonTree(GsonComponentSerializer.gson().serializeToTree(component), DynamicRegistryManager.EMPTY);
//    }
}
