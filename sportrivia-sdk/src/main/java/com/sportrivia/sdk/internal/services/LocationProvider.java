package com.sportrivia.sdk.internal.services;

import com.sportrivia.sdk.internal.models.LocationResult;

/**
 * Abstraction over location capture so GameEngine stays free of android.*
 * dependencies (and unit-testable on a plain JVM). The production
 * implementation is {@link LocationService}.
 */
public interface LocationProvider {

    /**
     * Best-effort capture: returns immediately when a fix or a definite
     * denial is already known, otherwise blocks the calling (background)
     * thread up to {@code timeoutMs}. Never throws.
     */
    LocationResult await(long timeoutMs);
}
