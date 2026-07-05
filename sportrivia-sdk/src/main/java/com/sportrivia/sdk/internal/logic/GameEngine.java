package com.sportrivia.sdk.internal.logic;

import com.sportrivia.sdk.internal.data.TeamAbbreviations;
import com.sportrivia.sdk.internal.models.CollectFields;
import com.sportrivia.sdk.internal.models.LocationResult;
import com.sportrivia.sdk.internal.models.PlayerInfo;
import com.sportrivia.sdk.internal.models.Sponsorship;
import com.sportrivia.sdk.internal.services.JsonParser;
import com.sportrivia.sdk.internal.services.LocationProvider;
import com.sportrivia.sdk.internal.services.S3DataService;
import com.sportrivia.sdk.public_api.Sport;
import com.sportrivia.sdk.public_api.SporTriviaGameResult;
import com.sportrivia.sdk.public_api.SporTriviaLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Core game logic for custom games.
 * Handles answer validation, scoring, and game setup.
 */
public class GameEngine {
    private final S3DataService s3Service;

    private String gameId;
    private Sport sport;
    private List<String> correctPlayerIds = new ArrayList<>();
    private Set<String> usedPlayerIds = new HashSet<>();
    private List<PlayerInfo> allPlayers = new ArrayList<>();
    private List<PlayerInfo> remainingCorrectPlayers = new ArrayList<>();
    private List<PlayerInfo> correctUserPlayers = new ArrayList<>();

    private int correct = 0;
    private int incorrect = 0;
    private int currentStreak = 0;
    private int maxStreak = 0;

    private String customQuestion;
    private String team1Name = "";
    private String teamAbbr = "";

    private String firstName = "";
    private String lastName = "";
    private String email = "";
    private String phoneNumber = "";
    private boolean over18 = false;
    private Map<String, String> customFieldAnswers = new HashMap<>();
    private CollectFields collectFields = CollectFields.legacyDefault();
    private Sponsorship sponsorship;
    private String responsePath;

    /**
     * Best-effort device location for the results upload; injected by the
     * UI layer. Null (e.g. in tests) uploads location_status "unavailable".
     */
    private LocationProvider locationProvider;

    public GameEngine(S3DataService s3Service) {
        this.s3Service = s3Service;
    }

    public void loadGame(String gameId, Sport sport) throws Exception {
        SporTriviaLogger.info("Loading game: gameId=" + gameId + ", sport=" + sport.getCode());
        this.gameId = gameId;
        this.sport = sport;

        byte[] answerKeyData = s3Service.downloadAnswerKey(gameId);
        Map<String, Object> answerKey = JsonParser.parseAnswerKey(answerKeyData);
        @SuppressWarnings("unchecked")
        List<String> rawIds = (List<String>) answerKey.get("player_ids");
        correctPlayerIds = new ArrayList<>();
        for (String id : rawIds) {
            correctPlayerIds.add(normalizePlayerId(id));
        }
        customQuestion = (String) answerKey.get("question");
        Object rawCollectFields = answerKey.get("collect_fields");
        collectFields = rawCollectFields instanceof CollectFields
                ? (CollectFields) rawCollectFields
                : CollectFields.legacyDefault();
        Object rawSponsorship = answerKey.get("sponsorship");
        sponsorship = rawSponsorship instanceof Sponsorship ? (Sponsorship) rawSponsorship : null;
        Object rawResponsePath = answerKey.get("response_path");
        responsePath = rawResponsePath instanceof String ? ((String) rawResponsePath).trim() : null;
        SporTriviaLogger.info("Answer key loaded: " + correctPlayerIds.size() + " correct IDs");

        byte[] playerData = s3Service.downloadPlayerList(sport);
        allPlayers = JsonParser.parsePlayerList(playerData);
        SporTriviaLogger.info("Player list loaded: " + allPlayers.size() + " players");

        remainingCorrectPlayers = findMatchingPlayers(correctPlayerIds, allPlayers);
        SporTriviaLogger.info("Matched " + remainingCorrectPlayers.size() + " of " + correctPlayerIds.size() + " player IDs to player names");

        if (remainingCorrectPlayers.isEmpty() && !correctPlayerIds.isEmpty()) {
            SporTriviaLogger.warning("0 players matched! Answer key IDs don't match player list IDs.");
            SporTriviaLogger.warning("Answer key IDs (first 5): " + correctPlayerIds.subList(0, Math.min(5, correctPlayerIds.size())));
            SporTriviaLogger.warning("Player list IDs (first 5): " + allPlayers.subList(0, Math.min(5, allPlayers.size())));
        }

        String[] parts = gameId.split("_");
        teamAbbr = parts[0];
        String resolved = TeamAbbreviations.getTeamName(teamAbbr, sport);
        team1Name = resolved != null ? resolved : teamAbbr;
        SporTriviaLogger.info("Game ready: '" + team1Name + "', " + remainingCorrectPlayers.size() + " players to guess");
    }

    public byte[] downloadTeamImage() throws Exception {
        return s3Service.downloadTeamImage(sport, teamAbbr);
    }

    /** Download the sponsorship banner image, or null when the question has no sponsor. */
    public byte[] downloadSponsorshipBanner() throws Exception {
        if (sponsorship == null) {
            return null;
        }
        return s3Service.download(sponsorship.assetKey);
    }

    public void startGame() {
        correct = 0;
        incorrect = 0;
        currentStreak = 0;
        maxStreak = 0;
        usedPlayerIds.clear();
        correctUserPlayers.clear();
    }

    public boolean isPlayerUsed(String playerId) {
        return usedPlayerIds.contains(playerId);
    }

    public boolean checkAnswer(PlayerInfo player) {
        String normalizedId = normalizePlayerId(player.playerId);
        boolean isCorrect = correctPlayerIds.contains(normalizedId);

        if (!isCorrect && sport != Sport.MLB) {
            for (String cid : correctPlayerIds) {
                if (cid.contains(normalizedId) || normalizedId.contains(cid)) {
                    isCorrect = true;
                    break;
                }
            }
        }

        if (isCorrect) {
            usedPlayerIds.add(player.playerId);
            correct++;
            currentStreak++;
            maxStreak = Math.max(maxStreak, currentStreak);
            correctUserPlayers.add(player);
            remainingCorrectPlayers.removeIf(p -> p.playerId.equals(player.playerId));
        } else {
            incorrect++;
        }

        return isCorrect;
    }

    public boolean allAnswersFound() {
        return remainingCorrectPlayers.isEmpty();
    }

    public void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    public void uploadResults() {
        try {
            // Best-effort location: waits at most 8s (on this background
            // thread) for a fix that started warming when the game opened;
            // never fails the upload.
            LocationResult location = locationProvider != null
                    ? locationProvider.await(8000)
                    : LocationResult.unavailable();
            byte[] data = JsonParser.formatGameResults(
                firstName, lastName, email, phoneNumber, over18, customFieldAnswers,
                gameId, correctUserPlayers, location
            );
            if (responsePath != null && !responsePath.isEmpty()) {
                // Preferred: the portal embeds the exact upload destination in
                // the answer key so results land where the portal export reads.
                s3Service.uploadGameResults(responsePath, data);
            } else {
                // Legacy answer keys (no response_path): derive a path from the
                // gameId. For partner accounts this folder may not match the
                // portal export's location — republish the question to fix.
                String[] parts = gameId.split("_");
                String suffix = parts.length > 1 ? gameId.substring(parts[0].length() + 1) : "";
                s3Service.uploadGameResults(sport, team1Name, suffix, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SporTriviaGameResult buildResult() {
        List<String> names = new ArrayList<>();
        for (PlayerInfo p : correctUserPlayers) {
            names.add(p.playerName);
        }
        return new SporTriviaGameResult(
            gameId, sport, correct, incorrect, maxStreak,
            firstName, lastName, email, phoneNumber, over18, customFieldAnswers, names
        );
    }

    public void setUserInfo(String firstName, String lastName, String email, String phoneNumber) {
        setUserInfo(firstName, lastName, email, phoneNumber, false, new HashMap<String, String>());
    }

    public void setUserInfo(String firstName, String lastName, String email, String phoneNumber,
                            boolean over18, Map<String, String> customFieldAnswers) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.over18 = over18;
        this.customFieldAnswers = customFieldAnswers == null
                ? new LinkedHashMap<String, String>()
                : new LinkedHashMap<>(customFieldAnswers);
    }

    /** Data-capture configuration for this game (legacy default when the answer key has none). */
    public CollectFields getCollectFields() { return collectFields; }

    /** Per-question sponsorship, or null when the question has no sponsor. */
    public Sponsorship getSponsorship() { return sponsorship; }

    public String getGameId() { return gameId; }
    public Sport getSport() { return sport; }
    public String getCustomQuestion() { return customQuestion; }
    public String getTeam1Name() { return team1Name; }
    public String getTeamAbbr() { return teamAbbr; }
    public int getCorrect() { return correct; }
    public int getIncorrect() { return incorrect; }
    public int getCurrentStreak() { return currentStreak; }
    public int getMaxStreak() { return maxStreak; }
    public List<PlayerInfo> getAllPlayers() { return allPlayers; }
    public List<PlayerInfo> getRemainingCorrectPlayers() { return remainingCorrectPlayers; }
    public List<PlayerInfo> getCorrectUserPlayers() { return correctUserPlayers; }

    private String normalizePlayerId(String id) {
        if (id.endsWith(".0")) {
            return id.substring(0, id.length() - 2);
        }
        return id;
    }

    private List<PlayerInfo> findMatchingPlayers(List<String> playerIds, List<PlayerInfo> allPlayers) {
        Set<String> normalizedIds = new HashSet<>();
        for (String id : playerIds) {
            normalizedIds.add(normalizePlayerId(id));
        }
        List<PlayerInfo> result = new ArrayList<>();
        for (PlayerInfo player : allPlayers) {
            if (normalizedIds.contains(normalizePlayerId(player.playerId))) {
                result.add(player);
            }
        }
        return result;
    }
}
