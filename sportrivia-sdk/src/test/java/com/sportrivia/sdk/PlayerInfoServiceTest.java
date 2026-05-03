package com.sportrivia.sdk;

import com.sportrivia.sdk.internal.logic.PlayerInfoService;
import com.sportrivia.sdk.internal.models.PlayerInfo;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PlayerInfoServiceTest {

    @Test
    public void testFilterPlayers() {
        List<PlayerInfo> players = Arrays.asList(
            new PlayerInfo("p1", "Wayne Gretzky", "1979-1999"),
            new PlayerInfo("p2", "Mario Lemieux", "1984-2006"),
            new PlayerInfo("p3", "Mark Messier", "1979-2004")
        );

        List<PlayerInfo> result = PlayerInfoService.filterPlayers("Mar", players, 10);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterPlayersWithLimit() {
        List<PlayerInfo> players = Arrays.asList(
            new PlayerInfo("p1", "Wayne Gretzky", "1979-1999"),
            new PlayerInfo("p2", "Mario Lemieux", "1984-2006"),
            new PlayerInfo("p3", "Mark Messier", "1979-2004")
        );

        List<PlayerInfo> result = PlayerInfoService.filterPlayers("Mar", players, 1);
        assertEquals(1, result.size());
    }

    @Test
    public void testFilterPlayersEmpty() {
        List<PlayerInfo> players = Arrays.asList(
            new PlayerInfo("p1", "Wayne Gretzky", "1979-1999")
        );

        List<PlayerInfo> result = PlayerInfoService.filterPlayers("", players, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSelectPlayerFromInputExact() {
        List<PlayerInfo> players = Arrays.asList(
            new PlayerInfo("p1", "Wayne Gretzky", "1979-1999")
        );

        PlayerInfo result = PlayerInfoService.selectPlayerFromInput("Wayne Gretzky", players);
        assertEquals("p1", result.playerId);
    }

    @Test
    public void testSelectPlayerFromInputNoMatch() {
        List<PlayerInfo> players = Arrays.asList(
            new PlayerInfo("p1", "Wayne Gretzky", "1979-1999")
        );

        PlayerInfo result = PlayerInfoService.selectPlayerFromInput("Not A Player", players);
        assertTrue(result.playerId.startsWith("unknown_"));
        assertEquals("Not A Player", result.playerName);
    }

    @Test
    public void testNormalizeDiacritics() {
        assertEquals("josebautista", PlayerInfoService.normalize("José Bautista"));
        assertEquals("josebautista", PlayerInfoService.normalize("Jose Bautista"));
    }

    @Test
    public void testNormalizeSpecialChars() {
        assertEquals("oneill", PlayerInfoService.normalize("O'Neill"));
        assertEquals("stlouis", PlayerInfoService.normalize("St. Louis"));
    }
}
