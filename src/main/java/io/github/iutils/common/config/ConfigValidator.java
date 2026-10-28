package io.github.iutils.common.config;

import io.github.iutils.blockrefill.domain.BlockRefillSettings;
import io.github.iutils.toolswap.domain.ToolSwapSettings;
import io.github.iutils.welcome.domain.WelcomeSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Validates raw YAML configuration maps and produces a {@link ConfigValidationResult}
 * with detailed Spanish error messages for server administrators.
 */
public final class ConfigValidator {

    public ConfigValidationResult validate(Map<String, Object> root) {
        List<String> errors = new ArrayList<>();
        if (root == null || root.isEmpty()) {
            errors.add("El archivo config.yml está vacío o no contiene una estructura YAML válida.");
            return new ConfigValidationResult(PluginConfig.defaults(), errors);
        }

        Map<String, Object> generalSection = extractSection(root, "general", errors);
        Map<String, Object> welcomeSection = extractSection(root, "welcome", errors);
        Map<String, Object> blockRefillSection = extractSection(root, "block-refill", errors);

        Map<String, Object> toolSwapSection = extractOptionalSection(root, "tool-swap", errors);

        String language = validateLanguage(generalSection, errors);
        String prefix = validatePrefix(generalSection, errors);
        MessageFormatMode formatMode = validateFormatMode(generalSection, errors);

        boolean welcomeEnabled = validateBoolean(
                welcomeSection,
                "enabled",
                "welcome.enabled",
                WelcomeSettings.defaults().enabled(),
                errors
        );
        boolean welcomeUsePrefix = validateBoolean(
                welcomeSection,
                "use-prefix",
                "welcome.use-prefix",
                WelcomeSettings.defaults().usePrefix(),
                errors
        );
        String welcomeMessage = validateWelcomeMessage(welcomeSection, welcomeEnabled, errors);

        boolean blockRefillEnabled = validateBoolean(
                blockRefillSection,
                "enabled",
                "block-refill.enabled",
                BlockRefillSettings.defaults().enabled(),
                errors
        );

        boolean toolSwapEnabled = validateBoolean(
                toolSwapSection,
                "enabled",
                "tool-swap.enabled",
                ToolSwapSettings.defaults().enabled(),
                errors
        );

        WelcomeSettings welcomeSettings = new WelcomeSettings(
                welcomeEnabled,
                welcomeUsePrefix,
                welcomeMessage
        );
        BlockRefillSettings blockRefillSettings = new BlockRefillSettings(blockRefillEnabled);
        PluginConfig config = new PluginConfig(
                language,
                prefix,
                formatMode,
                welcomeSettings,
                blockRefillSettings,
                new ToolSwapSettings(toolSwapEnabled)
        );

        return new ConfigValidationResult(config, errors);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractSection(
            Map<String, Object> parent,
            String key,
            List<String> errors
    ) {
        Object rawSection = parent.get(key);
        if (rawSection == null) {
            errors.add("Falta la sección obligatoria '" + key + "' en config.yml.");
            return Collections.emptyMap();
        }
        if (!(rawSection instanceof Map<?, ?>)) {
            errors.add("La propiedad '" + key + "' debe ser una sección de configuración válida.");
            return Collections.emptyMap();
        }
        return (Map<String, Object>) rawSection;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractOptionalSection(
            Map<String, Object> parent,
            String key,
            List<String> errors
    ) {
        Object rawSection = parent.get(key);
        if (rawSection == null) {
            return Collections.emptyMap();
        }
        if (!(rawSection instanceof Map<?, ?>)) {
            errors.add("La propiedad '" + key + "' debe ser una secci\u00f3n de configuraci\u00f3n v\u00e1lida.");
            return Collections.emptyMap();
        }
        return (Map<String, Object>) rawSection;
    }

    private String validateLanguage(Map<String, Object> generalSection, List<String> errors) {
        if (generalSection.isEmpty()) {
            return PluginConfig.DEFAULT_LANGUAGE;
        }
        Object rawLanguage = generalSection.get("language");
        if (!(rawLanguage instanceof String language) || language.isBlank()) {
            errors.add("El valor 'general.language' debe ser un texto no vacío (ej. 'es_ES').");
            return PluginConfig.DEFAULT_LANGUAGE;
        }
        return language.trim();
    }

    private String validatePrefix(Map<String, Object> generalSection, List<String> errors) {
        if (generalSection.isEmpty()) {
            return PluginConfig.DEFAULT_PREFIX;
        }
        Object rawPrefix = generalSection.get("prefix");
        if (!(rawPrefix instanceof String prefix)) {
            errors.add("El valor 'general.prefix' debe ser una cadena de texto.");
            return PluginConfig.DEFAULT_PREFIX;
        }
        return prefix;
    }

    private MessageFormatMode validateFormatMode(Map<String, Object> generalSection, List<String> errors) {
        if (generalSection.isEmpty()) {
            return PluginConfig.DEFAULT_FORMAT_MODE;
        }
        Object rawFormat = generalSection.get("format");
        if (!(rawFormat instanceof String formatText)) {
            errors.add("El valor 'general.format' debe ser un texto ('MINIMESSAGE', 'LEGACY' o 'PLAIN').");
            return PluginConfig.DEFAULT_FORMAT_MODE;
        }
        Optional<MessageFormatMode> parsedMode = MessageFormatMode.fromString(formatText);
        if (parsedMode.isEmpty()) {
            errors.add("El formato '" + formatText + "' en 'general.format' no es válido. "
                    + "Valores permitidos: MINIMESSAGE, LEGACY, PLAIN.");
            return PluginConfig.DEFAULT_FORMAT_MODE;
        }
        return parsedMode.get();
    }

    private boolean validateBoolean(
            Map<String, Object> section,
            String key,
            String fullPath,
            boolean defaultValue,
            List<String> errors
    ) {
        if (section.isEmpty()) {
            return defaultValue;
        }
        Object rawValue = section.get(key);
        if (!(rawValue instanceof Boolean boolValue)) {
            errors.add("El valor '" + fullPath + "' debe ser un booleano estricto (true o false).");
            return defaultValue;
        }
        return boolValue;
    }

    private String validateWelcomeMessage(
            Map<String, Object> welcomeSection,
            boolean welcomeEnabled,
            List<String> errors
    ) {
        if (welcomeSection.isEmpty()) {
            return WelcomeSettings.DEFAULT_MESSAGE_TEMPLATE;
        }
        Object rawMessage = welcomeSection.get("message");
        if (!(rawMessage instanceof String message)) {
            errors.add("El valor 'welcome.message' debe ser una cadena de texto.");
            return WelcomeSettings.DEFAULT_MESSAGE_TEMPLATE;
        }
        if (welcomeEnabled && message.isBlank()) {
            errors.add("El valor 'welcome.message' no puede estar vacío cuando 'welcome.enabled' es true.");
            return WelcomeSettings.DEFAULT_MESSAGE_TEMPLATE;
        }
        return message;
    }
}
