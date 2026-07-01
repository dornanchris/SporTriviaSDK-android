package com.sportrivia.sdk.internal.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.sportrivia.sdk.public_api.SporTriviaConfiguration;
import com.sportrivia.sdk.public_api.SporTriviaDelegate;
import com.sportrivia.sdk.public_api.SporTriviaSDK;

/**
 * Transparent entry-point activity that catches a SporTrivia deep link and
 * launches the corresponding custom game.
 *
 * Partners enable this by declaring the deep-link scheme in their app via the
 * {@code sporTriviaScheme} manifest placeholder (see the SDK setup guide). No
 * partner code is required; the game is launched with the configuration's
 * default delegate (or the active delegate) if one is set.
 */
public class SporTriviaDeepLinkActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri data = getIntent() != null ? getIntent().getData() : null;
        SporTriviaDelegate delegate = resolveDelegate();

        if (data != null) {
            SporTriviaSDK.handleDeepLink(this, data, delegate);
        }
        finish();
    }

    private SporTriviaDelegate resolveDelegate() {
        SporTriviaConfiguration config = SporTriviaSDK.getConfiguration();
        if (config != null && config.getDefaultDelegate() != null) {
            return config.getDefaultDelegate();
        }
        return SporTriviaSDK.getActiveDelegate();
    }
}
