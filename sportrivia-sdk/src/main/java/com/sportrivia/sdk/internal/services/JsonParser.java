package com.sportrivia.sdk.internal.services;

import com.sportrivia.sdk.internal.models.CollectFields;
import com.sportrivia.sdk.internal.models.LocationResult;
import com.sportrivia.sdk.internal.models.PlayerInfo;
import com.sportrivia.sdk.internal.models.Sponsorship;
import com.sportrivia.sdk.public_api.SporTriviaVersion;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

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

        // Per-question sponsorship chosen in the portal; absent when the
        // question has no sponsor.
        Sponsorship sponsorship = Sponsorship.fromJson(json.optJSONObject("sponsorship"));
        if (sponsorship != null) {
            result.put("sponsorship", sponsorship);
        }

        // S3 prefix where results must be uploaded (e.g.
        // "custom/MLB/CD Test/who-holds-the-home-run-record/responses/");
        // absent on answer keys published before the portal embedded it.
        String responsePath = json.isNull("response_path") ? "" : json.optString("response_path", "");
        if (!responsePath.isEmpty()) {
            result.put("response_path", responsePath);
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
     * Format game results as JSON for S3 upload — schema v2.
     *
     * <p>The schema is a cross-platform contract with partners: the Android
     * and iOS SDKs (and the first-party apps) emit the SAME keys in the SAME
     * order, documented in PARTNER_SETUP.md. Built with {@link JSONStringer}
     * because it streams keys in insertion order — {@link JSONObject} does
     * not guarantee order.
     */
    public static byte[] formatGameResults(String firstName, String lastName, String email,
                                            String phoneNumber, boolean over18,
                                            Map<String, String> customFieldAnswers,
                                            String gameId,
                                            List<PlayerInfo> correctPlayers,
                                            LocationResult location) throws Exception {
        if (location == null) {
            location = LocationResult.unavailable();
        }
        String safeFirst = firstName == null ? "" : firstName.trim();
        String safeLast = lastName == null ? "" : lastName.trim();
        StringBuilder fullName = new StringBuilder(safeFirst);
        if (!safeLast.isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(' ');
            }
            fullName.append(safeLast);
        }

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        JSONStringer json = new JSONStringer();
        json.object();
        json.key("schema_version").value(2);
        json.key("game_id").value(gameId);
        json.key("submitted_at").value(isoFormat.format(new Date()));
        json.key("platform").value("android");
        json.key("source").value("sdk");
        json.key("sdk_version").value(SporTriviaVersion.SDK_VERSION);
        json.key("first_name").value(safeFirst);
        json.key("last_name").value(safeLast);
        json.key("name").value(fullName.toString());
        json.key("email").value(email == null ? "" : email);
        json.key("phone").value(phoneNumber == null ? "" : phoneNumber);
        json.key("over_18").value(over18);

        // Alphabetical key order keeps custom answers deterministic across
        // platforms (the source maps are unordered on both).
        json.key("custom_field_answers").object();
        if (customFieldAnswers != null) {
            for (Map.Entry<String, String> entry : new TreeMap<>(customFieldAnswers).entrySet()) {
                json.key(entry.getKey()).value(entry.getValue());
            }
        }
        json.endObject();

        json.key("answers_found").array();
        for (PlayerInfo p : correctPlayers) {
            String yearsPlayed = p.yearsPlayed == null ? "" : p.yearsPlayed.trim();
            json.value(yearsPlayed.isEmpty() ? p.playerName : p.playerName + " " + yearsPlayed);
        }
        json.endArray();

        json.key("correct_answers").array();
        for (PlayerInfo p : correctPlayers) {
            json.object();
            json.key("player_id").value(p.playerId == null ? "" : p.playerId);
            json.key("player_name").value(p.playerName == null ? "" : p.playerName);
            json.key("years_played").value(p.yearsPlayed == null ? "" : p.yearsPlayed);
            json.endObject();
        }
        json.endArray();

        json.key("location");
        if (location.hasFix()) {
            json.object();
            json.key("latitude").value((double) location.latitude);
            json.key("longitude").value((double) location.longitude);
            json.key("accuracy_meters").value(location.accuracyMeters == null ? 0d : location.accuracyMeters);
            json.key("captured_at").value(location.capturedAt == null ? "" : location.capturedAt);
            json.endObject();
        } else {
            json.value(JSONObject.NULL);
        }
        json.key("location_status").value(location.status);

        json.endObject();
        return json.toString().getBytes("UTF-8");
    }
}
