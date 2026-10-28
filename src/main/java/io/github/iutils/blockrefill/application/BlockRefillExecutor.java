package io.github.iutils.blockrefill.application;

import io.github.iutils.blockrefill.domain.BlockRefillAction;

/**
 * Output port responsible for applying a resolved {@link BlockRefillAction} to a player's inventory.
 */
@FunctionalInterface
public interface BlockRefillExecutor {

    /**
     * Executes the inventory slot transfer defined by the action.
     *
     * @param action the resolved domain block refill action
     */
    void executeRefill(BlockRefillAction action);
}
