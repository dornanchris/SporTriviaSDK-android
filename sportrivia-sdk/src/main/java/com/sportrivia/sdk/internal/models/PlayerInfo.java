package com.sportrivia.sdk.internal.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Represents a player with their ID, name, and years active.
 */
public class PlayerInfo {
    public String playerId;
    public String playerName;
    public String yearsPlayed;

    public PlayerInfo(String playerId, String playerName, String yearsPlayed) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.yearsPlayed = yearsPlayed;
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
