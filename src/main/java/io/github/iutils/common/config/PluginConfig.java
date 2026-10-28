package io.github.iutils.common.config;

import io.github.iutils.blockrefill.domain.BlockRefillSettings;
import io.github.iutils.toolswap.domain.ToolSwapSettings;
import io.github.iutils.welcome.domain.WelcomeSettings;

import java.util.Objects;

/**
 * Immutable snapshot of the complete plugin configuration.
 *
 * @param language    the default language code (e.g., {@code es_ES})
 * @param prefix      the global plugin prefix string
 * @param formatMode  the message formatting engine mode
 * @param welcome     the welcome module configuration settings
 * @param blockRefill the automatic block refill module configuration settings
 * @param toolSwap    the automatic tool swap module configuration settings
 */
public record PluginConfig(
        String language,
        String prefix,
        MessageFormatMode formatMode,
        WelcomeSettings welcome,
        BlockRefillSettings blockRefill,
        ToolSwapSettings toolSwap
) {

    public static final String DEFAULT_LANGUAGE = "es_ES";
    public static final String DEFAULT_PREFIX = "<gradient:#00d2ff:#3a7bd5><bold>[Iutils]</bold></gradient> ";
    public static final MessageFormatMode DEFAULT_FORMAT_MODE = MessageFormatMode.MINIMESSAGE;

    public PluginConfig {
        Objects.requireNonNull(language, "language cannot be null");
        Objects.requireNonNull(prefix, "prefix cannot be null");
        Objects.requireNonNull(formatMode, "formatMode cannot be null");
        Objects.requireNonNull(welcome, "welcome settings cannot be null");
        Objects.requireNonNull(blockRefill, "blockRefill settings cannot be null");
        Objects.requireNonNull(toolSwap, "toolSwap settings cannot be null");
    }

    public PluginConfig(
            String language,
            String prefix,
            MessageFormatMode formatMode,
            WelcomeSettings welcome
    ) {
        this(language, prefix, formatMode, welcome, BlockRefillSettings.defaults(), ToolSwapSettings.defaults());
    }

    public PluginConfig(
            String language,
            String prefix,
            MessageFormatMode formatMode,
            WelcomeSettings welcome,
            BlockRefillSettings blockRefill
    ) {
        this(language, prefix, formatMode, welcome, blockRefill, ToolSwapSettings.defaults());
    }

    public static PluginConfig defaults() {
        return new PluginConfig(
                DEFAULT_LANGUAGE,
                DEFAULT_PREFIX,
                DEFAULT_FORMAT_MODE,
                WelcomeSettings.defaults(),
                BlockRefillSettings.defaults(),
                ToolSwapSettings.defaults()
        );
    }
}
