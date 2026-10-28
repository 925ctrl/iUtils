package io.github.iutils.toolswap.domain;

/**
 * Immutable domain configuration for the automatic tool swap feature.
 *
 * @param enabled whether breaking tools are swapped for an equivalent tool
 */
public record ToolSwapSettings(boolean enabled) {

    public static final boolean DEFAULT_ENABLED = true;

    public static ToolSwapSettings defaults() {
        return new ToolSwapSettings(DEFAULT_ENABLED);
    }
}