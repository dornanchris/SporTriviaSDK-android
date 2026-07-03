package com.sportrivia.sdk;

import com.sportrivia.sdk.internal.models.CollectFields;
import com.sportrivia.sdk.internal.models.PlayerInfo;
import com.sportrivia.sdk.internal.services.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CollectFieldsTest {

    @Test
    public void testParseAnswerKeyWithCollectFields() throws Exception {
        String json = "{"
                + "\"combo\":\"NYI_Top5A\",\"type\":2,\"player_id\":[\"p1\"],"
                + "\"question\":\"Q\","
                + "\"collect_fields\":{"
                + "  \"name\":true,\"email\":true,\"phone\":false,\"over_18\":true,"
                + "  \"custom_questions\":["
                + "    {\"id\":\"q1\",\"label\":\"Favorite player\",\"placeholder\":\"Type a name\",\"required\":true},"
                + "    {\"id\":\"q2\",\"label\":\"Season ticket holder?\",\"placeholder\":\"\",\"required\":false}"
                + "  ]"
                + "}}";

        Map<String, Object> result = JsonParser.parseAnswerKey(json.getBytes("UTF-8"));
        CollectFields fields = (CollectFields) result.get("collect_fields");

        assertNotNull(fields);
        assertTrue(fields.name);
        assertTrue(fields.email);
        assertFalse(fields.phone);
        assertTrue(fields.over18);
        assertTrue(fields.hasAnythingToCollect());
        assertEquals(2, fields.customQuestions.size());
        assertEquals("q1", fields.customQuestions.get(0).id);
        assertEquals("Favorite player", fields.customQuestions.get(0).label);
        assertEquals("Type a name", fields.customQuestions.get(0).placeholder);
        assertTrue(fields.customQuestions.get(0).required);
        assertFalse(fields.customQuestions.get(1).required);
    }

    @Test
    public void testLegacyAnswerKeyHasNoCollectFields() throws Exception {
        String json = "{\"combo\":\"NYI_Top5A\",\"type\":2,\"player_id\":[\"p1\"],\"question\":\"Q\"}";

        Map<String, Object> result = JsonParser.parseAnswerKey(json.getBytes("UTF-8"));

        assertNull(result.get("collect_fields"));
        CollectFields legacy = CollectFields.legacyDefault();
        assertTrue(legacy.name && legacy.email && legacy.phone);
        assertFalse(legacy.over18);
        assertTrue(legacy.hasAnythingToCollect());
    }

    @Test
    public void testNothingToCollect() {
        CollectFields fields = CollectFields.fromJson(new JSONObject());
        assertFalse(fields.hasAnythingToCollect());
    }

    @Test
    public void testFormatGameResultsIncludesPortalKeys() throws Exception {
        Map<String, String> customAnswers = new LinkedHashMap<>();
        customAnswers.put("Favorite player", "Mat Barzal");
        List<PlayerInfo> players = new ArrayList<>();
        players.add(new PlayerInfo("p1", "Mat Barzal", "2016-2024"));

        byte[] data = JsonParser.formatGameResults(
                "Casey", "Fan", "casey@example.com", "555-1234",
                true, customAnswers, "NYI_Top5A", players);
        JSONObject payload = new JSONObject(new String(data, "UTF-8"));

        assertEquals("Casey Fan", payload.getString("name"));
        assertEquals("casey@example.com", payload.getString("email"));
        assertEquals("555-1234", payload.getString("phone"));
        assertTrue(payload.getBoolean("over_18"));
        assertEquals("Mat Barzal", payload.getJSONObject("custom_field_answers").getString("Favorite player"));
        JSONArray answersFound = payload.getJSONArray("answers_found");
        assertEquals("Mat Barzal 2016-2024", answersFound.getString(0));
        assertTrue(payload.has("submitted_at"));
        // Legacy keys kept for older consumers
        assertEquals("Casey", payload.getString("firstName"));
        assertEquals("555-1234", payload.getString("phoneNumber"));
        assertTrue(payload.has("correctAnswers"));
    }
}
