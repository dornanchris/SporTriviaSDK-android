package com.sportrivia.sdk.public_api;

import java.util.List;

/**
 * Result data returned when a custom game completes.
 */
public class SporTriviaGameResult {
    private final String gameId;
    private final Sport sport;
    private final int correct;
    private final int incorrect;
    private final int maxStreak;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phoneNumber;
    private final List<String> correctPlayerNames;

    public SporTriviaGameResult(String gameId, Sport sport, int correct, int incorrect,
                                 int maxStreak, String firstName, String lastName,
                                 String email, String phoneNumber,
                                 List<String> correctPlayerNames) {
        this.gameId = gameId;
        this.sport = sport;
        this.correct = correct;
        this.incorrect = incorrect;
        this.maxStreak = maxStreak;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.correctPlayerNames = correctPlayerNames;
    }

    public String getGameId() { return gameId; }
    public Sport getSport() { return sport; }
    public int getCorrect() { return correct; }
    public int getIncorrect() { return incorrect; }
    public int getMaxStreak() { return maxStreak; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public List<String> getCorrectPlayerNames() { return correctPlayerNames; }
}
