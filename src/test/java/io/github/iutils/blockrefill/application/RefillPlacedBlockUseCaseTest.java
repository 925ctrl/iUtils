package io.github.iutils.blockrefill.application;

import io.github.iutils.blockrefill.domain.BlockRefillAction;
import io.github.iutils.blockrefill.domain.BlockRefillRequest;
import io.github.iutils.blockrefill.domain.BlockRefillSettings;
import io.github.iutils.blockrefill.domain.HandSlotType;
import io.github.iutils.blockrefill.domain.InventorySlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefillPlacedBlockUseCaseTest {

    @Test
    @DisplayName("Should prioritize backpack slots (9-35) over hotbar slots (0-8) when refilling")
    void shouldPrioritizeBackpackOverHotbar() {
        List<BlockRefillAction> actions = new ArrayList<>();
        RefillPlacedBlockUseCase useCase = new RefillPlacedBlockUseCase(
                BlockRefillSettings::defaults,
                actions::add
        );

        UUID playerId = UUID.randomUUID();
        BlockRefillRequest request = new BlockRefillRequest(
                playerId,
                HandSlotType.MAIN_HAND,
                2,
                "STONE",
                true,
                List.of(
                        new InventorySlot(0, "STONE", 64, true),
                        new InventorySlot(14, "STONE", 32, true),
                        new InventorySlot(9, "STONE", 16, true)
                )
        );

        boolean refilled = useCase.execute(request);

        assertTrue(refilled);
        assertEquals(1, actions.size());
        BlockRefillAction action = actions.getFirst();
        assertEquals(playerId, action.playerId());
        assertEquals(HandSlotType.MAIN_HAND, action.handSlotType());
        assertEquals(9, action.sourceSlotIndex(), "Should pick the first matching backpack slot (index 9)");
        assertEquals(2, action.targetHotbarSlot());
    }

    @Test
    @DisplayName("Should fallback to another hotbar slot (0-8) when no matching block exists in backpack")
    void shouldFallbackToHotbarSlotWhenBackpackHasNoMatch() {
        List<BlockRefillAction> actions = new ArrayList<>();
        RefillPlacedBlockUseCase useCase = new RefillPlacedBlockUseCase(
                BlockRefillSettings::defaults,
                actions::add
        );

        UUID playerId = UUID.randomUUID();
        BlockRefillRequest request = new BlockRefillRequest(
                playerId,
                HandSlotType.MAIN_HAND,
                1,
                "OAK_PLANKS",
                true,
                List.of(
                        new InventorySlot(10, "COBBLESTONE", 64, false),
                        new InventorySlot(5, "OAK_PLANKS", 48, true),
                        new InventorySlot(3, "OAK_PLANKS", 12, true)
                )
        );

        boolean refilled = useCase.execute(request);

        assertTrue(refilled);
        assertEquals(1, actions.size());
        assertEquals(3, actions.getFirst().sourceSlotIndex());
        assertEquals(1, actions.getFirst().targetHotbarSlot());
    }

    @Test
    @DisplayName("Should support refilling the off-hand slot from backpack or hotbar")
    void shouldSupportOffHandRefill() {
        List<BlockRefillAction> actions = new ArrayList<>();
        RefillPlacedBlockUseCase useCase = new RefillPlacedBlockUseCase(
                BlockRefillSettings::defaults,
                actions::add
        );

        UUID playerId = UUID.randomUUID();
        BlockRefillRequest request = new BlockRefillRequest(
                playerId,
                HandSlotType.OFF_HAND,
                0,
                "TORCH",
                true,
                List.of(
                        new InventorySlot(0, "TORCH", 20, true)
                )
        );

        boolean refilled = useCase.execute(request);

        assertTrue(refilled);
        assertEquals(1, actions.size());
        assertEquals(HandSlotType.OFF_HAND, actions.getFirst().handSlotType());
        assertEquals(0, actions.getFirst().sourceSlotIndex());
    }

    @Test
    @DisplayName("Should ignore blocks of the same material that do not match metadata similarity")
    void shouldIgnoreDissimilarItemStacks() {
        List<BlockRefillAction> actions = new ArrayList<>();
        RefillPlacedBlockUseCase useCase = new RefillPlacedBlockUseCase(
                BlockRefillSettings::defaults,
                actions::add
        );

        BlockRefillRequest request = new BlockRefillRequest(
                UUID.randomUUID(),
                HandSlotType.MAIN_HAND,
                0,
                "CHEST",
                true,
                List.of(
                        new InventorySlot(12, "CHEST", 10, false)
                )
        );

        boolean refilled = useCase.execute(request);

        assertFalse(refilled);
        assertTrue(actions.isEmpty());
    }

    @Test
    @DisplayName("Should not refill when target slot is no longer empty or feature is disabled")
    void shouldNotRefillWhenTargetNotEmptyOrFeatureDisabled() {
        List<BlockRefillAction> actions = new ArrayList<>();
        AtomicReference<BlockRefillSettings> settingsRef =
                new AtomicReference<>(new BlockRefillSettings(false));

        RefillPlacedBlockUseCase useCase = new RefillPlacedBlockUseCase(
                settingsRef::get,
                actions::add
        );

        BlockRefillRequest validRequest = new BlockRefillRequest(
                UUID.randomUUID(),
                HandSlotType.MAIN_HAND,
                4,
                "DIRT",
                true,
                List.of(new InventorySlot(15, "DIRT", 64, true))
        );

        assertFalse(useCase.execute(validRequest), "Should not refill while disabled");
        assertTrue(actions.isEmpty());

        settingsRef.set(new BlockRefillSettings(true));
        BlockRefillRequest nonEmptyTargetRequest = new BlockRefillRequest(
                UUID.randomUUID(),
                HandSlotType.MAIN_HAND,
                4,
                "DIRT",
                false,
                List.of(new InventorySlot(15, "DIRT", 64, true))
        );

        assertFalse(useCase.execute(nonEmptyTargetRequest), "Should not refill when target slot is not empty");
        assertTrue(actions.isEmpty());

        assertTrue(useCase.execute(validRequest), "Should refill immediately after enabling via reload");
        assertEquals(1, actions.size());
    }

    @Test
    @DisplayName("Should validate domain model invariants on InventorySlot, BlockRefillRequest, and BlockRefillAction")
    void shouldValidateDomainInvariants() {
        UUID playerId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> new InventorySlot(-1, "STONE", 1, true));
        assertThrows(IllegalArgumentException.class, () -> new InventorySlot(36, "STONE", 1, true));
        assertThrows(IllegalArgumentException.class, () -> new InventorySlot(0, "   ", 1, true));
        assertThrows(IllegalArgumentException.class, () -> new InventorySlot(0, "STONE", 0, true));

        assertThrows(
                IllegalArgumentException.class,
                () -> new BlockRefillRequest(playerId, HandSlotType.MAIN_HAND, 9, "STONE", true, List.of())
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new BlockRefillAction(playerId, HandSlotType.MAIN_HAND, 3, 3)
        );
    }
}
