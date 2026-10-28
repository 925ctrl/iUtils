package io.github.iutils.common.config;

import io.github.iutils.blockrefill.domain.BlockRefillSettings;
import io.github.iutils.toolswap.domain.ToolSwapSettings;
import io.github.iutils.welcome.domain.WelcomeSettings;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the lifecycle of {@code config.yml}, including default file creation,
 * YAML parsing, validation, and hot-reloading.
 */
public final class ConfigManager {

    public static final String CONFIG_FILE_NAME = "config.yml";

    private final Path configFile;
    private final Logger logger;
    private final Supplier<InputStream> defaultResourceSupplier;
    private final ConfigValidator validator;
    private final AtomicReference<PluginConfig> activeConfig;

    public ConfigManager(JavaPlugin plugin) {
        this(
                plugin.getDataFolder().toPath(),
                plugin.getLogger(),
                () -> plugin.getResource(CONFIG_FILE_NAME),
                new ConfigValidator()
        );
    }

    public ConfigManager(
            Path dataDirectory,
            Logger logger,
            Supplier<InputStream> defaultResourceSupplier,
            ConfigValidator validator
    ) {
        Objects.requireNonNull(dataDirectory, "dataDirectory cannot be null");
        this.configFile = dataDirectory.resolve(CONFIG_FILE_NAME);
        this.logger = Objects.requireNonNull(logger, "logger cannot be null");
        this.defaultResourceSupplier = Objects.requireNonNull(
                defaultResourceSupplier,
                "defaultResourceSupplier cannot be null"
        );
        this.validator = Objects.requireNonNull(validator, "validator cannot be null");
        this.activeConfig = new AtomicReference<>(PluginConfig.defaults());
    }

    /**
     * Performs the initial load of {@code config.yml} during plugin startup.
     * If invalid values are found, warnings are logged and safe fallback defaults are used.
     *
     * @return the validation result of the initial load
     */
    public ConfigValidationResult loadInitial() {
        ensureConfigFileExists();
        ConfigValidationResult result = parseAndValidateFile();

        activeConfig.set(result.config());

        if (!result.isValid()) {
            logger.warning("Se detectaron errores en config.yml durante el inicio. "
                    + "Se utilizarán valores predeterminados seguros para las propiedades inválidas:");
            for (String error : result.errors()) {
                logger.warning(" - " + error);
            }
        }

        return result;
    }

    /**
     * Reloads {@code config.yml} from disk.
     * If validation succeeds, the new configuration becomes active immediately.
     * If validation fails, errors are logged and the previous valid configuration remains active.
     *
     * @return the validation result of the reload operation
     */
    public ConfigValidationResult reload() {
        ensureConfigFileExists();
        ConfigValidationResult result = parseAndValidateFile();

        if (!result.isValid()) {
            logger.severe("No se pudo aplicar la recarga de config.yml debido a errores de validación. "
                    + "Se mantiene la configuración válida anterior:");
            for (String error : result.errors()) {
                logger.severe(" - " + error);
            }
            return new ConfigValidationResult(activeConfig.get(), result.errors());
        }

        activeConfig.set(result.config());
        logger.info("Configuración de Iutils recargada y validada correctamente.");
        return result;
    }

    /**
     * Returns the currently active immutable plugin configuration.
     *
     * @return the active {@link PluginConfig}
     */
    public PluginConfig getConfig() {
        return activeConfig.get();
    }

    /**
     * Returns the currently active welcome module settings.
     *
     * @return the active {@link WelcomeSettings}
     */
    public WelcomeSettings getWelcomeSettings() {
        return activeConfig.get().welcome();
    }

    /**
     * Returns the currently active automatic block refill module settings.
     *
     * @return the active {@link BlockRefillSettings}
     */
    public BlockRefillSettings getBlockRefillSettings() {
        return activeConfig.get().blockRefill();
    }

    /**
     * Returns the active tool swap settings.
     *
     * @return the active {@link ToolSwapSettings}
     */
    public ToolSwapSettings getToolSwapSettings() {
        return activeConfig.get().toolSwap();
    }

    private void ensureConfigFileExists() {
        if (Files.exists(configFile)) {
            return;
        }
        try {
            Path parent = configFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (InputStream in = defaultResourceSupplier.get()) {
                if (in == null) {
                    throw new IOException("No se encontró el recurso predeterminado " + CONFIG_FILE_NAME);
                }
                Files.copy(in, configFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "No se pudo crear el archivo predeterminado config.yml.", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private ConfigValidationResult parseAndValidateFile() {
        if (!Files.exists(configFile)) {
            return new ConfigValidationResult(
                    PluginConfig.defaults(),
                    List.of("El archivo config.yml no existe en el directorio del plugin.")
            );
        }

        try {
            String rawContent = Files.readString(configFile, StandardCharsets.UTF_8);
            Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
            Object loaded = yaml.load(rawContent);

            if (!(loaded instanceof Map<?, ?> loadedMap)) {
                return new ConfigValidationResult(
                        PluginConfig.defaults(),
                        List.of("El contenido de config.yml debe ser un mapa YAML clave-valor válido.")
                );
            }

            return validator.validate((Map<String, Object>) loadedMap);
        } catch (YAMLException ex) {
            return new ConfigValidationResult(
                    PluginConfig.defaults(),
                    List.of("Error de sintaxis YAML en config.yml: " + ex.getMessage())
            );
        } catch (IOException ex) {
            return new ConfigValidationResult(
                    PluginConfig.defaults(),
                    List.of("Error de lectura en config.yml: " + ex.getMessage())
            );
        }
    }
}
