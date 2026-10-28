package io.github.iutils.blockrefill.domain;

import java.util.Objects;

/**
 * Represents a populated storage slot inside a player's inventory.
 *
 * @param index          the inventory slot index (0-8 for hotbar, 9-35 for main backpack)
 * @param itemType       the canonical block/item identifier
 * @param amount         the stack amount in this slot (must be positive)
 * @param matchingTarget whether the item stack matches the depleted block (including metadata similarity)
 */
public record InventorySlot(
        int index,
        String itemType,
        int amount,
        boolean matchingTarget
) {

    public static final int MIN_STORAGE_INDEX = 0;
    public static final int MAX_HOTBAR_INDEX = 8;
    public static final int MIN_BACKPACK_INDEX = 9;
    public static final int MAX_STORAGE_INDEX = 35;

    public InventorySlot {
        if (index < MIN_STORAGE_INDEX || index > MAX_STORAGE_INDEX) {
            throw new IllegalArgumentException("Inventory slot index must be between 0 and 35");
        }
        Objects.requireNonNull(itemType, "itemType cannot be null");
        if (itemType.isBlank()) {
            throw new IllegalArgumentException("itemType cannot be blank");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
    }

    /**
     * Checks whether this slot belongs to the player's internal backpack storage (slots 9-35).
     *
     * @return {@code true} if the slot index is between 9 and 35
     */
    public boolean isBackpackSlot() {
        return index >= MIN_BACKPACK_INDEX && index <= MAX_STORAGE_INDEX;
    }

    /**
     * Checks whether this slot belongs to the player's hotbar (slots 0-8).
     *
     * @return {@code true} if the slot index is between 0 and 8
     */
    public boolean isHotbarSlot() {
        return index >= MIN_STORAGE_INDEX && index <= MAX_HOTBAR_INDEX;
    }
}
