package com.sportrivia.sdk.internal.services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sportrivia.sdk.internal.models.LocationResult;
import com.sportrivia.sdk.public_api.SporTriviaLogger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Captures the device location for game-result uploads using only the
 * framework {@link LocationManager} — no Play Services dependency.
 *
 * <p>Flow: {@link #requestPermissionIfNeeded(Activity)} fires the runtime
 * permission prompt on the player-info screen and starts acquiring a fix, so
 * by the time the game ends {@link #await(long)} usually returns instantly.
 * Strictly best-effort: denial, missing providers, or a slow fix never block
 * or fail the upload beyond the timeout.
 */
public class LocationService implements LocationProvider {

    public static final int PERMISSION_REQUEST_CODE = 7341;

    private final Context appContext;
    private final AtomicReference<LocationResult> fix = new AtomicReference<>();
    private final AtomicReference<String> settledStatus = new AtomicReference<>();
    private final CountDownLatch settled = new CountDownLatch(1);

    public LocationService(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /** Ask for location permission (if needed) and start warming up a fix. */
    public void requestPermissionIfNeeded(Activity activity) {
        if (hasPermission()) {
            warmUp();
            return;
        }
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSION_REQUEST_CODE
        );
    }

    /** Forward from the hosting activity's onRequestPermissionsResult. */
    public void onPermissionResult(int requestCode) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return;
        }
        if (hasPermission()) {
            warmUp();
        } else {
            settle(LocationResult.denied());
        }
    }

    /** Start acquiring a location fix (permission already granted). */
    public void warmUp() {
        LocationManager manager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            settle(LocationResult.unavailable());
            return;
        }
        try {
            // Seed with the last known fix so we always have something,
            // then request one fresh precise fix.
            Location best = null;
            for (String provider : manager.getProviders(true)) {
                Location last = manager.getLastKnownLocation(provider);
                if (last != null && (best == null || last.getAccuracy() < best.getAccuracy())) {
                    best = last;
                }
            }
            if (best != null) {
                deliver(best);
            }

            String provider = pickProvider(manager);
            if (provider == null) {
                if (fix.get() == null) {
                    settle(LocationResult.unavailable());
                }
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                manager.getCurrentLocation(
                        provider,
                        null,
                        Executors.newSingleThreadExecutor(),
                        location -> {
                            if (location != null) {
                                deliver(location);
                            } else if (fix.get() == null) {
                                settle(LocationResult.unavailable());
                            }
                        }
                );
            } else {
                manager.requestSingleUpdate(provider, location -> {
                    if (location != null) {
                        deliver(location);
                    }
                }, appContext.getMainLooper());
            }
        } catch (SecurityException e) {
            SporTriviaLogger.warning("Location permission revoked mid-flight: " + e.getMessage());
            settle(LocationResult.denied());
        } catch (Exception e) {
            SporTriviaLogger.warning("Location capture failed: " + e.getMessage() + " — uploading without location");
            if (fix.get() == null) {
                settle(LocationResult.unavailable());
            }
        }
    }

    @Override
    public LocationResult await(long timeoutMs) {
        LocationResult current = fix.get();
        if (current != null) {
            return current;
        }
        String status = settledStatus.get();
        if (status != null) {
            return statusResult(status);
        }
        try {
            settled.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        current = fix.get();
        if (current != null) {
            return current;
        }
        status = settledStatus.get();
        if (status != null) {
            return statusResult(status);
        }
        // Permission granted but no fix arrived in time vs never prompted.
        return hasPermission() ? LocationResult.timeout() : LocationResult.unavailable();
    }

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private String pickProvider(LocationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && manager.getProviders(true).contains(LocationManager.FUSED_PROVIDER)) {
            return LocationManager.FUSED_PROVIDER;
        }
        if (manager.getProviders(true).contains(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        }
        if (manager.getProviders(true).contains(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }
        return null;
    }

    private void deliver(Location location) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        fix.set(LocationResult.granted(
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracy(),
                format.format(new Date(location.getTime()))
        ));
        settledStatus.compareAndSet(null, LocationResult.STATUS_GRANTED);
        settled.countDown();
    }

    private void settle(LocationResult result) {
        settledStatus.compareAndSet(null, result.status);
        settled.countDown();
    }

    private static LocationResult statusResult(String status) {
        switch (status) {
            case LocationResult.STATUS_DENIED:
                return LocationResult.denied();
            case LocationResult.STATUS_TIMEOUT:
                return LocationResult.timeout();
            default:
                return LocationResult.unavailable();
        }
    }
}
