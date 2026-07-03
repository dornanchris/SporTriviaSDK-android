package com.sportrivia.sdk.internal.ui;

import com.sportrivia.sdk.internal.logic.GameEngine;
import com.sportrivia.sdk.internal.services.S3DataService;
import com.sportrivia.sdk.public_api.Sport;
import com.sportrivia.sdk.public_api.SporTriviaSDK;

/**
 * Holds the shared game engine instance across SDK activities in a single game session.
 * Cleared when the flow completes.
 */
class SporTriviaSession {
    private static GameEngine gameEngine;
    private static byte[] teamImageBytes;
    private static byte[] sponsorBannerBytes;

    static GameEngine getOrCreateEngine() {
        if (gameEngine == null) {
            S3DataService s3 = new S3DataService(
                SporTriviaSDK.getConfiguration().getCredentialProvider()
            );
            gameEngine = new GameEngine(s3);
        }
        return gameEngine;
    }

    static GameEngine getEngine() {
        return gameEngine;
    }

    static void setTeamImageBytes(byte[] bytes) {
        teamImageBytes = bytes;
    }

    static byte[] getTeamImageBytes() {
        return teamImageBytes;
    }

    static void setSponsorBannerBytes(byte[] bytes) {
        sponsorBannerBytes = bytes;
    }

    static byte[] getSponsorBannerBytes() {
        return sponsorBannerBytes;
    }

    static void clear() {
        gameEngine = null;
        teamImageBytes = null;
        sponsorBannerBytes = null;
    }
}
