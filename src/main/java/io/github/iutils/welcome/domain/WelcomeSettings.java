package io.github.iutils.welcome.domain;

import java.util.Objects;

/**
 * Immutable domain configuration for the welcome feature.
 *
 * @param enabled         whether the welcome message feature is active
 * @param usePrefix       whether the global plugin prefix should be prepended
 * @param messageTemplate the message template containing optional placeholders like {@code {player}}
 */
public record WelcomeSettings(
        boolean enabled,
        boolean usePrefix,
        String messageTemplate
) {

    public static final String PLAYER_PLACEHOLDER = "{player}";
    public static final String DEFAULT_MESSAGE_TEMPLATE =
            "<green>¡Bienvenido al servidor, <yellow>{player}</yellow>! Esperamos que disfrutes tu estancia.</green>";

    public WelcomeSettings {
        Objects.requireNonNull(messageTemplate, "Welcome messageTemplate cannot be null");
    }

    public static WelcomeSettings defaults() {
        return new WelcomeSettings(true, true, DEFAULT_MESSAGE_TEMPLATE);
    }

    /**
     * Resolves the welcome message for a specific player by substituting the {@code {player}} placeholder.
     *
     * @param player the joining player
     * @return the resolved {@link WelcomeMessage}
     */
    public WelcomeMessage resolveFor(WelcomePlayer player) {
        Objects.requireNonNull(player, "WelcomePlayer cannot be null");
        String resolvedText = messageTemplate.replace(PLAYER_PLACEHOLDER, player.name());
        return new WelcomeMessage(player, messageTemplate, resolvedText, usePrefix);
    }
}
