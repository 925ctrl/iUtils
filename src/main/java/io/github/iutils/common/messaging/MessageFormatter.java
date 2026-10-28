package io.github.iutils.common.messaging;

import io.github.iutils.common.config.MessageFormatMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Map;
import java.util.Objects;

/**
 * Formats plugin messages into Adventure {@link Component} instances according
 * to the configured {@link MessageFormatMode} and optional prefix.
 */
public final class MessageFormatter {

    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;

    public MessageFormatter() {
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();
    }

    /**
     * Formats a raw message string with optional prefix using the specified format mode.
     *
     * @param message       the message text to format
     * @param formatMode    the formatting engine to use
     * @param includePrefix whether to prepend the prefix
     * @param prefix        the prefix text to prepend when {@code includePrefix} is true
     * @return the rendered Adventure {@link Component}
     */
    public Component format(
            String message,
            MessageFormatMode formatMode,
            boolean includePrefix,
            String prefix
    ) {
        Objects.requireNonNull(message, "message cannot be null");
        Objects.requireNonNull(formatMode, "formatMode cannot be null");
        Objects.requireNonNull(prefix, "prefix cannot be null");

        Component bodyComponent = deserialize(message, formatMode);
        if (!includePrefix || prefix.isEmpty()) {
            return bodyComponent;
        }

        Component prefixComponent = deserialize(prefix, formatMode);
        return Component.empty().append(prefixComponent).append(bodyComponent);
    }

    /**
     * Formats a message template by safely substituting placeholders before deserializing
     * with the target {@link MessageFormatMode}.
     * In {@link MessageFormatMode#MINIMESSAGE} mode, placeholder values have their tags escaped
     * so untrusted values (such as player names) cannot inject MiniMessage tags.
     *
     * @param template      the message template containing placeholders (e.g., {@code {player}})
     * @param placeholders  map of literal tokens (e.g., {@code "{player}"}) to replacement values
     * @param formatMode    the formatting engine to use
     * @param includePrefix whether to prepend the prefix
     * @param prefix        the prefix text to prepend when {@code includePrefix} is true
     * @return the rendered Adventure {@link Component}
     */
    public Component formatTemplate(
            String template,
            Map<String, String> placeholders,
            MessageFormatMode formatMode,
            boolean includePrefix,
            String prefix
    ) {
        Objects.requireNonNull(template, "template cannot be null");
        Objects.requireNonNull(placeholders, "placeholders cannot be null");
        Objects.requireNonNull(formatMode, "formatMode cannot be null");
        Objects.requireNonNull(prefix, "prefix cannot be null");

        String resolved = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String key = entry.getKey();
            String rawValue = entry.getValue() == null ? "" : entry.getValue();
            String safeValue = formatMode == MessageFormatMode.MINIMESSAGE
                    ? miniMessage.escapeTags(rawValue)
                    : rawValue;
            resolved = resolved.replace(key, safeValue);
        }

        return format(resolved, formatMode, includePrefix, prefix);
    }

    private Component deserialize(String input, MessageFormatMode formatMode) {
        return switch (formatMode) {
            case MINIMESSAGE -> miniMessage.deserialize(input);
            case LEGACY -> legacySerializer.deserialize(input);
            case PLAIN -> Component.text(input);
        };
    }
}
