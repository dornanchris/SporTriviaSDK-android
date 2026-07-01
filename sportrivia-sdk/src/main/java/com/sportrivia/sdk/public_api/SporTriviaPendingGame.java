package com.sportrivia.sdk.public_api;

/**
 * A custom game referenced by a deep link, ready to be launched.
 */
public final class SporTriviaPendingGame {
    private final String gameId;
    private final Sport sport;

    public SporTriviaPendingGame(String gameId, Sport sport) {
        this.gameId = gameId;
        this.sport = sport;
    }

    /** The custom game identifier (S3 file name, e.g. "NYI_Top5A"). */
    public String getGameId() { return gameId; }

    /** The sport/league for this game. */
    public Sport getSport() { return sport; }
}
