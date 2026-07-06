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

    @Test
    public void testNormalizedNamePrecomputedAtConstruction() {
        PlayerInfo player = new PlayerInfo("p1", "José Peña", "2004-2018");
        assertEquals("josepena", player.normalizedName);
    }

    @Test
    public void testFilterMatchesDiacriticsViaPrecomputedName() {
        List<PlayerInfo> players = Arrays.asList(
            new PlayerInfo("p1", "José Peña", "2004-2018"),
            new PlayerInfo("p2", "Wayne Gretzky", "1979-1999")
        );

        List<PlayerInfo> result = PlayerInfoService.filterPlayers("jose pena", players, 10);
        assertEquals(1, result.size());
        assertEquals("p1", result.get(0).playerId);
    }

    @Test
    public void testFilterEarlyExitsOnLargeList() {
        // 30k synthetic players; the first 10 match — the filter must stop
        // there (limit early-break) and stay fast.
        List<PlayerInfo> players = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            players.add(new PlayerInfo("m" + i, "Common Target " + i, "2000-2010"));
        }
        for (int i = 0; i < 30000; i++) {
            players.add(new PlayerInfo("f" + i, "Filler Player " + i, "2000-2010"));
        }

        long start = System.nanoTime();
        List<PlayerInfo> result = PlayerInfoService.filterPlayers("common target", players, 10);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertEquals(10, result.size());
        assertTrue("filter took " + elapsedMs + "ms — precomputed names should make this near-instant",
                elapsedMs < 250);
    }

    @Test
    public void testFilterInputThatNormalizesToEmpty() {
        List<PlayerInfo> players = Arrays.asList(
            new PlayerInfo("p1", "Wayne Gretzky", "1979-1999")
        );

        assertTrue(PlayerInfoService.filterPlayers("!!!", players, 10).isEmpty());
    }
}
