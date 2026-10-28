package io.github.iutils.welcome.presentation;

import io.github.iutils.common.config.PluginConfig;
import io.github.iutils.common.messaging.MessageFormatter;
import io.github.iutils.welcome.application.WelcomeMessagePresenter;
import io.github.iutils.welcome.domain.WelcomeMessage;
import io.github.iutils.welcome.domain.WelcomeSettings;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Presentation component that renders a {@link WelcomeMessage} into an Adventure {@link Component}
 * and delivers it to a player's {@link Audience}.
 */
public final class AdventureWelcomePresenter implements WelcomeMessagePresenter {

    private final Supplier<PluginConfig> configSupplier;
    private final MessageFormatter messageFormatter;
    private final Function<UUID, Optional<? extends Audience>> audienceResolver;

    public AdventureWelcomePresenter(
            Supplier<PluginConfig> configSupplier,
            MessageFormatter messageFormatter,
            Function<UUID, Optional<? extends Audience>> audienceResolver
    ) {
        this.configSupplier = Objects.requireNonNull(configSupplier, "configSupplier cannot be null");
        this.messageFormatter = Objects.requireNonNull(messageFormatter, "messageFormatter cannot be null");
        this.audienceResolver = Objects.requireNonNull(audienceResolver, "audienceResolver cannot be null");
    }

    @Override
    public void present(WelcomeMessage message) {
        Objects.requireNonNull(message, "WelcomeMessage cannot be null");
        audienceResolver.apply(message.player().uniqueId())
                .ifPresent(audience -> presentTo(audience, message));
    }

    /**
     * Renders and sends the welcome message directly to the provided Adventure {@link Audience}.
     *
     * @param audience the recipient audience (e.g., Paper Player)
     * @param message  the resolved domain welcome message
     */
    public void presentTo(Audience audience, WelcomeMessage message) {
        Objects.requireNonNull(audience, "audience cannot be null");
        Objects.requireNonNull(message, "WelcomeMessage cannot be null");

        Component component = render(message);
        audience.sendMessage(component);
    }

    /**
     * Builds the Adventure {@link Component} for the given {@link WelcomeMessage}
     * using the currently active plugin configuration.
     *
     * @param message the resolved domain welcome message
     * @return the formatted Adventure {@link Component}
     */
    public Component render(WelcomeMessage message) {
        Objects.requireNonNull(message, "WelcomeMessage cannot be null");
        PluginConfig config = configSupplier.get();

        return messageFormatter.formatTemplate(
                message.template(),
                Map.of(WelcomeSettings.PLAYER_PLACEHOLDER, message.player().name()),
                config.formatMode(),
                message.usePrefix(),
                config.prefix()
        );
    }
}
