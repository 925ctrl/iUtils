package io.github.iutils.bootstrap;

import io.github.iutils.common.config.ConfigManager;
import io.github.iutils.common.config.ConfigValidator;
import io.github.iutils.common.messaging.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IutilsCommandTest {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    @TempDir
    Path tempDir;

    private ConfigManager configManager;
    private IutilsCommand iutilsCommand;
    private Command dummyCommand;

    @BeforeEach
    void setUp() {
        Logger silentLogger = Logger.getLogger("IutilsCommandTest");
        silentLogger.setUseParentHandlers(false);
        configManager = new ConfigManager(
                tempDir,
                silentLogger,
                () -> getClass().getClassLoader().getResourceAsStream(ConfigManager.CONFIG_FILE_NAME),
                new ConfigValidator()
        );
        configManager.loadInitial();
        iutilsCommand = new IutilsCommand(configManager, new MessageFormatter());
        dummyCommand = new DummyCommand("iutils");
    }

    @Test
    @DisplayName("Should deny /iutils reload when sender does not have iutils.admin permission")
    void shouldDenyExecutionWithoutAdminPermission() throws IOException {
        writeUpdatedConfig(false, "Nuevo mensaje para {player}");
        List<Component> messages = new ArrayList<>();
        CommandSender unauthorizedSender = createSender(false, messages);

        boolean handled = iutilsCommand.onCommand(
                unauthorizedSender,
                dummyCommand,
                "iutils",
                new String[]{"reload"}
        );

        assertTrue(handled);
        assertEquals(1, messages.size());
        assertTrue(PLAIN.serialize(messages.getFirst()).contains("No tienes permiso"));
        assertTrue(
                configManager.getWelcomeSettings().enabled(),
                "Configuration must not be reloaded by an unauthorized sender"
        );
    }

    @Test
    @DisplayName("Should reload configuration and confirm in Spanish when sender has iutils.admin permission")
    void shouldReloadConfigWithAdminPermission() throws IOException {
        writeUpdatedConfig(false, "Mensaje actualizado {player}");
        List<Component> messages = new ArrayList<>();
        CommandSender adminSender = createSender(true, messages);

        boolean handled = iutilsCommand.onCommand(
                adminSender,
                dummyCommand,
                "iutils",
                new String[]{"reload"}
        );

        assertTrue(handled);
        assertEquals(1, messages.size());
        assertTrue(PLAIN.serialize(messages.getFirst()).contains("recargado y validado correctamente"));
        assertFalse(configManager.getWelcomeSettings().enabled());
        assertEquals("Mensaje actualizado {player}", configManager.getWelcomeSettings().messageTemplate());
    }

    @Test
    @DisplayName("Should show usage message when subcommand is missing or invalid")
    void shouldShowUsageOnInvalidArguments() {
        List<Component> messages = new ArrayList<>();
        CommandSender adminSender = createSender(true, messages);

        iutilsCommand.onCommand(adminSender, dummyCommand, "iutils", new String[]{});

        assertEquals(1, messages.size());
        assertTrue(PLAIN.serialize(messages.getFirst()).contains("/iutils reload"));
    }

    @Test
    @DisplayName("Should provide tab completion only for authorized administrators")
    void shouldProvideTabCompletionOnlyForAuthorizedAdmins() {
        CommandSender admin = createSender(true, new ArrayList<>());
        CommandSender player = createSender(false, new ArrayList<>());

        List<String> adminCompletions = iutilsCommand.onTabComplete(admin, dummyCommand, "iutils", new String[]{"re"});
        List<String> playerCompletions = iutilsCommand.onTabComplete(player, dummyCommand, "iutils", new String[]{"re"});

        assertEquals(List.of("reload"), adminCompletions);
        assertTrue(playerCompletions.isEmpty());
    }

    private void writeUpdatedConfig(boolean enabled, String message) throws IOException {
        String yaml = """
                general:
                  language: "es_ES"
                  prefix: "[Iutils] "
                  format: "MINIMESSAGE"
                welcome:
                  enabled: %s
                  use-prefix: true
                  message: "%s"
                block-refill:
                  enabled: true
                """.formatted(enabled, message);
        Files.writeString(tempDir.resolve(ConfigManager.CONFIG_FILE_NAME), yaml, StandardCharsets.UTF_8);
    }

    private CommandSender createSender(boolean hasAdminPermission, List<Component> capturedMessages) {
        return (CommandSender) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{CommandSender.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("hasPermission".equals(name) && args != null && args.length == 1) {
                        return hasAdminPermission && IutilsCommand.ADMIN_PERMISSION.equals(args[0]);
                    }
                    if ("sendMessage".equals(name) && args != null && args.length == 1 && args[0] instanceof Component c) {
                        capturedMessages.add(c);
                        return null;
                    }
                    if ("getName".equals(name)) {
                        return hasAdminPermission ? "AdminUser" : "RegularPlayer";
                    }
                    return null;
                }
        );
    }

    private static final class DummyCommand extends Command {
        DummyCommand(String name) {
            super(name);
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            return true;
        }
    }
}
