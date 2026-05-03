package com.sportrivia.sdk.internal.services;

import com.sportrivia.sdk.internal.models.PlayerInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses JSON data structures used by the SDK.
 */
public class JsonParser {

    public static Map<String, Object> parseAnswerKey(byte[] data) throws Exception {
        JSONObject json = new JSONObject(new String(data, "UTF-8"));
        Map<String, Object> result = new HashMap<>();
        result.put("combo", json.getString("combo"));
        result.put("type", json.getInt("type"));
        result.put("question", json.optString("question", null));

        JSONArray playerIdArray = json.getJSONArray("player_id");
        List<String> playerIds = new ArrayList<>();
        for (int i = 0; i < playerIdArray.length(); i++) {
            Object val = playerIdArray.get(i);
            playerIds.add(String.valueOf(val));
        }
        result.put("player_ids", playerIds);
        return result;
    }

    public static List<PlayerInfo> parsePlayerList(byte[] data) throws Exception {
        JSONArray jsonArray = new JSONArray(new String(data, "UTF-8"));
        Map<String, PlayerInfo> seen = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);

            String playerId = String.valueOf(obj.get("player_id")).trim();

            String playerName;
            if (obj.has("Player")) {
                playerName = obj.getString("Player");
            } else {
                playerName = obj.getString("name");
            }
            playerName = playerName.trim()
                .replace("Â", "")
                .replace("#", "")
                .replace("+", "")
                .replace("*", "")
                .replace("?", "");

            String firstSeason = String.valueOf(obj.get("first_season")).replace(".0", "");
            String lastSeason = String.valueOf(obj.get("last_season")).replace(".0", "");
            String yearsPlayed = firstSeason + "-" + lastSeason;

            seen.put(playerId, new PlayerInfo(playerId, playerName, yearsPlayed));
        }

        return new ArrayList<>(seen.values());
    }

    public static byte[] formatGameResults(String firstName, String lastName, String email,
                                            String phoneNumber, String gameId,
                                            List<PlayerInfo> correctPlayers) throws Exception {
        JSONObject json = new JSONObject();
        json.put("firstName", firstName);
        json.put("lastName", lastName);
        json.put("email", email);
        json.put("phoneNumber", phoneNumber);
        json.put("gameId", gameId);

        JSONArray answers = new JSONArray();
        for (PlayerInfo p : correctPlayers) {
            answers.put(p.toJSON());
        }
        json.put("correctAnswers", answers);

        return json.toString(2).getBytes("UTF-8");
    }
}
