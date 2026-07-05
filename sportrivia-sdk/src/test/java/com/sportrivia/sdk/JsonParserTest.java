package com.sportrivia.sdk;

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

    @Test
    public void testFormatGameResults() throws Exception {
        List<PlayerInfo> correct = List.of(
            new PlayerInfo("p1", "Player One", "2000-2010")
        );

        byte[] result = JsonParser.formatGameResults(
            "Jane", "Smith", "jane@test.com", "555-0000",
            true, Map.of("How often do you attend games?", "Weekly"),
            "NYI_Top5A", correct
        );

        String json = new String(result, "UTF-8");
        assertTrue(json.contains("Jane"));
        assertTrue(json.contains("NYI_Top5A"));
        assertTrue(json.contains("Player One"));
        assertTrue(json.contains("Weekly"));
    }
}
