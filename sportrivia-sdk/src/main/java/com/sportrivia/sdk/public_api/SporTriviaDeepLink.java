package com.sportrivia.sdk.public_api;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
 * <p>Apps that opt into verified App Links receive the QR's https URL instead:
 *
 * <pre>https://&lt;sportrivia-host&gt;/sdk/r/&lt;team&gt;/&lt;questionId&gt;?game=&lt;gameId&gt;&amp;info=&lt;sportCode&gt;</pre>
 *
 * {@code parse} understands both forms, so one handler covers scheme launches
 * and App Link launches alike.
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

        // Verified App Link form carries the game id as a query parameter;
        // scheme deep links carry it as the path segment after "custom".
        String gameId = queryParam(query, "game");
        if (gameId == null || gameId.isEmpty()) {
            String[] segments = remainder.split("/");
            for (int i = 0; i < segments.length - 1; i++) {
                if (segments[i].equalsIgnoreCase("custom")) {
                    gameId = segments[i + 1];
                    break;
                }
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

        String sportCode = queryParam(query, "info");
        if (sportCode == null || sportCode.trim().isEmpty()) {
            return null;
        }

        try {
            return new SporTriviaDeepLink(gameId, Sport.fromCode(sportCode));
        } catch (IllegalArgumentException unknownSport) {
            return null;
        }
    }

    /**
     * Checks the system clipboard for a SporTrivia deep link URL.
     *
     * <p>The SporTrivia redirect page copies the deep link to the clipboard
     * before sending the user to the Play Store. Call this method once your
     * launcher Activity has input focus (for example from
     * {@code onWindowFocusChanged(true)}) to recover the deep link after the
     * user installs and opens the app for the first time. Android only permits
     * clipboard reads while your app is focused, so calling it from
     * {@code onCreate} is denied by the OS and returns {@code null}.
     *
     * <p>If a valid deep link is found it is consumed (the clipboard is
     * cleared) so it will not trigger again on subsequent launches.
     *
     * @param context an Android Context (Activity or Application)
     * @return a parsed {@link SporTriviaDeepLink} if the clipboard contained
     *         a valid game link, or {@code null} otherwise
     */
    public static SporTriviaDeepLink checkClipboard(Context context) {
        try {
            ClipboardManager clipboard =
                    (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null || !clipboard.hasPrimaryClip()) {
                SporTriviaLogger.debug("Clipboard: empty or unavailable");
                return null;
            }

            ClipData clip = clipboard.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) {
                return null;
            }

            String text = clip.getItemAt(0).coerceToText(context).toString();
            if (text.isEmpty()) {
                return null;
            }

            SporTriviaDeepLink link = parse(text);
            if (link == null) {
                SporTriviaLogger.debug("Clipboard: not a SporTrivia deep link");
                return null;
            }

            SporTriviaLogger.info("Clipboard: found deep link gameId=" +
                    link.getGameId() + ", sport=" + link.getSport().getCode());
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
            return link;
        } catch (Exception e) {
            SporTriviaLogger.error("Clipboard check failed: " + e.getMessage());
            return null;
        }
    }

    private static String queryParam(String query, String name) {
        for (String param : query.split("&")) {
            int equalsIndex = param.indexOf('=');
            if (equalsIndex > 0 && param.substring(0, equalsIndex).equalsIgnoreCase(name)) {
                return param.substring(equalsIndex + 1);
            }
        }
        return null;
    }
}
