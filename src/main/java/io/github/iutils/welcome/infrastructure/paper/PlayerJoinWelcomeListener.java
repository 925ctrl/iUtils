package io.github.iutils.welcome.infrastructure.paper;

import io.github.iutils.welcome.application.SendWelcomeMessageUseCase;
import io.github.iutils.welcome.domain.WelcomePlayer;
import io.github.iutils.welcome.presentation.AdventureWelcomePresenter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

/**
 * Paper infrastructure adapter that listens to {@link PlayerJoinEvent} and triggers
 * the {@link SendWelcomeMessageUseCase}.
 */
public final class PlayerJoinWelcomeListener implements Listener {

    private final SendWelcomeMessageUseCase sendWelcomeMessageUseCase;
    private final AdventureWelcomePresenter welcomePresenter;

    public PlayerJoinWelcomeListener(
            SendWelcomeMessageUseCase sendWelcomeMessageUseCase,
            AdventureWelcomePresenter welcomePresenter
    ) {
        this.sendWelcomeMessageUseCase = Objects.requireNonNull(
                sendWelcomeMessageUseCase,
                "sendWelcomeMessageUseCase cannot be null"
        );
        this.welcomePresenter = Objects.requireNonNull(
                welcomePresenter,
                "welcomePresenter cannot be null"
        );
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        WelcomePlayer welcomePlayer = new WelcomePlayer(player.getUniqueId(), player.getName());
        sendWelcomeMessageUseCase.execute(
                welcomePlayer,
                message -> welcomePresenter.presentTo(player, message)
        );
    }
}
