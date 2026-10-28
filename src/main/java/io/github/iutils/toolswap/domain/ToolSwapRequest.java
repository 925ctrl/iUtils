package io.github.iutils.toolswap.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Context describing a tool about to break and the tools available to replace it.
 *
 * @param playerId        the player owning the inventory
 * @param hand            the hand holding the breaking tool
 * @param targetSlotIndex the storage slot of the breaking tool
 * @param brokenType      the category of the breaking tool
 * @param candidates      tools present in the storage inventory
 */
public record ToolSwapRequest(
        UUID playerId,
        ToolHand hand,
        int targetSlotIndex,
        ToolType brokenType,
        List<ToolSlot> candidates
) {

    /** Minimum remaining durability a replacement needs (one use would break it otherwise). */
    public static final int MIN_REPLACEMENT_DURABILITY = 2;

    public ToolSwapRequest {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(hand, "hand cannot be null");
        Objects.requireNonNull(brokenType, "brokenType cannot be null");
        candidates = List.copyOf(Objects.requireNonNull(candidates, "candidates cannot be null"));
    }

    /**
     * Finds the best replacement: same tool type, by material priority, then most remaining durability.
     *
     * @return the best replacement slot, or empty if none qualifies
     */
    public Optional<ToolSlot> findBestReplacement() {
        return candidates.stream()
                .filter(slot -> slot.type() == brokenType)
                .filter(slot -> hand != ToolHand.MAIN_HAND || slot.index() != targetSlotIndex)
                .filter(slot -> slot.remainingDurability() >= MIN_REPLACEMENT_DURABILITY)
                .min(Comparator.comparingInt((ToolSlot slot) -> slot.material().priority())
                        .thenComparing(Comparator.comparingInt(ToolSlot::remainingDurability).reversed())
                        .thenComparingInt(ToolSlot::index));
    }
}