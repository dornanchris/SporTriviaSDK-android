package com.sportrivia.sdk.internal.services;

import com.sportrivia.sdk.internal.models.CollectFields;
import com.sportrivia.sdk.internal.models.PlayerInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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

        // Data-capture config from the portal's Data Capture step; absent on
        // answer keys written before the feature existed.
        JSONObject collectFieldsJson = json.optJSONObject("collect_fields");
        if (collectFieldsJson != null) {
            result.put("collect_fields", CollectFields.fromJson(collectFieldsJson));
        }
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

    /**
     * Format game results as JSON for S3 upload.
     *
     * <p>Writes the keys the SporTrivia portal reads back for its contacts
     * view and CSV export (name/email/phone/over_18/custom_field_answers/
     * answers_found), plus the original firstName/lastName/phoneNumber/
     * correctAnswers keys for older downstream consumers.
     */
    public static byte[] formatGameResults(String firstName, String lastName, String email,
                                            String phoneNumber, boolean over18,
                                            Map<String, String> customFieldAnswers,
                                            String gameId,
                                            List<PlayerInfo> correctPlayers) throws Exception {
        JSONObject json = new JSONObject();
        json.put("gameId", gameId);

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        json.put("submitted_at", isoFormat.format(new Date()));

        StringBuilder fullName = new StringBuilder(firstName == null ? "" : firstName.trim());
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(' ');
            }
            fullName.append(lastName.trim());
        }
        json.put("name", fullName.toString());
        json.put("email", email == null ? "" : email);
        json.put("phone", phoneNumber == null ? "" : phoneNumber);
        json.put("over_18", over18);

        JSONObject customAnswers = new JSONObject();
        if (customFieldAnswers != null) {
            for (Map.Entry<String, String> entry : customFieldAnswers.entrySet()) {
                customAnswers.put(entry.getKey(), entry.getValue());
            }
        }
        json.put("custom_field_answers", customAnswers);

        JSONArray answersFound = new JSONArray();
        for (PlayerInfo p : correctPlayers) {
            String yearsPlayed = p.yearsPlayed == null ? "" : p.yearsPlayed.trim();
            answersFound.put(yearsPlayed.isEmpty() ? p.playerName : p.playerName + " " + yearsPlayed);
        }
        json.put("answers_found", answersFound);

        // Legacy keys kept for older consumers
        json.put("firstName", firstName);
        json.put("lastName", lastName);
        json.put("phoneNumber", phoneNumber);
        JSONArray answers = new JSONArray();
        for (PlayerInfo p : correctPlayers) {
            answers.put(p.toJSON());
        }
        json.put("correctAnswers", answers);

        return json.toString(2).getBytes("UTF-8");
    }
}
