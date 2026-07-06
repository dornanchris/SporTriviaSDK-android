package com.sportrivia.sdk.internal.models;

/**
 * Outcome of a device-location capture attempt for the results upload.
 *
 * <p>Pure Java (no android.* imports) so game logic and the JSON formatter
 * stay unit-testable on a plain JVM. {@code latitude}/{@code longitude}/
 * {@code accuracyMeters}/{@code capturedAt} are non-null iff {@code status}
 * is {@link #STATUS_GRANTED}.
 */
public final class LocationResult {

    public static final String STATUS_GRANTED = "granted";
    public static final String STATUS_DENIED = "denied";
    public static final String STATUS_UNAVAILABLE = "unavailable";
    public static final String STATUS_TIMEOUT = "timeout";

    public final String status;
    public final Double latitude;
    public final Double longitude;
    public final Double accuracyMeters;
    /** UTC timestamp of the fix, formatted like submitted_at. */
    public final String capturedAt;

    private LocationResult(String status, Double latitude, Double longitude,
                           Double accuracyMeters, String capturedAt) {
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracyMeters = accuracyMeters;
        this.capturedAt = capturedAt;
    }

    public static LocationResult granted(double latitude, double longitude,
                                         double accuracyMeters, String capturedAt) {
        return new LocationResult(STATUS_GRANTED, latitude, longitude, accuracyMeters, capturedAt);
    }

    public static LocationResult denied() {
        return new LocationResult(STATUS_DENIED, null, null, null, null);
    }

    public static LocationResult unavailable() {
        return new LocationResult(STATUS_UNAVAILABLE, null, null, null, null);
    }

    public static LocationResult timeout() {
        return new LocationResult(STATUS_TIMEOUT, null, null, null, null);
    }

    public boolean hasFix() {
        return STATUS_GRANTED.equals(status) && latitude != null && longitude != null;
    }
}
