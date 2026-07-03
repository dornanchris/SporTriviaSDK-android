package com.sportrivia.sdk.internal.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sportrivia.sdk.internal.logic.GameEngine;
import com.sportrivia.sdk.internal.models.Sponsorship;
import com.sportrivia.sdk.public_api.SporTriviaLogger;

/**
 * Shows the per-question sponsorship banner (chosen in the SporTrivia
 * portal) on a screen, or leaves the views hidden when the question has no
 * sponsor or the banner failed to download.
 */
final class SponsorBanner {

    private SponsorBanner() {}

    static void bind(Activity activity, ImageView bannerView, TextView sponsorText, GameEngine engine) {
        if (bannerView == null || engine == null) {
            return;
        }
        Sponsorship sponsorship = engine.getSponsorship();
        byte[] bannerBytes = SporTriviaSession.getSponsorBannerBytes();
        if (sponsorship == null || bannerBytes == null) {
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(bannerBytes, 0, bannerBytes.length);
        if (bitmap == null) {
            SporTriviaLogger.error("Sponsorship banner for '" + sponsorship.brand
                    + "' downloaded but could not be decoded as an image ("
                    + bannerBytes.length + " bytes) — banner will not be shown");
            return;
        }

        bannerView.setImageBitmap(bitmap);
        bannerView.setVisibility(View.VISIBLE);
        if (sponsorText != null && !sponsorship.brand.isEmpty()) {
            sponsorText.setText(activity.getString(
                    com.sportrivia.sdk.R.string.sportrivia_sdk_sponsored_by, sponsorship.brand));
            sponsorText.setVisibility(View.VISIBLE);
        }

        if (!sponsorship.url.isEmpty()) {
            View.OnClickListener openSponsor = v -> {
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sponsorship.url)));
                } catch (Exception ignored) {
                    // No browser available — the tap is simply a no-op
                }
            };
            bannerView.setOnClickListener(openSponsor);
            if (sponsorText != null) {
                sponsorText.setOnClickListener(openSponsor);
            }
        }
    }
}
