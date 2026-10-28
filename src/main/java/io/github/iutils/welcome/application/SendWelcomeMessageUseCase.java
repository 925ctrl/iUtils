package io.github.iutils.welcome.application;

import io.github.iutils.welcome.domain.WelcomeMessage;
import io.github.iutils.welcome.domain.WelcomePlayer;
import io.github.iutils.welcome.domain.WelcomeSettings;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Application use case that coordinates generating and sending the welcome message
 * when a player joins the server.
 */
public final class SendWelcomeMessageUseCase {

    private final Supplier<WelcomeSettings> settingsProvider;
    private final WelcomeMessagePresenter defaultPresenter;

    public SendWelcomeMessageUseCase(
            Supplier<WelcomeSettings> settingsProvider,
            WelcomeMessagePresenter defaultPresenter
    ) {
        this.settingsProvider = Objects.requireNonNull(settingsProvider, "settingsProvider cannot be null");
        this.defaultPresenter = Objects.requireNonNull(defaultPresenter, "defaultPresenter cannot be null");
    }

    /**
     * Executes the welcome use case for the given player using the default presenter.
     *
     * @param player the player who joined the server
     * @return {@code true} if a welcome message was generated and sent, {@code false} if disabled or blank
     */
    public boolean execute(WelcomePlayer player) {
        return execute(player, defaultPresenter);
    }

    /**
     * Executes the welcome use case for the given player using a specific presenter callback.
     *
     * @param player    the player who joined the server
     * @param presenter the presenter responsible for delivering the message
     * @return {@code true} if a welcome message was generated and sent, {@code false} if disabled or blank
     */
    public boolean execute(WelcomePlayer player, WelcomeMessagePresenter presenter) {
        Objects.requireNonNull(player, "WelcomePlayer cannot be null");
        Objects.requireNonNull(presenter, "WelcomeMessagePresenter cannot be null");

        WelcomeSettings settings = settingsProvider.get();
        if (settings == null || !settings.enabled()) {
            return false;
        }

        if (settings.messageTemplate().isBlank()) {
            return false;
        }

        WelcomeMessage welcomeMessage = settings.resolveFor(player);
        presenter.present(welcomeMessage);
        return true;
    }
}
