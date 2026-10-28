package io.github.iutils.welcome.presentation;

import io.github.iutils.common.config.MessageFormatMode;
import io.github.iutils.common.config.PluginConfig;
import io.github.iutils.common.messaging.MessageFormatter;
import io.github.iutils.welcome.domain.WelcomeMessage;
import io.github.iutils.welcome.domain.WelcomePlayer;
import io.github.iutils.welcome.domain.WelcomeSettings;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdventureWelcomePresenterTest {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    @Test
    @DisplayName("Should format and deliver MiniMessage welcome component with prefix")
    void shouldFormatAndDeliverMiniMessageWithPrefix() {
        CapturingAudience audience = new CapturingAudience();
        WelcomeSettings welcomeSettings = new WelcomeSettings(
                true,
                true,
                "<green>¡Bienvenido al servidor, <yellow>{player}</yellow>!</green>"
        );
        PluginConfig config = new PluginConfig(
                "es_ES",
                "<aqua>[Iutils]</aqua> ",
                MessageFormatMode.MINIMESSAGE,
                welcomeSettings
        );

        AdventureWelcomePresenter presenter = new AdventureWelcomePresenter(
                () -> config,
                new MessageFormatter(),
                uuid -> Optional.of(audience)
        );

        WelcomePlayer player = new WelcomePlayer(UUID.randomUUID(), "Carlos");
        WelcomeMessage message = welcomeSettings.resolveFor(player);

        presenter.present(message);

        assertEquals(1, audience.messages().size());
        assertEquals(
                "[Iutils] ¡Bienvenido al servidor, Carlos!",
                PLAIN.serialize(audience.messages().getFirst())
        );
    }

    @Test
    @DisplayName("Should omit prefix when usePrefix is false")
    void shouldOmitPrefixWhenUsePrefixIsFalse() {
        CapturingAudience audience = new CapturingAudience();
        WelcomeSettings welcomeSettings = new WelcomeSettings(
                true,
                false,
                "<green>Hola, {player}</green>"
        );
        PluginConfig config = new PluginConfig(
                "es_ES",
                "[Iutils] ",
                MessageFormatMode.MINIMESSAGE,
                welcomeSettings
        );

        AdventureWelcomePresenter presenter = new AdventureWelcomePresenter(
                () -> config,
                new MessageFormatter(),
                uuid -> Optional.of(audience)
        );

        WelcomePlayer player = new WelcomePlayer(UUID.randomUUID(), "Lucia");
        presenter.presentTo(audience, welcomeSettings.resolveFor(player));

        assertEquals(1, audience.messages().size());
        assertEquals("Hola, Lucia", PLAIN.serialize(audience.messages().getFirst()));
    }

    @Test
    @DisplayName("Should support LEGACY formatting mode with ampersand color codes")
    void shouldSupportLegacyFormatMode() {
        CapturingAudience audience = new CapturingAudience();
        WelcomeSettings welcomeSettings = new WelcomeSettings(
                true,
                true,
                "&aBienvenido &e{player} &aal servidor!"
        );
        PluginConfig config = new PluginConfig(
                "es_ES",
                "&b[Iutils] ",
                MessageFormatMode.LEGACY,
                welcomeSettings
        );

        AdventureWelcomePresenter presenter = new AdventureWelcomePresenter(
                () -> config,
                new MessageFormatter(),
                uuid -> Optional.of(audience)
        );

        WelcomePlayer player = new WelcomePlayer(UUID.randomUUID(), "Pedro");
        presenter.presentTo(audience, welcomeSettings.resolveFor(player));

        assertEquals(1, audience.messages().size());
        assertEquals("[Iutils] Bienvenido Pedro al servidor!", PLAIN.serialize(audience.messages().getFirst()));
    }

    @Test
    @DisplayName("Should escape MiniMessage tags in player name to prevent tag injection")
    void shouldEscapeMiniMessageTagsInPlayerName() {
        CapturingAudience audience = new CapturingAudience();
        WelcomeSettings welcomeSettings = new WelcomeSettings(
                true,
                false,
                "<green>Bienvenido {player}</green>"
        );
        PluginConfig config = new PluginConfig(
                "es_ES",
                "",
                MessageFormatMode.MINIMESSAGE,
                welcomeSettings
        );

        AdventureWelcomePresenter presenter = new AdventureWelcomePresenter(
                () -> config,
                new MessageFormatter(),
                uuid -> Optional.of(audience)
        );

        WelcomePlayer player = new WelcomePlayer(UUID.randomUUID(), "<red>Injector</red>");
        presenter.presentTo(audience, welcomeSettings.resolveFor(player));

        assertEquals(1, audience.messages().size());
        assertEquals("Bienvenido <red>Injector</red>", PLAIN.serialize(audience.messages().getFirst()));
    }

    private static final class CapturingAudience implements Audience {
        private final List<Component> messages = new ArrayList<>();

        @Override
        public void sendMessage(Component message) {
            messages.add(message);
        }

        List<Component> messages() {
            return messages;
        }
    }
}
