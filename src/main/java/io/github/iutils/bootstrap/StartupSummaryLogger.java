package io.github.iutils.bootstrap;

import io.github.iutils.common.config.PluginConfig;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Formats and logs the plugin startup summary banner, including plugin metadata
 * (name, version, creator) and the activation status of each feature module.
 */
public final class StartupSummaryLogger {

    public static final String BORDER_LINE = "==================================================";
    public static final String STATUS_ACTIVE = "Activo";
    public static final String STATUS_INACTIVE = "Inactivo";
    public static final String DEFAULT_CREATOR = "925ctrl";

    private final Logger logger;

    public StartupSummaryLogger(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger cannot be null");
    }

    /**
     * Logs the formatted startup summary lines at {@code INFO} level.
     *
     * @param pluginName the plugin name
     * @param version    the plugin version string
     * @param authors    the list of plugin authors/creators
     * @param config     the active plugin configuration snapshot
     */
    public void logStartupSummary(
            String pluginName,
            String version,
            List<String> authors,
            PluginConfig config) {
        List<String> lines = formatSummaryLines(pluginName, version, authors, config);
        for (String line : lines) {
            logger.info(line);
        }
    }

    /**
     * Builds the immutable list of startup summary lines to be displayed on plugin
     * startup.
     *
     * @param pluginName the plugin name
     * @param version    the plugin version string
     * @param authors    the list of plugin authors/creators
     * @param config     the active plugin configuration snapshot
     * @return an immutable list of formatted summary lines in Spanish
     */
    public List<String> formatSummaryLines(
            String pluginName,
            String version,
            List<String> authors,
            PluginConfig config) {
        Objects.requireNonNull(pluginName, "pluginName cannot be null");
        Objects.requireNonNull(version, "version cannot be null");
        Objects.requireNonNull(authors, "authors cannot be null");
        Objects.requireNonNull(config, "config cannot be null");

        String creator = formatAuthors(authors);
        String welcomeStatus = formatStatus(config.welcome().enabled());
        String blockRefillStatus = formatStatus(config.blockRefill().enabled());

        String toolSwapStatus = formatStatus(config.toolSwap().enabled());

        return List.of(
                BORDER_LINE,
                "Plugin: " + pluginName + " v" + version,
                "Creador: " + creator,
                "Resumen de módulos:",
                " - Mensaje de bienvenida: " + welcomeStatus,
                " - BlockRefill (Recarga de bloques): " + blockRefillStatus,
                " - ToolSwap (Intercambio de herramientas): " + toolSwapStatus,
                BORDER_LINE);
    }

    private static String formatAuthors(List<String> authors) {
        String joined = authors.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(author -> !author.isEmpty())
                .collect(Collectors.joining(", "));
        return joined.isEmpty() ? DEFAULT_CREATOR : joined;
    }

    private static String formatStatus(boolean enabled) {
        return enabled ? STATUS_ACTIVE : STATUS_INACTIVE;
    }
}
