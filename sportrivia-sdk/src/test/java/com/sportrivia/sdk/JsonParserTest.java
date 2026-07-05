package com.sportrivia.sdk;

import com.sportrivia.sdk.internal.models.LocationResult;
import com.sportrivia.sdk.internal.models.PlayerInfo;
import com.sportrivia.sdk.internal.services.JsonParser;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class JsonParserTest {

    @Test
    public void testParseAnswerKey() throws Exception {
        String json = "{\"combo\":\"NYI_Top5A\",\"type\":2,\"player_id\":[\"p1\",\"p2\",\"p3\"],\"question\":\"Name top 5\"}";
        byte[] data = json.getBytes("UTF-8");

        Map<String, Object> result = JsonParser.parseAnswerKey(data);

        assertEquals("NYI_Top5A", result.get("combo"));
        assertEquals(2, result.get("type"));
        assertEquals("Name top 5", result.get("question"));

        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) result.get("player_ids");
        assertEquals(3, ids.size());
        assertEquals("p1", ids.get(0));
    }

    @Test
    public void testParseAnswerKeyWithIntIds() throws Exception {
        String json = "{\"combo\":\"BOS_100W\",\"type\":2,\"player_id\":[123,456]}";
        byte[] data = json.getBytes("UTF-8");

        Map<String, Object> result = JsonParser.parseAnswerKey(data);

        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) result.get("player_ids");
        assertEquals(2, ids.size());
        assertEquals("123", ids.get(0));
    }

    @Test
    public void testParseAnswerKeyWithResponsePath() throws Exception {
        String json = "{\"combo\":\"custom_who-holds-the-home-run-record\",\"type\":2,\"player_id\":[\"p1\"],"
                + "\"response_path\":\"custom/MLB/CD Test/who-holds-the-home-run-record/responses/\"}";

        Map<String, Object> result = JsonParser.parseAnswerKey(json.getBytes("UTF-8"));

        assertEquals("custom/MLB/CD Test/who-holds-the-home-run-record/responses/", result.get("response_path"));
    }

    @Test
    public void testParseAnswerKeyWithoutResponsePath() throws Exception {
        String json = "{\"combo\":\"NYI_Top5A\",\"type\":2,\"player_id\":[\"p1\"]}";

        Map<String, Object> result = JsonParser.parseAnswerKey(json.getBytes("UTF-8"));

        assertNull(result.get("response_path"));
    }

    @Test
    public void testParseAnswerKeyWithNullResponsePath() throws Exception {
        String json = "{\"combo\":\"NYI_Top5A\",\"type\":2,\"player_id\":[\"p1\"],\"response_path\":null}";

        Map<String, Object> result = JsonParser.parseAnswerKey(json.getBytes("UTF-8"));

        assertNull(result.get("response_path"));
    }

    @Test
    public void testParsePlayerList() throws Exception {
        String json = "[{\"player_id\":\"gretz01\",\"Player\":\"Wayne Gretzky\",\"first_season\":\"1979\",\"last_season\":\"1999\"}]";
        byte[] data = json.getBytes("UTF-8");

        List<PlayerInfo> players = JsonParser.parsePlayerList(data);
        assertEquals(1, players.size());
        assertEquals("Wayne Gretzky", players.get(0).playerName);
        assertEquals("1979-1999", players.get(0).yearsPlayed);
    }

    @Test
    public void testParsePlayerListHockeyFormat() throws Exception {
        String json = "[{\"player_id\":8471214,\"name\":\"Sidney Crosby\",\"first_season\":2005,\"last_season\":2024}]";
        byte[] data = json.getBytes("UTF-8");

        List<PlayerInfo> players = JsonParser.parsePlayerList(data);
        assertEquals(1, players.size());
        assertEquals("Sidney Crosby", players.get(0).playerName);
        assertEquals("8471214", players.get(0).playerId);
    }

    // ===== Schema v2 upload payload =====

    /**
     * The documented cross-platform key order — must stay in lockstep with
     * the iOS SDK's JsonParserTests and PARTNER_SETUP.md.
     */
    static final String[] EXPECTED_KEY_ORDER = {
        "schema_version", "game_id", "submitted_at", "platform", "source",
        "sdk_version", "first_name", "last_name", "name", "email", "phone",
        "over_18", "custom_field_answers", "answers_found", "correct_answers",
        "location", "location_status",
    };

    private static String formatV2(LocationResult location) throws Exception {
        List<PlayerInfo> correct = List.of(new PlayerInfo("p1", "Player One", "2000-2010"));
        Map<String, String> custom = new java.util.LinkedHashMap<>();
        custom.put("Zebra question", "z");
        custom.put("Alpha question", "a");
        byte[] result = JsonParser.formatGameResults(
            "Jane", "Smith", "jane@test.com", "555-0000",
            true, custom, "NYI_Top5A", correct, location
        );
        return new String(result, "UTF-8");
    }

    @Test
    public void testFormatGameResultsSchemaV2() throws Exception {
        String json = formatV2(LocationResult.unavailable());
        org.json.JSONObject parsed = new org.json.JSONObject(json);

        assertEquals(2, parsed.getInt("schema_version"));
        assertEquals("NYI_Top5A", parsed.getString("game_id"));
        assertEquals("android", parsed.getString("platform"));
        assertEquals("sdk", parsed.getString("source"));
        assertEquals(com.sportrivia.sdk.public_api.SporTriviaVersion.SDK_VERSION, parsed.getString("sdk_version"));
        assertEquals("Jane", parsed.getString("first_name"));
        assertEquals("Smith", parsed.getString("last_name"));
        assertEquals("Jane Smith", parsed.getString("name"));
        assertTrue(parsed.getBoolean("over_18"));

        org.json.JSONArray answers = parsed.getJSONArray("correct_answers");
        assertEquals(1, answers.length());
        assertEquals("p1", answers.getJSONObject(0).getString("player_id"));
        assertEquals("Player One", answers.getJSONObject(0).getString("player_name"));
        assertEquals("2000-2010", answers.getJSONObject(0).getString("years_played"));

        assertTrue(parsed.isNull("location"));
        assertEquals("unavailable", parsed.getString("location_status"));

        // Legacy duplicate keys are gone in v2
        for (String legacy : new String[]{"gameId", "firstName", "lastName", "phoneNumber", "correctAnswers"}) {
            assertFalse(legacy + " must not be emitted in schema v2", parsed.has(legacy));
        }
    }

    @Test
    public void testFormatGameResultsKeyOrderIsExact() throws Exception {
        String json = formatV2(LocationResult.unavailable());
        int lastIndex = -1;
        for (String key : EXPECTED_KEY_ORDER) {
            int index = json.indexOf("\"" + key + "\":");
            assertTrue("key missing: " + key, index >= 0);
            assertTrue("key out of order: " + key, index > lastIndex);
            lastIndex = index;
        }
    }

    @Test
    public void testFormatGameResultsCustomAnswersAlphabetical() throws Exception {
        String json = formatV2(LocationResult.unavailable());
        assertTrue(json.indexOf("Alpha question") < json.indexOf("Zebra question"));
    }

    @Test
    public void testFormatGameResultsIsPrettyPrinted() throws Exception {
        String json = formatV2(LocationResult.unavailable());
        // One field per line: a newline separates top-level keys.
        assertTrue("output should be multi-line", json.contains("\n"));
        assertTrue(json.contains("\"schema_version\": 2"));
        // Still valid JSON with the same content.
        org.json.JSONObject parsed = new org.json.JSONObject(json);
        assertEquals("NYI_Top5A", parsed.getString("game_id"));
    }

    @Test
    public void testFormatGameResultsTimestampFormat() throws Exception {
        org.json.JSONObject parsed = new org.json.JSONObject(formatV2(LocationResult.unavailable()));
        assertTrue(parsed.getString("submitted_at")
                .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"));
    }

    @Test
    public void testFormatGameResultsWithGrantedLocation() throws Exception {
        String json = formatV2(LocationResult.granted(40.75, -73.99, 12.5, "2026-07-05T12:00:00Z"));
        org.json.JSONObject parsed = new org.json.JSONObject(json);
        org.json.JSONObject location = parsed.getJSONObject("location");

        assertEquals(40.75, location.getDouble("latitude"), 1e-9);
        assertEquals(-73.99, location.getDouble("longitude"), 1e-9);
        assertEquals(12.5, location.getDouble("accuracy_meters"), 1e-9);
        assertEquals("2026-07-05T12:00:00Z", location.getString("captured_at"));
        assertEquals("granted", parsed.getString("location_status"));

        // Inner key order is part of the schema too
        assertTrue(json.indexOf("\"latitude\"") < json.indexOf("\"longitude\""));
        assertTrue(json.indexOf("\"longitude\"") < json.indexOf("\"accuracy_meters\""));
        assertTrue(json.indexOf("\"accuracy_meters\"") < json.indexOf("\"captured_at\""));
    }

    @Test
    public void testFormatGameResultsLocationStatuses() throws Exception {
        assertEquals("denied", new org.json.JSONObject(formatV2(LocationResult.denied())).getString("location_status"));
        assertEquals("timeout", new org.json.JSONObject(formatV2(LocationResult.timeout())).getString("location_status"));
        // Null location argument behaves like unavailable
        byte[] result = JsonParser.formatGameResults(
            "J", "S", "", "", false, null, "g", List.of(), null);
        assertEquals("unavailable", new org.json.JSONObject(new String(result, "UTF-8")).getString("location_status"));
    }

    @Test
    public void testFormatGameResultsEscapesUserInput() throws Exception {
        byte[] result = JsonParser.formatGameResults(
            "Ja\"ne\n", "Smi\\th", "a@b.c", "", false, null, "g", List.of(),
            LocationResult.unavailable());
        // Must parse back cleanly — user input cannot break the JSON
        org.json.JSONObject parsed = new org.json.JSONObject(new String(result, "UTF-8"));
        assertEquals("Ja\"ne", parsed.getString("first_name"));
        assertEquals("Smi\\th", parsed.getString("last_name"));
    }
}
