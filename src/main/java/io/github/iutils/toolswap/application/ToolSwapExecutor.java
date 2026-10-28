package io.github.iutils.toolswap.application;

import io.github.iutils.toolswap.domain.ToolSwapAction;

/**
 * Output port that applies a resolved {@link ToolSwapAction} to a player's inventory.
 */
@FunctionalInterface
public interface ToolSwapExecutor {

    /**
     * Executes the slot swap.
     *
     * @param action the resolved swap action
     */
    void executeSwap(ToolSwapAction action);
}