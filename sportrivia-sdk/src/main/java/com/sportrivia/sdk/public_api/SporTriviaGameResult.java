package com.sportrivia.sdk.public_api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result data returned when a custom game completes.
 *
 * <p>Which user-info fields are populated depends on the question's Data
 * Capture configuration in the SporTrivia portal — fields that were not
 * asked for stay empty.
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
    private final boolean over18;
    private final Map<String, String> customFieldAnswers;
    private final List<String> correctPlayerNames;

    public SporTriviaGameResult(String gameId, Sport sport, int correct, int incorrect,
                                 int maxStreak, String firstName, String lastName,
                                 String email, String phoneNumber,
                                 List<String> correctPlayerNames) {
        this(gameId, sport, correct, incorrect, maxStreak, firstName, lastName,
                email, phoneNumber, false, new HashMap<String, String>(), correctPlayerNames);
    }

    public SporTriviaGameResult(String gameId, Sport sport, int correct, int incorrect,
                                 int maxStreak, String firstName, String lastName,
                                 String email, String phoneNumber, boolean over18,
                                 Map<String, String> customFieldAnswers,
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
        this.over18 = over18;
        this.customFieldAnswers = customFieldAnswers == null
                ? Collections.<String, String>emptyMap()
                : Collections.unmodifiableMap(new HashMap<>(customFieldAnswers));
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
    /** Whether the fan checked the "I am over 18" box (false when not asked). */
    public boolean isOver18() { return over18; }
    /** Answers to portal-configured custom questions, keyed by question label. */
    public Map<String, String> getCustomFieldAnswers() { return customFieldAnswers; }
    public List<String> getCorrectPlayerNames() { return correctPlayerNames; }
}
