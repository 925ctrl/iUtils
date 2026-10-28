package io.github.iutils.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigManagerTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should create default config.yml on initial load and validate successfully")
    void shouldCreateDefaultConfigOnInitialLoad() {
        ConfigManager manager = createConfigManager();

        ConfigValidationResult result = manager.loadInitial();

        assertTrue(result.isValid());
        assertTrue(Files.exists(tempDir.resolve(ConfigManager.CONFIG_FILE_NAME)));
        assertTrue(manager.getWelcomeSettings().enabled());
        assertTrue(manager.getBlockRefillSettings().enabled());
        assertEquals(MessageFormatMode.MINIMESSAGE, manager.getConfig().formatMode());
    }

    @Test
    @DisplayName("Should apply new configuration values upon valid reload")
    void shouldApplyUpdatedValuesOnValidReload() throws IOException {
        ConfigManager manager = createConfigManager();
        manager.loadInitial();

        String customYaml = """
                general:
                  language: "es_ES"
                  prefix: "&6[MiServidor] "
                  format: "LEGACY"
                welcome:
                  enabled: true
                  use-prefix: false
                  message: "&aBienvenido al servidor, &e{player}&a!"
                block-refill:
                  enabled: false
                """;
        Files.writeString(tempDir.resolve(ConfigManager.CONFIG_FILE_NAME), customYaml, StandardCharsets.UTF_8);

        ConfigValidationResult reloadResult = manager.reload();

        assertTrue(reloadResult.isValid());
        assertEquals("&6[MiServidor] ", manager.getConfig().prefix());
        assertEquals(MessageFormatMode.LEGACY, manager.getConfig().formatMode());
        assertFalse(manager.getWelcomeSettings().usePrefix());
        assertEquals("&aBienvenido al servidor, &e{player}&a!", manager.getWelcomeSettings().messageTemplate());
        assertFalse(manager.getBlockRefillSettings().enabled());
    }

    @Test
    @DisplayName("Should reject invalid reload and keep previous valid configuration active")
    void shouldRejectInvalidReloadAndKeepPreviousValidConfig() throws IOException {
        ConfigManager manager = createConfigManager();
        manager.loadInitial();
        String initialTemplate = manager.getWelcomeSettings().messageTemplate();

        String invalidYaml = """
                general:
                  language: "es_ES"
                  prefix: "[Iutils] "
                  format: "FORMATO_INVALIDO"
                welcome:
                  enabled: "no_es_booleano"
                  use-prefix: true
                  message: ""
                block-refill:
                  enabled: "invalido"
                """;
        Files.writeString(tempDir.resolve(ConfigManager.CONFIG_FILE_NAME), invalidYaml, StandardCharsets.UTF_8);

        ConfigValidationResult reloadResult = manager.reload();

        assertFalse(reloadResult.isValid());
        assertFalse(reloadResult.errors().isEmpty());
        assertEquals(
                initialTemplate,
                manager.getWelcomeSettings().messageTemplate(),
                "Previous valid configuration must remain active when reload fails validation"
        );
        assertTrue(manager.getBlockRefillSettings().enabled());
        assertEquals(MessageFormatMode.MINIMESSAGE, manager.getConfig().formatMode());
    }

    private ConfigManager createConfigManager() {
        Logger silentLogger = Logger.getLogger("ConfigManagerTest");
        silentLogger.setUseParentHandlers(false);
        return new ConfigManager(
                tempDir,
                silentLogger,
                () -> getClass().getClassLoader().getResourceAsStream(ConfigManager.CONFIG_FILE_NAME),
                new ConfigValidator()
        );
    }
}
