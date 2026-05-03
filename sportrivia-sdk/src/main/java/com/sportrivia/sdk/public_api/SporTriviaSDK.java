package com.sportrivia.sdk.public_api;

import android.content.Context;
import android.content.Intent;

import com.sportrivia.sdk.internal.ui.UserInfoActivity;

/**
 * Main entry point for the SporTrivia SDK.
 */
public final class SporTriviaSDK {

    private static SporTriviaConfiguration configuration;
    private static SporTriviaDelegate activeDelegate;

    private SporTriviaSDK() {}

    public static void configure(SporTriviaConfiguration config) {
        configuration = config;
    }

    public static void launchCustomGame(Context context, String gameId, Sport sport,
                                         SporTriviaDelegate delegate) {
        if (configuration == null) {
            throw new IllegalStateException("SporTriviaSDK.configure() must be called before launching games.");
        }

        activeDelegate = delegate;

        Intent intent = new Intent(context, UserInfoActivity.class);
        intent.putExtra("gameId", gameId);
        intent.putExtra("sport", sport.getCode());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static SporTriviaConfiguration getConfiguration() {
        return configuration;
    }

    public static SporTriviaDelegate getActiveDelegate() {
        return activeDelegate;
    }

    public static SporTriviaTheme getTheme() {
        if (configuration != null && configuration.getTheme() != null) {
            return configuration.getTheme();
        }
        return new SporTriviaTheme();
    }

    public static void clearActiveDelegate() {
        activeDelegate = null;
    }
}
