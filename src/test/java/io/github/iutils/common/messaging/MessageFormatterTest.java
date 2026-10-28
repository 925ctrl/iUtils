package io.github.iutils.common.messaging;

import io.github.iutils.common.config.MessageFormatMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageFormatterTest {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    @Test
    @DisplayName("Should format plain mode literally without parsing tags or color codes")
    void shouldFormatPlainModeLiterally() {
        MessageFormatter formatter = new MessageFormatter();
        Component component = formatter.formatTemplate(
                "<green>Hola &a{player}</green>",
                Map.of("{player}", "Mateo"),
                MessageFormatMode.PLAIN,
                true,
                "[Prefijo] "
        );

        assertEquals("[Prefijo] <green>Hola &aMateo</green>", PLAIN.serialize(component));
    }

    @Test
    @DisplayName("Should format MiniMessage colors properly")
    void shouldFormatMiniMessageColors() {
        MessageFormatter formatter = new MessageFormatter();
        Component component = formatter.format(
                "<green>Activo</green>",
                MessageFormatMode.MINIMESSAGE,
                false,
                "[Iutils] "
        );

        assertEquals("Activo", PLAIN.serialize(component));
        assertEquals(NamedTextColor.GREEN, component.color());
    }
}
