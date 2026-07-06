package com.sportrivia.sdk.internal.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a player with their ID, name, and years active.
 */
public class PlayerInfo {
    // Precompiled once — normalize() used to call String.replaceAll, which
    // compiles a fresh Pattern per invocation; at ~24k players per keystroke
    // that was the dominant cause of autocomplete lag.
    private static final Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]");

    public String playerId;
    public String playerName;
    public String yearsPlayed;

    /**
     * Search key computed once at construction — autocomplete compares
     * against this instead of re-normalizing 20k+ names per keystroke.
     */
    public final String normalizedName;

    public PlayerInfo(String playerId, String playerName, String yearsPlayed) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.yearsPlayed = yearsPlayed;
        this.normalizedName = normalize(playerName);
    }

    /**
     * Lowercased, diacritic-insensitive, alphanumerics only
     * ("José Peña" → "josepena").
     */
    public static String normalize(String str) {
        if (str == null) return "";
        String decomposed = Normalizer.normalize(str, Normalizer.Form.NFD);
        String noDiacritics = DIACRITICAL_MARKS.matcher(decomposed).replaceAll("");
        return NON_ALPHANUMERIC.matcher(noDiacritics.toLowerCase()).replaceAll("");
    }

    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public String getYearsPlayed() { return yearsPlayed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerInfo that = (PlayerInfo) o;
        return Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("playerId", playerId);
            json.put("playerName", playerName);
            json.put("yearsPlayed", yearsPlayed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static PlayerInfo fromJSON(JSONObject json) {
        try {
            return new PlayerInfo(
                json.getString("playerId"),
                json.getString("playerName"),
                json.getString("yearsPlayed")
            );
        } catch (JSONException e) {
            return null;
        }
    }
}
