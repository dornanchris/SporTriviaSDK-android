package com.sportrivia.sdk.public_api;

/**
 * Delegate interface for receiving game lifecycle events from the SDK.
 */
public interface SporTriviaDelegate {
    void onGameComplete(SporTriviaGameResult result);
    void onGameCancelled();
    void onGameError(Exception error);
}
