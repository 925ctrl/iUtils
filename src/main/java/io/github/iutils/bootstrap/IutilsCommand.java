package io.github.iutils.bootstrap;

import io.github.iutils.common.config.ConfigManager;
import io.github.iutils.common.config.ConfigValidationResult;
import io.github.iutils.common.config.MessageFormatMode;
import io.github.iutils.common.config.PluginConfig;
import io.github.iutils.common.messaging.MessageFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Handles the administrative command {@code /iutils} and its {@code reload} subcommand.
 */
public final class IutilsCommand implements CommandExecutor, TabCompleter {

    public static final String ADMIN_PERMISSION = "iutils.admin";
    public static final String SUBCOMMAND_RELOAD = "reload";

    private static final String MSG_NO_PERMISSION =
            "<red>No tienes permiso para ejecutar este comando.</red>";
    private static final String MSG_USAGE =
            "<yellow>Uso correcto: <white>/iutils reload</white></yellow>";
    private static final String MSG_RELOAD_SUCCESS =
            "<green>La configuración de Iutils se ha recargado y validado correctamente.</green>";
    private static final String MSG_RELOAD_FAILED_HEADER =
            "<red>Se encontraron errores al validar config.yml. Se mantuvo la configuración anterior:</red>";

    private final ConfigManager configManager;
    private final MessageFormatter messageFormatter;

    public IutilsCommand(ConfigManager configManager, MessageFormatter messageFormatter) {
        this.configManager = Objects.requireNonNull(configManager, "configManager cannot be null");
        this.messageFormatter = Objects.requireNonNull(messageFormatter, "messageFormatter cannot be null");
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sendAdminMessage(sender, MSG_NO_PERMISSION);
            return true;
        }

        if (args.length != 1 || !SUBCOMMAND_RELOAD.equalsIgnoreCase(args[0])) {
            sendAdminMessage(sender, MSG_USAGE);
            return true;
        }

        ConfigValidationResult result = configManager.reload();
        if (result.isValid()) {
            sendAdminMessage(sender, MSG_RELOAD_SUCCESS);
        } else {
            sendAdminMessage(sender, MSG_RELOAD_FAILED_HEADER);
            for (String error : result.errors()) {
                sender.sendMessage(messageFormatter.format(
                        "<gray> - </gray><red>" + error + "</red>",
                        MessageFormatMode.MINIMESSAGE,
                        false,
                        ""
                ));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);
            if (SUBCOMMAND_RELOAD.startsWith(partial)) {
                return List.of(SUBCOMMAND_RELOAD);
            }
        }
        return Collections.emptyList();
    }

    private void sendAdminMessage(CommandSender sender, String miniMessageText) {
        PluginConfig config = configManager.getConfig();
        sender.sendMessage(messageFormatter.format(
                miniMessageText,
                MessageFormatMode.MINIMESSAGE,
                true,
                config.prefix()
        ));
    }
}
