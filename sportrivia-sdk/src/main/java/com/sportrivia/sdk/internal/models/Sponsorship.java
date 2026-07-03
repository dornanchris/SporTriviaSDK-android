package com.sportrivia.sdk.internal.models;

import org.json.JSONObject;

/**
 * Per-question sponsorship chosen in the SporTrivia portal and embedded in
 * the answer key JSON. {@code assetKey} is the S3 key of the banner image.
 */
public final class Sponsorship {

    public final String brand;
    public final String url;
    public final String assetKey;

    public Sponsorship(String brand, String url, String assetKey) {
        this.brand = brand;
        this.url = url;
        this.assetKey = assetKey;
    }

    /** Parse the sponsorship object from an answer key JSON; null when incomplete. */
    public static Sponsorship fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        String assetKey = json.optString("asset_key", "").trim();
        if (assetKey.isEmpty()) {
            return null;
        }
        return new Sponsorship(
                json.optString("brand", "").trim(),
                json.optString("url", "").trim(),
                assetKey
        );
    }
}
