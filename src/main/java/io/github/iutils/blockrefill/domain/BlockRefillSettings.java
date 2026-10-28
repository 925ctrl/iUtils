package io.github.iutils.blockrefill.domain;

/**
 * Immutable domain configuration for the automatic block refill feature.
 *
 * @param enabled whether automatic block refill on placement depletion is active
 */
public record BlockRefillSettings(boolean enabled) {

    public static final boolean DEFAULT_ENABLED = true;

    public static BlockRefillSettings defaults() {
        return new BlockRefillSettings(DEFAULT_ENABLED);
    }
}
