package io.github.iutils.toolswap.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Resolved instruction to swap the breaking tool with a replacement slot.
 *
 * @param playerId             the player owning the inventory
 * @param hand                 the hand holding the breaking tool
 * @param targetSlotIndex      the storage slot of the breaking tool (main hand only)
 * @param replacementSlotIndex the storage slot of the replacement tool
 */
public record ToolSwapAction(UUID playerId, ToolHand hand, int targetSlotIndex, int replacementSlotIndex) {

    public ToolSwapAction {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(hand, "hand cannot be null");
    }
}