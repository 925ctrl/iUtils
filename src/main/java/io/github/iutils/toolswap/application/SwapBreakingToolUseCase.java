package io.github.iutils.toolswap.application;

import io.github.iutils.toolswap.domain.ToolSlot;
import io.github.iutils.toolswap.domain.ToolSwapAction;
import io.github.iutils.toolswap.domain.ToolSwapRequest;
import io.github.iutils.toolswap.domain.ToolSwapSettings;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Use case that swaps a breaking tool for the best equivalent tool in the inventory.
 */
public final class SwapBreakingToolUseCase {

    private final Supplier<ToolSwapSettings> settingsProvider;

    public SwapBreakingToolUseCase(Supplier<ToolSwapSettings> settingsProvider) {
        this.settingsProvider = Objects.requireNonNull(settingsProvider, "settingsProvider cannot be null");
    }

    /**
     * @return whether the feature is currently enabled
     */
    public boolean isEnabled() {
        ToolSwapSettings settings = settingsProvider.get();
        return settings != null && settings.enabled();
    }

    /**
     * Evaluates the request and executes the swap if a replacement exists.
     *
     * @param request  the breaking tool context
     * @param executor the executor that mutates the inventory
     * @return {@code true} if a swap was executed, {@code false} otherwise
     */
    public boolean execute(ToolSwapRequest request, ToolSwapExecutor executor) {
        Objects.requireNonNull(request, "ToolSwapRequest cannot be null");
        Objects.requireNonNull(executor, "ToolSwapExecutor cannot be null");
        if (!isEnabled()) {
            return false;
        }
        Optional<ToolSlot> replacement = request.findBestReplacement();
        if (replacement.isEmpty()) {
            return false;
        }
        executor.executeSwap(new ToolSwapAction(
                request.playerId(),
                request.hand(),
                request.targetSlotIndex(),
                replacement.get().index()
        ));
        return true;
    }
}