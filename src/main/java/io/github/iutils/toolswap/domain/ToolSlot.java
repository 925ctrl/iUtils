package io.github.iutils.toolswap.domain;

import java.util.Objects;

/**
 * Snapshot of a tool stored in a player's storage inventory.
 *
 * @param index               the storage slot index
 * @param type                the tool category
 * @param material            the tool material tier
 * @param remainingDurability uses left before the tool breaks
 */
public record ToolSlot(int index, ToolType type, ToolMaterial material, int remainingDurability) {

    public ToolSlot {
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(material, "material cannot be null");
    }
}