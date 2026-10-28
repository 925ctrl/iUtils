package io.github.iutils.toolswap.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Tool materials ordered by replacement priority (lower priority value is preferred).
 */
public enum ToolMaterial {
    NETHERITE("NETHERITE_", 1),
    DIAMOND("DIAMOND_", 2),
    GOLDEN("GOLDEN_", 3),
    IRON("IRON_", 4),
    COPPER("COPPER_", 5),
    STONE("STONE_", 6),
    WOODEN("WOODEN_", 7);

    private final String prefix;
    private final int priority;

    ToolMaterial(String prefix, int priority) {
        this.prefix = prefix;
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }

    /**
     * Resolves the tool material from a material name such as {@code IRON_AXE}.
     *
     * @param materialName the material enum name
     * @return the matching tool material, or empty if unknown
     */
    public static Optional<ToolMaterial> fromMaterialName(String materialName) {
        Objects.requireNonNull(materialName, "materialName cannot be null");
        String name = materialName.toUpperCase(Locale.ROOT);
        for (ToolMaterial material : values()) {
            if (name.startsWith(material.prefix)) {
                return Optional.of(material);
            }
        }
        return Optional.empty();
    }
}