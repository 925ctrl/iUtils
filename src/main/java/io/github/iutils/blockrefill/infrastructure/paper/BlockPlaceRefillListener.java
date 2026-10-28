package io.github.iutils.blockrefill.infrastructure.paper;

import io.github.iutils.blockrefill.application.RefillPlacedBlockUseCase;
import io.github.iutils.blockrefill.domain.BlockRefillAction;
import io.github.iutils.blockrefill.domain.BlockRefillRequest;
import io.github.iutils.blockrefill.domain.HandSlotType;
import io.github.iutils.blockrefill.domain.InventorySlot;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Paper infrastructure adapter that listens to {@link BlockPlaceEvent} and triggers
 * the {@link RefillPlacedBlockUseCase} when a player in Survival mode depletes their
 * active building block stack.
 */
public final class BlockPlaceRefillListener implements Listener {

    private final RefillPlacedBlockUseCase refillPlacedBlockUseCase;
    private final Consumer<Runnable> nextTickScheduler;

    public BlockPlaceRefillListener(
            RefillPlacedBlockUseCase refillPlacedBlockUseCase,
            Consumer<Runnable> nextTickScheduler
    ) {
        this.refillPlacedBlockUseCase = Objects.requireNonNull(
                refillPlacedBlockUseCase,
                "refillPlacedBlockUseCase cannot be null"
        );
        this.nextTickScheduler = Objects.requireNonNull(
                nextTickScheduler,
                "nextTickScheduler cannot be null"
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.canBuild()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        ItemStack placedItem = event.getItemInHand();
        if (placedItem.getType().isAir() || !placedItem.getType().isBlock() || placedItem.getAmount() != 1) {
            return;
        }

        EquipmentSlot equipmentSlot = event.getHand();
        HandSlotType handSlotType;
        if (equipmentSlot == EquipmentSlot.HAND) {
            handSlotType = HandSlotType.MAIN_HAND;
        } else if (equipmentSlot == EquipmentSlot.OFF_HAND) {
            handSlotType = HandSlotType.OFF_HAND;
        } else {
            return;
        }

        int targetHotbarSlot = player.getInventory().getHeldItemSlot();
        ItemStack referenceItem = placedItem.clone();
        referenceItem.setAmount(1);
        String placedBlockType = referenceItem.getType().name();

        nextTickScheduler.accept(() -> {
            if (!player.isOnline() || player.getGameMode() != GameMode.SURVIVAL) {
                return;
            }

            PlayerInventory inventory = player.getInventory();
            boolean targetSlotEmpty = isTargetSlotEmpty(inventory, handSlotType, targetHotbarSlot);
            if (!targetSlotEmpty) {
                return;
            }

            List<InventorySlot> availableSlots = snapshotStorageSlots(inventory, referenceItem);
            BlockRefillRequest request = new BlockRefillRequest(
                    player.getUniqueId(),
                    handSlotType,
                    targetHotbarSlot,
                    placedBlockType,
                    true,
                    availableSlots
            );

            refillPlacedBlockUseCase.execute(
                    request,
                    action -> applyInventoryTransfer(inventory, action)
            );
        });
    }

    private boolean isTargetSlotEmpty(
            PlayerInventory inventory,
            HandSlotType handSlotType,
            int targetHotbarSlot
    ) {
        ItemStack currentTargetItem = switch (handSlotType) {
            case MAIN_HAND -> inventory.getItem(targetHotbarSlot);
            case OFF_HAND -> inventory.getItemInOffHand();
        };
        return currentTargetItem == null
                || currentTargetItem.getType().isAir()
                || currentTargetItem.getAmount() <= 0;
    }

    private List<InventorySlot> snapshotStorageSlots(PlayerInventory inventory, ItemStack referenceItem) {
        ItemStack[] storageContents = inventory.getStorageContents();
        List<InventorySlot> slots = new ArrayList<>();
        int limit = Math.min(storageContents.length, InventorySlot.MAX_STORAGE_INDEX + 1);

        for (int index = 0; index < limit; index++) {
            ItemStack stack = storageContents[index];
            if (stack == null || stack.getType().isAir() || stack.getAmount() <= 0) {
                continue;
            }
            slots.add(new InventorySlot(
                    index,
                    stack.getType().name(),
                    stack.getAmount(),
                    stack.isSimilar(referenceItem)
            ));
        }

        return slots;
    }

    private void applyInventoryTransfer(PlayerInventory inventory, BlockRefillAction action) {
        ItemStack sourceStack = inventory.getItem(action.sourceSlotIndex());
        if (sourceStack == null || sourceStack.getType().isAir() || sourceStack.getAmount() <= 0) {
            return;
        }

        ItemStack stackToMove = sourceStack.clone();
        inventory.setItem(action.sourceSlotIndex(), null);

        switch (action.handSlotType()) {
            case MAIN_HAND -> inventory.setItem(action.targetHotbarSlot(), stackToMove);
            case OFF_HAND -> inventory.setItemInOffHand(stackToMove);
        }
    }
}
