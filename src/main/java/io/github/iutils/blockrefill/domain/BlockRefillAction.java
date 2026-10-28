package io.github.iutils.blockrefill.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Domain action instructing the inventory adapter to move a stack from a source storage slot
 * into the depleted hand slot.
 *
 * @param playerId         the unique identifier of the player
 * @param handSlotType     the target hand to refill
 * @param sourceSlotIndex  the inventory storage slot index (0-35) containing the replacement stack
 * @param targetHotbarSlot the target hotbar index (0-8) when refilling {@link HandSlotType#MAIN_HAND}
 */
public record BlockRefillAction(
        UUID playerId,
        HandSlotType handSlotType,
        int sourceSlotIndex,
        int targetHotbarSlot
) {

    public BlockRefillAction {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(handSlotType, "handSlotType cannot be null");
        if (sourceSlotIndex < InventorySlot.MIN_STORAGE_INDEX || sourceSlotIndex > InventorySlot.MAX_STORAGE_INDEX) {
            throw new IllegalArgumentException("sourceSlotIndex must be between 0 and 35");
        }
        if (targetHotbarSlot < InventorySlot.MIN_STORAGE_INDEX || targetHotbarSlot > InventorySlot.MAX_HOTBAR_INDEX) {
            throw new IllegalArgumentException("targetHotbarSlot must be between 0 and 8");
        }
        if (handSlotType == HandSlotType.MAIN_HAND && sourceSlotIndex == targetHotbarSlot) {
            throw new IllegalArgumentException("sourceSlotIndex cannot be equal to targetHotbarSlot for MAIN_HAND");
        }
    }
}
