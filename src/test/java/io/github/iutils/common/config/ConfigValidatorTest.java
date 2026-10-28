package io.github.iutils.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigValidatorTest {

    private final ConfigValidator validator = new ConfigValidator();

    @Test
    @DisplayName("Should validate a well-formed configuration map without errors")
    void shouldValidateValidConfiguration() {
        Map<String, Object> root = Map.of(
                "general", Map.of(
                        "language", "es_ES",
                        "prefix", "<aqua>[Servidor]</aqua> ",
                        "format", "MINIMESSAGE"
                ),
                "welcome", Map.of(
                        "enabled", true,
                        "use-prefix", false,
                        "message", "<green>Hola {player}</green>"
                ),
                "block-refill", Map.of(
                        "enabled", true
                )
        );

        ConfigValidationResult result = validator.validate(root);

        assertTrue(result.isValid());
        assertTrue(result.errors().isEmpty());
        assertEquals("es_ES", result.config().language());
        assertEquals("<aqua>[Servidor]</aqua> ", result.config().prefix());
        assertEquals(MessageFormatMode.MINIMESSAGE, result.config().formatMode());
        assertTrue(result.config().welcome().enabled());
        assertFalse(result.config().welcome().usePrefix());
        assertEquals("<green>Hola {player}</green>", result.config().welcome().messageTemplate());
        assertTrue(result.config().blockRefill().enabled());
    }

    @Test
    @DisplayName("Should report error when root configuration is empty or null")
    void shouldReportErrorWhenRootIsEmptyOrNull() {
        ConfigValidationResult nullResult = validator.validate(null);
        assertFalse(nullResult.isValid());
        assertEquals(1, nullResult.errors().size());

        ConfigValidationResult emptyResult = validator.validate(Map.of());
        assertFalse(emptyResult.isValid());
        assertEquals(1, emptyResult.errors().size());
    }

    @Test
    @DisplayName("Should report errors for invalid format, non-boolean flags, and blank welcome message")
    void shouldReportErrorsForInvalidValues() {
        Map<String, Object> general = new HashMap<>();
        general.put("language", "   ");
        general.put("prefix", 12345);
        general.put("format", "FORMATO_INEXISTENTE");

        Map<String, Object> welcome = new HashMap<>();
        welcome.put("enabled", "yes");
        welcome.put("use-prefix", "no");
        welcome.put("message", "   ");

        Map<String, Object> blockRefill = new HashMap<>();
        blockRefill.put("enabled", "invalid_boolean");

        Map<String, Object> root = Map.of(
                "general", general,
                "welcome", welcome,
                "block-refill", blockRefill
        );

        ConfigValidationResult result = validator.validate(root);

        assertFalse(result.isValid());
        assertEquals(7, result.errors().size());
        assertEquals(PluginConfig.DEFAULT_LANGUAGE, result.config().language());
        assertEquals(PluginConfig.DEFAULT_PREFIX, result.config().prefix());
        assertEquals(MessageFormatMode.MINIMESSAGE, result.config().formatMode());
        assertTrue(result.config().blockRefill().enabled());
    }

    @Test
    @DisplayName("Should allow blank welcome message only when welcome.enabled is false")
    void shouldAllowBlankWelcomeMessageWhenDisabled() {
        Map<String, Object> root = Map.of(
                "general", Map.of(
                        "language", "es_ES",
                        "prefix", "[Iutils] ",
                        "format", "PLAIN"
                ),
                "welcome", Map.of(
                        "enabled", false,
                        "use-prefix", true,
                        "message", ""
                ),
                "block-refill", Map.of(
                        "enabled", false
                )
        );

        ConfigValidationResult result = validator.validate(root);

        assertTrue(result.isValid());
        assertFalse(result.config().welcome().enabled());
        assertFalse(result.config().blockRefill().enabled());
        assertEquals(MessageFormatMode.PLAIN, result.config().formatMode());
    }

    @Test
    @DisplayName("Should parse tool-swap section, default when missing, and reject non-boolean")
    void shouldValidateToolSwapSection() {
        Map<String, Object> base = new HashMap<>(Map.of(
                "general", Map.of("language", "es_ES", "prefix", "x", "format", "PLAIN"),
                "welcome", Map.of("enabled", true, "use-prefix", true, "message", "hola"),
                "block-refill", Map.of("enabled", true)
        ));

        ConfigValidationResult missing = validator.validate(base);
        assertTrue(missing.isValid());
        assertTrue(missing.config().toolSwap().enabled());

        base.put("tool-swap", Map.of("enabled", false));
        ConfigValidationResult disabled = validator.validate(base);
        assertTrue(disabled.isValid());
        assertFalse(disabled.config().toolSwap().enabled());

        base.put("tool-swap", Map.of("enabled", "si"));
        assertFalse(validator.validate(base).isValid());
    }
}
