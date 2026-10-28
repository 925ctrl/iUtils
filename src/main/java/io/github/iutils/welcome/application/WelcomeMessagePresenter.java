package io.github.iutils.welcome.application;

import io.github.iutils.welcome.domain.WelcomeMessage;

/**
 * Output port responsible for presenting a resolved welcome message to a player.
 */
@FunctionalInterface
public interface WelcomeMessagePresenter {

    /**
     * Presents the generated welcome message.
     *
     * @param message the resolved domain welcome message
     */
    void present(WelcomeMessage message);
}
