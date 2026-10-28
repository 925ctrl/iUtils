package io.github.iutils.welcome.application;

import io.github.iutils.welcome.domain.WelcomeMessage;
import io.github.iutils.welcome.domain.WelcomePlayer;
import io.github.iutils.welcome.domain.WelcomeSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SendWelcomeMessageUseCaseTest {

    @Test
    @DisplayName("Should generate welcome message and substitute {player} placeholder once when enabled")
    void shouldGenerateWelcomeMessageAndSubstitutePlaceholder() {
        List<WelcomeMessage> deliveredMessages = new ArrayList<>();
        WelcomeSettings settings = new WelcomeSettings(
                true,
                true,
                "<green>¡Hola <yellow>{player}</yellow>, bienvenido a la comunidad!</green>"
        );
        SendWelcomeMessageUseCase useCase = new SendWelcomeMessageUseCase(
                () -> settings,
                deliveredMessages::add
        );

        WelcomePlayer player = new WelcomePlayer(UUID.randomUUID(), "AlexCraft");
        boolean sent = useCase.execute(player);

        assertTrue(sent);
        assertEquals(1, deliveredMessages.size(), "Message must be sent exactly once per event");

        WelcomeMessage delivered = deliveredMessages.getFirst();
        assertEquals(player, delivered.player());
        assertTrue(delivered.usePrefix());
        assertEquals(
                "<green>¡Hola <yellow>AlexCraft</yellow>, bienvenido a la comunidad!</green>",
                delivered.renderedText()
        );
    }

    @Test
    @DisplayName("Should replace multiple occurrences of {player} placeholder in template")
    void shouldReplaceMultipleOccurrencesOfPlayerPlaceholder() {
        List<WelcomeMessage> deliveredMessages = new ArrayList<>();
        WelcomeSettings settings = new WelcomeSettings(
                true,
                false,
                "Bienvenido {player}! Tu usuario registrado es {player}."
        );
        SendWelcomeMessageUseCase useCase = new SendWelcomeMessageUseCase(
                () -> settings,
                deliveredMessages::add
        );

        WelcomePlayer player = new WelcomePlayer(UUID.randomUUID(), "Steve");
        boolean sent = useCase.execute(player);

        assertTrue(sent);
        assertEquals(1, deliveredMessages.size());
        assertEquals(
                "Bienvenido Steve! Tu usuario registrado es Steve.",
                deliveredMessages.getFirst().renderedText()
        );
        assertFalse(deliveredMessages.getFirst().usePrefix());
    }

    @Test
    @DisplayName("Should not send any message when welcome is disabled in configuration")
    void shouldNotSendMessageWhenWelcomeIsDisabled() {
        List<WelcomeMessage> deliveredMessages = new ArrayList<>();
        WelcomeSettings disabledSettings = new WelcomeSettings(
                false,
                true,
                "<green>¡Bienvenido, {player}!</green>"
        );
        SendWelcomeMessageUseCase useCase = new SendWelcomeMessageUseCase(
                () -> disabledSettings,
                deliveredMessages::add
        );

        WelcomePlayer player = new WelcomePlayer(UUID.randomUUID(), "Miner99");
        boolean sent = useCase.execute(player);

        assertFalse(sent);
        assertTrue(deliveredMessages.isEmpty(), "No message should be delivered when welcome is disabled");
    }

    @Test
    @DisplayName("Should not send any message when welcome template is blank")
    void shouldNotSendMessageWhenTemplateIsBlank() {
        List<WelcomeMessage> deliveredMessages = new ArrayList<>();
        WelcomeSettings blankTemplateSettings = new WelcomeSettings(true, true, "   ");
        SendWelcomeMessageUseCase useCase = new SendWelcomeMessageUseCase(
                () -> blankTemplateSettings,
                deliveredMessages::add
        );

        WelcomePlayer player = new WelcomePlayer(UUID.randomUUID(), "Miner99");
        boolean sent = useCase.execute(player);

        assertFalse(sent);
        assertTrue(deliveredMessages.isEmpty());
    }

    @Test
    @DisplayName("Should immediately reflect updated settings when configuration is reloaded")
    void shouldReflectUpdatedSettingsDynamically() {
        List<WelcomeMessage> deliveredMessages = new ArrayList<>();
        AtomicReference<WelcomeSettings> currentSettings = new AtomicReference<>(
                new WelcomeSettings(true, true, "Mensaje inicial para {player}")
        );
        SendWelcomeMessageUseCase useCase = new SendWelcomeMessageUseCase(
                currentSettings::get,
                deliveredMessages::add
        );

        WelcomePlayer player = new WelcomePlayer(UUID.randomUUID(), "Elena");
        assertTrue(useCase.execute(player));
        assertEquals("Mensaje inicial para Elena", deliveredMessages.getFirst().renderedText());

        currentSettings.set(new WelcomeSettings(true, false, "Mensaje recargado para {player}"));
        assertTrue(useCase.execute(player));
        assertEquals(2, deliveredMessages.size());
        assertEquals("Mensaje recargado para Elena", deliveredMessages.get(1).renderedText());
    }

    @Test
    @DisplayName("Should reject blank or null player names in domain model")
    void shouldRejectInvalidPlayerDomainModel() {
        UUID id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> new WelcomePlayer(id, "   "));
        assertThrows(NullPointerException.class, () -> new WelcomePlayer(null, "Steve"));
        assertThrows(NullPointerException.class, () -> new WelcomePlayer(id, null));
    }
}
