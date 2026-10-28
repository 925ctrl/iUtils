package io.github.iutils.blockrefill.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

/**
 * Domain request describing the player's inventory state after depleting a building block stack.
 *
 * @param playerId         the unique identifier of the player
 * @param handSlotType     the hand where the block stack was depleted
 * @param targetHotbarSlot the active hotbar index (0-8) held by the player
 * @param placedBlockType  the identifier of the depleted block type
 * @param targetSlotEmpty  whether the target hand slot is currently empty and ready for refill
 * @param availableSlots   the immutable snapshot of non-empty storage slots (0-35)
 */
public record BlockRefillRequest(
        UUID playerId,
        HandSlotType handSlotType,
        int targetHotbarSlot,
        String placedBlockType,
        boolean targetSlotEmpty,
        List<InventorySlot> availableSlots
) {

    public BlockRefillRequest {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(handSlotType, "handSlotType cannot be null");
        if (targetHotbarSlot < InventorySlot.MIN_STORAGE_INDEX || targetHotbarSlot > InventorySlot.MAX_HOTBAR_INDEX) {
            throw new IllegalArgumentException("targetHotbarSlot must be between 0 and 8");
        }
        Objects.requireNonNull(placedBlockType, "placedBlockType cannot be null");
        if (placedBlockType.isBlank()) {
            throw new IllegalArgumentException("placedBlockType cannot be blank");
        }
        Objects.requireNonNull(availableSlots, "availableSlots cannot be null");
        availableSlots = List.copyOf(availableSlots);
    }

    /**
     * Finds the best source slot index to refill the depleted hand slot.
     * Prioritizes backpack slots (9-35) before other hotbar slots (0-8).
     *
     * @return an {@link OptionalInt} containing the source slot index if a compatible stack exists
     */
    public OptionalInt findReplacementSlotIndex() {
        if (!targetSlotEmpty) {
            return OptionalInt.empty();
        }

        OptionalInt backpackMatch = availableSlots.stream()
                .filter(InventorySlot::isBackpackSlot)
                .filter(this::isCompatibleCandidate)
                .sorted(Comparator.comparingInt(InventorySlot::index))
                .mapToInt(InventorySlot::index)
                .findFirst();

        if (backpackMatch.isPresent()) {
            return backpackMatch;
        }

        return availableSlots.stream()
                .filter(InventorySlot::isHotbarSlot)
                .filter(slot -> handSlotType != HandSlotType.MAIN_HAND || slot.index() != targetHotbarSlot)
                .filter(this::isCompatibleCandidate)
                .sorted(Comparator.comparingInt(InventorySlot::index))
                .mapToInt(InventorySlot::index)
                .findFirst();
    }

    private boolean isCompatibleCandidate(InventorySlot slot) {
        return slot.matchingTarget() && slot.itemType().equals(placedBlockType) && slot.amount() > 0;
    }
}
