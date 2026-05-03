package com.sportrivia.sdk.internal.logic;

import com.sportrivia.sdk.internal.models.PlayerInfo;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Fuzzy player name matching and autocomplete filtering.
 */
public class PlayerInfoService {

    public static List<PlayerInfo> filterPlayers(String input, List<PlayerInfo> allPlayers, int limit) {
        if (input == null || input.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String normalizedInput = normalize(input);
        List<PlayerInfo> results = new ArrayList<>();

        for (PlayerInfo player : allPlayers) {
            String normalizedName = normalize(player.playerName);
            if (normalizedName.contains(normalizedInput)) {
                results.add(player);
                if (results.size() >= limit) break;
            }
        }
        return results;
    }

    public static PlayerInfo selectPlayerFromInput(String input, List<PlayerInfo> allPlayers) {
        String normalizedInput = normalize(input);

        for (PlayerInfo player : allPlayers) {
            String normalizedName = normalize(player.playerName);
            if (normalizedName.equals(normalizedInput)) {
                return player;
            }
        }

        return new PlayerInfo("unknown_" + System.currentTimeMillis(), input, "");
    }

    public static String normalize(String str) {
        if (str == null) return "";
        String decomposed = Normalizer.normalize(str, Normalizer.Form.NFD);
        String noDiacritics = decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noDiacritics.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
