package com.sportrivia.sdk.internal.logic;

import com.sportrivia.sdk.internal.models.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Fuzzy player name matching and autocomplete filtering.
 *
 * <p>Per-keystroke work is a plain {@code String.contains} against each
 * player's precomputed {@link PlayerInfo#normalizedName} — only the input is
 * normalized, once per call.
 */
public class PlayerInfoService {

    public static List<PlayerInfo> filterPlayers(String input, List<PlayerInfo> allPlayers, int limit) {
        if (input == null || input.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String normalizedInput = normalize(input);
        if (normalizedInput.isEmpty()) {
            return new ArrayList<>();
        }
        List<PlayerInfo> results = new ArrayList<>();

        for (PlayerInfo player : allPlayers) {
            if (player.normalizedName.contains(normalizedInput)) {
                results.add(player);
                if (results.size() >= limit) break;
            }
        }
        return results;
    }

    public static PlayerInfo selectPlayerFromInput(String input, List<PlayerInfo> allPlayers) {
        String normalizedInput = normalize(input);

        for (PlayerInfo player : allPlayers) {
            if (player.normalizedName.equals(normalizedInput)) {
                return player;
            }
        }

        return new PlayerInfo("unknown_" + System.currentTimeMillis(), input, "");
    }

    public static String normalize(String str) {
        return PlayerInfo.normalize(str);
    }
}
