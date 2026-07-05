package com.sportrivia.sdk.public_api;

/**
 * SDK release version, reported as {@code sdk_version} in game-result
 * uploads. Keep in step with the git tag when releasing.
 *
 * <p>Lives in its own android-free class so the upload formatter (and its
 * JVM unit tests) can reference it without pulling in {@link SporTriviaSDK},
 * which imports Android framework types.
 */
public final class SporTriviaVersion {

    public static final String SDK_VERSION = "1.1.0";

    private SporTriviaVersion() {}
}
