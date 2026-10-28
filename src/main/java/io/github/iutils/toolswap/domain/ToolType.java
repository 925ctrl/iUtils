package io.github.iutils.toolswap.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Supported tool categories for the tool swap feature.
 */
public enum ToolType {
    SWORD("_SWORD"),
    SHOVEL("_SHOVEL"),
    PICKAXE("_PICKAXE"),
    AXE("_AXE"),
    HOE("_HOE");

    private final String suffix;

    ToolType(String suffix) {
        this.suffix = suffix;
    }

    /**
     * Resolves the tool type from a material name such as {@code DIAMOND_PICKAXE}.
     *
     * @param materialName the material enum name
     * @return the matching tool type, or empty when the material is not a supported tool
     */
    public static Optional<ToolType> fromMaterialName(String materialName) {
        Objects.requireNonNull(materialName, "materialName cannot be null");
        String name = materialName.toUpperCase(Locale.ROOT);
        for (ToolType type : values()) {
            if (name.endsWith(type.suffix) && ToolMaterial.fromMaterialName(name).isPresent()) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}