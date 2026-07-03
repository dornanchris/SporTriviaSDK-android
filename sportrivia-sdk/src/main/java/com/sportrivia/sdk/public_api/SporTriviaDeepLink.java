package com.sportrivia.sdk.public_api;

import android.net.Uri;

import java.util.Locale;

/**
 * A parsed SporTrivia deep link.
 *
 * <p>SporTrivia QR codes launch partner apps with a URL of the form:
 *
 * <pre>yourscheme://sportrivia/custom/&lt;gameId&gt;?info=&lt;sportCode&gt;</pre>
 *
 * where {@code yourscheme} is the custom URL scheme your app registers in its
 * {@code AndroidManifest.xml} intent filter. The path and query are supplied
 * by SporTrivia and identify the question to load.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * SporTriviaDeepLink link = SporTriviaDeepLink.parse(intent.getData());
 * if (link != null) {
 *     SporTriviaSDK.launchCustomGame(context, link.getGameId(), link.getSport(), delegate);
 * }
 * }</pre>
 */
public final class SporTriviaDeepLink {

    private final String gameId;
    private final Sport sport;

    private SporTriviaDeepLink(String gameId, Sport sport) {
        this.gameId = gameId;
        this.sport = sport;
    }

    /** The custom game identifier (S3 file name, e.g. "NYI_Top5A"). */
    public String getGameId() {
        return gameId;
    }

    /** The sport/league for the game. */
    public Sport getSport() {
        return sport;
    }

    /**
     * Parses a SporTrivia deep link from an incoming intent's data Uri.
     *
     * @return the parsed link, or {@code null} if the Uri is not a SporTrivia
     *         game link or the sport code is unknown.
     */
    public static SporTriviaDeepLink parse(Uri uri) {
        if (uri == null) {
            return null;
        }
        return parse(uri.toString());
    }

    /**
     * Parses a SporTrivia deep link from a raw URL string. Accepts both the
     * partner form ({@code scheme://sportrivia/custom/<gameId>}) and the
     * SporTrivia app form ({@code sportrivia://custom/<gameId>}).
     */
    public static SporTriviaDeepLink parse(String url) {
        if (url == null) {
            return null;
        }

        String value = url.trim();
        int schemeSeparator = value.indexOf("://");
        if (schemeSeparator < 0) {
            return null;
        }

        String remainder = value.substring(schemeSeparator + 3);
        int fragmentIndex = remainder.indexOf('#');
        if (fragmentIndex >= 0) {
            remainder = remainder.substring(0, fragmentIndex);
        }
        String query = "";
        int queryIndex = remainder.indexOf('?');
        if (queryIndex >= 0) {
            query = remainder.substring(queryIndex + 1);
            remainder = remainder.substring(0, queryIndex);
        }

        String gameId = null;
        String[] segments = remainder.split("/");
        for (int i = 0; i < segments.length - 1; i++) {
            if (segments[i].equalsIgnoreCase("custom")) {
                gameId = segments[i + 1];
                break;
            }
        }
        if (gameId == null || gameId.isEmpty()) {
            return null;
        }
        if (gameId.toLowerCase(Locale.US).endsWith(".json")) {
            gameId = gameId.substring(0, gameId.length() - 5);
        }
        if (gameId.isEmpty()) {
            return null;
        }

        String sportCode = null;
        for (String param : query.split("&")) {
            int equalsIndex = param.indexOf('=');
            if (equalsIndex > 0 && param.substring(0, equalsIndex).equalsIgnoreCase("info")) {
                sportCode = param.substring(equalsIndex + 1);
                break;
            }
        }
        if (sportCode == null || sportCode.trim().isEmpty()) {
            return null;
        }

        try {
            return new SporTriviaDeepLink(gameId, Sport.fromCode(sportCode));
        } catch (IllegalArgumentException unknownSport) {
            return null;
        }
    }
}
