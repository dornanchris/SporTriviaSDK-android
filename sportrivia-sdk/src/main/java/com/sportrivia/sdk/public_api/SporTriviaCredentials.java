package com.sportrivia.sdk.public_api;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * AWS credentials issued by the SporTrivia team to a licensed partner.
 */
public final class SporTriviaCredentials {
    public final String accessKey;
    public final String secretKey;
    public final String region;
    public final String sessionToken;

    public SporTriviaCredentials(String accessKey, String secretKey, String region) {
        this(accessKey, secretKey, region, null);
    }

    public SporTriviaCredentials(String accessKey, String secretKey, String region, String sessionToken) {
        if (accessKey == null || accessKey.isEmpty()) {
            throw new IllegalArgumentException("accessKey is required");
        }
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("secretKey is required");
        }
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = (region == null || region.isEmpty()) ? "us-east-2" : region;
        this.sessionToken = sessionToken;
    }

    /**
     * Load credentials from a properties file in the app's assets folder.
     * <p>
     * The properties file must contain keys:
     * <ul>
     *   <li>{@code sportrivia.accessKey}</li>
     *   <li>{@code sportrivia.secretKey}</li>
     *   <li>{@code sportrivia.region} (optional, defaults to us-east-2)</li>
     * </ul>
     */
    public static SporTriviaCredentials fromAssets(Context context, String assetFileName) throws IOException {
        AssetManager assets = context.getAssets();
        Properties props = new Properties();
        try (InputStream in = assets.open(assetFileName)) {
            props.load(in);
        }
        String accessKey = props.getProperty("sportrivia.accessKey");
        String secretKey = props.getProperty("sportrivia.secretKey");
        String region = props.getProperty("sportrivia.region", "us-east-2");

        if (accessKey == null || accessKey.isEmpty()) {
            throw new IOException("Missing 'sportrivia.accessKey' in " + assetFileName);
        }
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IOException("Missing 'sportrivia.secretKey' in " + assetFileName);
        }
        return new SporTriviaCredentials(accessKey, secretKey, region);
    }
}
