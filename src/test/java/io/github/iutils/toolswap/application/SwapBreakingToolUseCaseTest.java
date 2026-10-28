package io.github.iutils.toolswap.application;

import io.github.iutils.toolswap.domain.ToolHand;
import io.github.iutils.toolswap.domain.ToolMaterial;
import io.github.iutils.toolswap.domain.ToolSlot;
import io.github.iutils.toolswap.domain.ToolSwapAction;
import io.github.iutils.toolswap.domain.ToolSwapRequest;
import io.github.iutils.toolswap.domain.ToolSwapSettings;
import io.github.iutils.toolswap.domain.ToolType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwapBreakingToolUseCaseTest {

    private final UUID playerId = UUID.randomUUID();

    private ToolSwapRequest requestWithIron() {
        return new ToolSwapRequest(playerId, ToolHand.MAIN_HAND, 2, ToolType.PICKAXE,
                List.of(new ToolSlot(10, ToolType.PICKAXE, ToolMaterial.IRON, 100)));
    }

    @Test
    void executesSwapWhenEnabledAndReplacementExists() {
        SwapBreakingToolUseCase useCase = new SwapBreakingToolUseCase(() -> new ToolSwapSettings(true));
        List<ToolSwapAction> actions = new ArrayList<>();

        assertTrue(useCase.execute(requestWithIron(), actions::add));
        assertEquals(new ToolSwapAction(playerId, ToolHand.MAIN_HAND, 2, 10), actions.get(0));
    }

    @Test
    void doesNothingWhenDisabled() {
        SwapBreakingToolUseCase useCase = new SwapBreakingToolUseCase(() -> new ToolSwapSettings(false));
        List<ToolSwapAction> actions = new ArrayList<>();

        assertFalse(useCase.execute(requestWithIron(), actions::add));
        assertTrue(actions.isEmpty());
    }

    @Test
    void returnsFalseWithoutReplacement() {
        SwapBreakingToolUseCase useCase = new SwapBreakingToolUseCase(() -> new ToolSwapSettings(true));
        ToolSwapRequest request = new ToolSwapRequest(playerId, ToolHand.MAIN_HAND, 2, ToolType.AXE, List.of());
        assertFalse(useCase.execute(request, action -> { }));
    }

    @Test
    void reflectsSettingsChanges() {
        AtomicReference<ToolSwapSettings> settings = new AtomicReference<>(new ToolSwapSettings(false));
        SwapBreakingToolUseCase useCase = new SwapBreakingToolUseCase(settings::get);
        assertFalse(useCase.execute(requestWithIron(), action -> { }));
        settings.set(new ToolSwapSettings(true));
        assertTrue(useCase.execute(requestWithIron(), action -> { }));
    }
}