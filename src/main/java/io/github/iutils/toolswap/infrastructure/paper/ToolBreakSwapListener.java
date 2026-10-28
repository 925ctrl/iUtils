package io.github.iutils.toolswap.infrastructure.paper;

import io.github.iutils.toolswap.application.SwapBreakingToolUseCase;
import io.github.iutils.toolswap.domain.ToolHand;
import io.github.iutils.toolswap.domain.ToolMaterial;
import io.github.iutils.toolswap.domain.ToolSlot;
import io.github.iutils.toolswap.domain.ToolSwapAction;
import io.github.iutils.toolswap.domain.ToolSwapRequest;
import io.github.iutils.toolswap.domain.ToolType;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Paper adapter that prevents a tool from breaking by swapping it with an equivalent
 * tool from the player's inventory.
 */
public final class ToolBreakSwapListener implements Listener {

    private static final int STORAGE_SIZE = 36;

    private final SwapBreakingToolUseCase useCase;

    public ToolBreakSwapListener(SwapBreakingToolUseCase useCase) {
        this.useCase = Objects.requireNonNull(useCase, "useCase cannot be null");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (!useCase.isEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        GameMode mode = player.getGameMode();
        if (mode != GameMode.SURVIVAL && mode != GameMode.ADVENTURE) {
            return;
        }

        ItemStack item = event.getItem();
        Optional<ToolType> type = ToolType.fromMaterialName(item.getType().name());
        if (type.isEmpty() || !(item.getItemMeta() instanceof Damageable damageable)) {
            return;
        }
        int maxDurability = item.getType().getMaxDurability();
        if (maxDurability <= 0 || damageable.getDamage() + event.getDamage() < maxDurability) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ToolHand hand;
        int heldSlot = inventory.getHeldItemSlot();
        if (item.equals(inventory.getItem(heldSlot))) {
            hand = ToolHand.MAIN_HAND;
        } else if (item.equals(inventory.getItemInOffHand())) {
            hand = ToolHand.OFF_HAND;
        } else {
            return;
        }

        ToolSwapRequest request = new ToolSwapRequest(
                player.getUniqueId(), hand, heldSlot, type.get(), snapshot(inventory, type.get()));

        boolean swapped = useCase.execute(request, action -> applySwap(inventory, action));
        if (swapped) {
            event.setCancelled(true);
        }
    }

    private List<ToolSlot> snapshot(PlayerInventory inventory, ToolType type) {
        ItemStack[] contents = inventory.getStorageContents();
        List<ToolSlot> slots = new ArrayList<>();
        for (int index = 0; index < Math.min(contents.length, STORAGE_SIZE); index++) {
            ItemStack stack = contents[index];
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            Optional<ToolType> stackType = ToolType.fromMaterialName(stack.getType().name());
            Optional<ToolMaterial> material = ToolMaterial.fromMaterialName(stack.getType().name());
            if (stackType.isEmpty() || stackType.get() != type || material.isEmpty()
                    || !(stack.getItemMeta() instanceof Damageable damageable)) {
                continue;
            }
            int remaining = stack.getType().getMaxDurability() - damageable.getDamage();
            slots.add(new ToolSlot(index, type, material.get(), remaining));
        }
        return slots;
    }

    private void applySwap(PlayerInventory inventory, ToolSwapAction action) {
        ItemStack replacement = inventory.getItem(action.replacementSlotIndex());
        switch (action.hand()) {
            case MAIN_HAND -> {
                ItemStack broken = inventory.getItem(action.targetSlotIndex());
                inventory.setItem(action.targetSlotIndex(), replacement);
                inventory.setItem(action.replacementSlotIndex(), broken);
            }
            case OFF_HAND -> {
                ItemStack broken = inventory.getItemInOffHand();
                inventory.setItemInOffHand(replacement);
                inventory.setItem(action.replacementSlotIndex(), broken);
            }
        }
    }
}