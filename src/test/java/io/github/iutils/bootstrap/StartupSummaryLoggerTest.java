package io.github.iutils.bootstrap;

import io.github.iutils.blockrefill.domain.BlockRefillSettings;
import io.github.iutils.common.config.MessageFormatMode;
import io.github.iutils.common.config.PluginConfig;
import io.github.iutils.welcome.domain.WelcomeSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StartupSummaryLoggerTest {

    private List<String> capturedLogs;
    private StartupSummaryLogger startupSummaryLogger;

    @BeforeEach
    void setUp() {
        capturedLogs = new ArrayList<>();
        Logger testLogger = Logger.getLogger("StartupSummaryLoggerTest");
        testLogger.setUseParentHandlers(false);
        for (Handler handler : testLogger.getHandlers()) {
            testLogger.removeHandler(handler);
        }
        testLogger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                capturedLogs.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
        startupSummaryLogger = new StartupSummaryLogger(testLogger);
    }

    @Test
    @DisplayName("Should log plugin name, version, creator and active modules when all features are enabled")
    void shouldLogSummaryWithAllModulesActive() {
        PluginConfig config = new PluginConfig(
                "es_ES",
                "[Iutils] ",
                MessageFormatMode.MINIMESSAGE,
                new WelcomeSettings(true, true, "Bienvenido {player}"),
                new BlockRefillSettings(true)
        );

        startupSummaryLogger.logStartupSummary("Iutils", "1.2.0", List.of("Iutils"), config);

        assertEquals(8, capturedLogs.size());
        assertTrue(capturedLogs.contains("Plugin: Iutils v1.2.0"));
        assertTrue(capturedLogs.contains("Creador: Iutils"));
        assertTrue(capturedLogs.contains(" - Mensaje de bienvenida: Activo"));
        assertTrue(capturedLogs.contains(" - BlockRefill (Recarga de bloques): Activo"));
    }

    @Test
    @DisplayName("Should report modules as Inactivo when disabled in PluginConfig")
    void shouldReportModulesAsInactiveWhenDisabled() {
        PluginConfig config = new PluginConfig(
                "es_ES",
                "[Iutils] ",
                MessageFormatMode.MINIMESSAGE,
                new WelcomeSettings(false, true, "Bienvenido {player}"),
                new BlockRefillSettings(false)
        );

        List<String> lines = startupSummaryLogger.formatSummaryLines(
                "Iutils",
                "1.2.0",
                List.of("Autor1", "Autor2"),
                config
        );

        assertTrue(lines.contains("Creador: Autor1, Autor2"));
        assertTrue(lines.contains(" - Mensaje de bienvenida: Inactivo"));
        assertTrue(lines.contains(" - BlockRefill (Recarga de bloques): Inactivo"));
    }

    @Test
    @DisplayName("Should use default creator fallback when authors list is empty")
    void shouldUseDefaultCreatorWhenAuthorsListIsEmpty() {
        List<String> lines = startupSummaryLogger.formatSummaryLines(
                "Iutils",
                "1.2.0",
                List.of(),
                PluginConfig.defaults()
        );

        assertTrue(lines.contains("Creador: " + StartupSummaryLogger.DEFAULT_CREATOR));
    }
}
