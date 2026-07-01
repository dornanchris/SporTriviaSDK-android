package com.sportrivia.sdk.public_api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.sportrivia.sdk.internal.ui.UserInfoActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main entry point for the SporTrivia SDK.
 */
public final class SporTriviaSDK {

    private static SporTriviaConfiguration configuration;
    private static SporTriviaDelegate activeDelegate;

    private static final String PREFS_NAME = "sportrivia_sdk";
    private static final String KEY_PENDING_GAME_ID = "pendingGameId";
    private static final String KEY_PENDING_SPORT = "pendingSport";
    private static final ExecutorService claimExecutor = Executors.newSingleThreadExecutor();

    private SporTriviaSDK() {}

    /** Callback for {@link #claimPendingGame(Context, PendingGameCallback)}. */
    public interface PendingGameCallback {
        void onResult(SporTriviaPendingGame game);
    }

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

    // ------------------------------------------------------------------ Deep links

    /**
     * Handle an incoming deep link and, if valid, launch the custom game.
     *
     * Recognizes links of the form
     * {@code sportrivia-<partnerId>://game?gameId=<id>&sport=<code>} produced by
     * the SporTrivia SDK redirect page. The game is also saved so it survives a
     * cold launch (see {@link #getPendingGame(Context)}).
     *
     * @return {@code true} if the URI was a valid SporTrivia game link.
     */
    public static boolean handleDeepLink(Context context, Uri uri, SporTriviaDelegate delegate) {
        SporTriviaPendingGame game = parseGameLink(uri);
        if (game == null) {
            return false;
        }
        savePendingGame(context, game);
        if (configuration != null) {
            launchCustomGame(context, game.getGameId(), game.getSport(), delegate);
        }
        return true;
    }

    /** Parse a SporTrivia game deep link without launching or saving it. */
    public static SporTriviaPendingGame parseGameLink(Uri uri) {
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            return null;
        }
        scheme = scheme.toLowerCase();
        if (!scheme.equals("sportrivia") && !scheme.startsWith("sportrivia-")) {
            return null;
        }

        String gameId = firstNonEmpty(uri.getQueryParameter("gameId"), uri.getQueryParameter("game_id"));
        if (gameId == null && uri.getPathSegments() != null && !uri.getPathSegments().isEmpty()) {
            gameId = uri.getPathSegments().get(uri.getPathSegments().size() - 1);
        }
        String sportCode = firstNonEmpty(uri.getQueryParameter("sport"), uri.getQueryParameter("info"));

        if (gameId == null || gameId.trim().isEmpty() || sportCode == null) {
            return null;
        }
        try {
            Sport sport = Sport.fromCode(sportCode.trim());
            return new SporTriviaPendingGame(gameId.trim(), sport);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** The game most recently saved from a deep link (or claimed), or null. */
    public static SporTriviaPendingGame getPendingGame(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String gameId = prefs.getString(KEY_PENDING_GAME_ID, null);
        String sportCode = prefs.getString(KEY_PENDING_SPORT, null);
        if (gameId == null || gameId.isEmpty() || sportCode == null) {
            return null;
        }
        try {
            return new SporTriviaPendingGame(gameId, Sport.fromCode(sportCode));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Persist a pending game so it survives an app restart / cold launch. */
    public static void savePendingGame(Context context, SporTriviaPendingGame game) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PENDING_GAME_ID, game.getGameId())
                .putString(KEY_PENDING_SPORT, game.getSport().getCode())
                .apply();
    }

    /** Clear any saved pending game. Call after launching it. */
    public static void clearPendingGame(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_PENDING_GAME_ID)
                .remove(KEY_PENDING_SPORT)
                .apply();
    }

    /**
     * Ask the SporTrivia redirect service whether a game was saved off for this
     * partner before the app was installed (deferred deep linking).
     *
     * Requires {@code partnerId} and {@code redirectBaseUrl} on the
     * configuration. On success the game is saved as the pending game. The
     * callback is invoked on the main thread; the argument is null if nothing
     * was found or claiming isn't configured.
     */
    public static void claimPendingGame(Context context, PendingGameCallback callback) {
        Handler main = new Handler(Looper.getMainLooper());
        final Context appContext = context.getApplicationContext();

        if (configuration == null
                || isEmpty(configuration.getPartnerId())
                || isEmpty(configuration.getRedirectBaseUrl())) {
            main.post(() -> callback.onResult(null));
            return;
        }

        final String partnerId = configuration.getPartnerId();
        final String baseUrl = configuration.getRedirectBaseUrl().replaceAll("/+$", "");

        claimExecutor.execute(() -> {
            SporTriviaPendingGame result = null;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(baseUrl + "/api/sdk/claim-game");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject body = new JSONObject();
                body.put("partner_id", partnerId);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                if (conn.getResponseCode() == 200) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    }
                    JSONObject json = new JSONObject(sb.toString());
                    if (json.optBoolean("found", false)) {
                        String gameId = json.optString("game_id", "");
                        String sportCode = json.optString("sport", "");
                        if (!gameId.isEmpty() && !sportCode.isEmpty()) {
                            Sport sport = Sport.fromCode(sportCode);
                            result = new SporTriviaPendingGame(gameId, sport);
                            savePendingGame(appContext, result);
                        }
                    }
                }
            } catch (Exception ignored) {
                // Best-effort: treat any failure as "no game found".
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            final SporTriviaPendingGame finalResult = result;
            main.post(() -> callback.onResult(finalResult));
        });
    }

    private static String firstNonEmpty(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a;
        }
        if (b != null && !b.trim().isEmpty()) {
            return b;
        }
        return null;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
