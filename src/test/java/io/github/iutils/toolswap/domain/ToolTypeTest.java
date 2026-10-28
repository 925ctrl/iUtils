package io.github.iutils.toolswap.domain;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolTypeTest {

    @Test
    void parsesSupportedTools() {
        assertEquals(Optional.of(ToolType.PICKAXE), ToolType.fromMaterialName("DIAMOND_PICKAXE"));
        assertEquals(Optional.of(ToolType.SWORD), ToolType.fromMaterialName("GOLDEN_SWORD"));
        assertEquals(Optional.of(ToolType.SHOVEL), ToolType.fromMaterialName("WOODEN_SHOVEL"));
        assertEquals(Optional.of(ToolType.HOE), ToolType.fromMaterialName("COPPER_HOE"));
        assertEquals(Optional.of(ToolType.AXE), ToolType.fromMaterialName("NETHERITE_AXE"));
    }

    @Test
    void pickaxeIsNotAxe() {
        assertEquals(Optional.of(ToolType.PICKAXE), ToolType.fromMaterialName("IRON_PICKAXE"));
    }

    @Test
    void rejectsUnsupportedItems() {
        assertTrue(ToolType.fromMaterialName("TRIDENT").isEmpty());
        assertTrue(ToolType.fromMaterialName("SHEARS").isEmpty());
        assertTrue(ToolType.fromMaterialName("FISHING_ROD").isEmpty());
        assertTrue(ToolType.fromMaterialName("STONE").isEmpty());
    }
}