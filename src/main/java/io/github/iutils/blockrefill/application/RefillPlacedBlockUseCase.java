package io.github.iutils.blockrefill.application;

import io.github.iutils.blockrefill.domain.BlockRefillAction;
import io.github.iutils.blockrefill.domain.BlockRefillRequest;
import io.github.iutils.blockrefill.domain.BlockRefillSettings;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Supplier;

/**
 * Application use case that coordinates finding a replacement block stack in a player's
 * inventory and moving it into the depleted hand slot.
 */
public final class RefillPlacedBlockUseCase {

    private final Supplier<BlockRefillSettings> settingsProvider;
    private final BlockRefillExecutor defaultExecutor;

    public RefillPlacedBlockUseCase(
            Supplier<BlockRefillSettings> settingsProvider,
            BlockRefillExecutor defaultExecutor
    ) {
        this.settingsProvider = Objects.requireNonNull(settingsProvider, "settingsProvider cannot be null");
        this.defaultExecutor = Objects.requireNonNull(defaultExecutor, "defaultExecutor cannot be null");
    }

    /**
     * Evaluates the refill request and executes the inventory transfer using the default executor.
     *
     * @param request the inventory snapshot and depleted block context
     * @return {@code true} if a replacement block stack was found and moved, {@code false} otherwise
     */
    public boolean execute(BlockRefillRequest request) {
        return execute(request, defaultExecutor);
    }

    /**
     * Evaluates the refill request and executes the inventory transfer using the provided executor callback.
     *
     * @param request  the inventory snapshot and depleted block context
     * @param executor the executor responsible for moving the stack in the player's inventory
     * @return {@code true} if a replacement block stack was found and moved, {@code false} otherwise
     */
    public boolean execute(BlockRefillRequest request, BlockRefillExecutor executor) {
        Objects.requireNonNull(request, "BlockRefillRequest cannot be null");
        Objects.requireNonNull(executor, "BlockRefillExecutor cannot be null");

        BlockRefillSettings settings = settingsProvider.get();
        if (settings == null || !settings.enabled()) {
            return false;
        }

        OptionalInt replacementSlot = request.findReplacementSlotIndex();
        if (replacementSlot.isEmpty()) {
            return false;
        }

        BlockRefillAction action = new BlockRefillAction(
                request.playerId(),
                request.handSlotType(),
                replacementSlot.getAsInt(),
                request.targetHotbarSlot()
        );
        executor.executeRefill(action);
        return true;
    }
}
