package io.github.iutils.toolswap.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolSwapRequestTest {

    private static ToolSwapRequest request(ToolType type, List<ToolSlot> candidates) {
        return new ToolSwapRequest(UUID.randomUUID(), ToolHand.MAIN_HAND, 0, type, candidates);
    }

    private static ToolSlot pick(int index, ToolMaterial material, int remaining) {
        return new ToolSlot(index, ToolType.PICKAXE, material, remaining);
    }

    @Test
    void prefersNetheriteOverEveryOtherMaterial() {
        List<ToolSlot> slots = List.of(
                pick(1, ToolMaterial.WOODEN, 50), pick(2, ToolMaterial.STONE, 50),
                pick(3, ToolMaterial.COPPER, 50), pick(4, ToolMaterial.IRON, 50),
                pick(5, ToolMaterial.GOLDEN, 50), pick(6, ToolMaterial.DIAMOND, 50),
                pick(7, ToolMaterial.NETHERITE, 50));
        assertEquals(7, request(ToolType.PICKAXE, slots).findBestReplacement().orElseThrow().index());
    }

    @Test
    void followsFullPriorityOrder() {
        List<ToolMaterial> order = List.of(ToolMaterial.NETHERITE, ToolMaterial.DIAMOND, ToolMaterial.GOLDEN,
                ToolMaterial.IRON, ToolMaterial.COPPER, ToolMaterial.STONE, ToolMaterial.WOODEN);
        for (int i = 0; i < order.size() - 1; i++) {
            assertTrue(order.get(i).priority() < order.get(i + 1).priority());
        }
    }

    @Test
    void ignoresOtherToolTypes() {
        List<ToolSlot> slots = List.of(new ToolSlot(1, ToolType.AXE, ToolMaterial.NETHERITE, 100));
        assertTrue(request(ToolType.PICKAXE, slots).findBestReplacement().isEmpty());
    }

    @Test
    void ignoresTheBreakingSlotItself() {
        assertTrue(request(ToolType.PICKAXE, List.of(pick(0, ToolMaterial.NETHERITE, 100)))
                .findBestReplacement().isEmpty());
    }

    @Test
    void ignoresAlmostBrokenCandidates() {
        assertTrue(request(ToolType.PICKAXE, List.of(pick(1, ToolMaterial.NETHERITE, 1)))
                .findBestReplacement().isEmpty());
        Optional<ToolSlot> best = request(ToolType.PICKAXE,
                List.of(pick(1, ToolMaterial.NETHERITE, 1), pick(2, ToolMaterial.WOODEN, 2))).findBestReplacement();
        assertEquals(2, best.orElseThrow().index());
    }

    @Test
    void tiesBrokenByRemainingDurability() {
        List<ToolSlot> slots = List.of(pick(1, ToolMaterial.IRON, 10), pick(2, ToolMaterial.IRON, 200));
        assertEquals(2, request(ToolType.PICKAXE, slots).findBestReplacement().orElseThrow().index());
    }

    @Test
    void offHandMayReplaceWithSameIndexAsTargetSlot() {
        ToolSwapRequest req = new ToolSwapRequest(UUID.randomUUID(), ToolHand.OFF_HAND, 0,
                ToolType.PICKAXE, List.of(pick(0, ToolMaterial.IRON, 50)));
        assertEquals(0, req.findBestReplacement().orElseThrow().index());
    }

    @Test
    void noCandidatesReturnsEmpty() {
        assertTrue(request(ToolType.SWORD, List.of()).findBestReplacement().isEmpty());
    }
}