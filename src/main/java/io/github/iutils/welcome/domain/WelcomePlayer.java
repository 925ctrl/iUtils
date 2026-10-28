package io.github.iutils.welcome.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents the essential identity of a player entering the server for the welcome domain.
 *
 * @param uniqueId the unique identifier of the player
 * @param name     the display/account name of the player
 */
public record WelcomePlayer(UUID uniqueId, String name) {

    public WelcomePlayer {
        Objects.requireNonNull(uniqueId, "Player uniqueId cannot be null");
        Objects.requireNonNull(name, "Player name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be blank");
        }
    }
}
