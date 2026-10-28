package io.github.iutils.common.config;

import java.util.Locale;
import java.util.Optional;

/**
 * Supported text formatting engines for plugin messages.
 */
public enum MessageFormatMode {
    /**
     * Adventure MiniMessage tags (e.g., {@code <green>}, {@code <gradient:#00d2ff:#3a7bd5>}).
     */
    MINIMESSAGE,

    /**
     * Legacy Minecraft ampersand color codes (e.g., {@code &a}, {@code &e}, {@code &l}).
     */
    LEGACY,

    /**
     * Unformatted plain text without color or style parsing.
     */
    PLAIN;

    /**
     * Parses a configuration string into a {@link MessageFormatMode}.
     *
     * @param rawValue the string from configuration
     * @return an {@link Optional} containing the matched mode, or empty if invalid
     */
    public static Optional<MessageFormatMode> fromString(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }
        String normalized = rawValue.trim().toUpperCase(Locale.ROOT);
        for (MessageFormatMode mode : values()) {
            if (mode.name().equals(normalized)) {
                return Optional.of(mode);
            }
        }
        return Optional.empty();
    }
}
