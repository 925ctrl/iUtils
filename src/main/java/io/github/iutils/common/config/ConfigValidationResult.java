package io.github.iutils.common.config;

import java.util.List;
import java.util.Objects;

/**
 * Holds the outcome of validating a configuration structure.
 *
 * @param config the resulting configuration (using safe fallback defaults for invalid fields)
 * @param errors the list of human-readable validation error messages in Spanish
 */
public record ConfigValidationResult(
        PluginConfig config,
        List<String> errors
) {

    public ConfigValidationResult {
        Objects.requireNonNull(config, "config cannot be null");
        Objects.requireNonNull(errors, "errors cannot be null");
        errors = List.copyOf(errors);
    }

    /**
     * Indicates whether the configuration passed validation with zero errors.
     *
     * @return {@code true} if no validation errors occurred
     */
    public boolean isValid() {
        return errors.isEmpty();
    }
}
