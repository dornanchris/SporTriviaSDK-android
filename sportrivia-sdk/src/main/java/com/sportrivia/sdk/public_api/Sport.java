package com.sportrivia.sdk.public_api;

/**
 * Supported sports/leagues in SporTrivia.
 */
public enum Sport {
    MLB("mlb", "Baseball", "baseball", "MLB"),
    NBA("nba", "Basketball", "basketball", "NBA"),
    NFL("nfl", "Football", "football", "NFL"),
    NHL("nhl", "Hockey", "hockey", "NHL"),
    AHL("ahl", "AHL Hockey", "hockey", "AHL"),
    ECHL("echl", "ECHL Hockey", "hockey", "ECHL");

    private final String code;
    private final String displayName;
    private final String sportCategory;
    private final String leagueKey;

    Sport(String code, String displayName, String sportCategory, String leagueKey) {
        this.code = code;
        this.displayName = displayName;
        this.sportCategory = sportCategory;
        this.leagueKey = leagueKey;
    }

    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public String getSportCategory() { return sportCategory; }
    public String getLeagueKey() { return leagueKey; }

    public static Sport fromCode(String code) {
        for (Sport sport : values()) {
            if (sport.code.equalsIgnoreCase(code.trim())) {
                return sport;
            }
        }
        throw new IllegalArgumentException("Unknown sport code: " + code);
    }
}
