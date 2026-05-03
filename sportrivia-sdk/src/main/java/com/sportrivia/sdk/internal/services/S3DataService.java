package com.sportrivia.sdk.internal.services;

import com.sportrivia.sdk.public_api.Sport;
import com.sportrivia.sdk.public_api.SporTriviaCredentialProvider;
import com.sportrivia.sdk.public_api.SporTriviaLogger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * Handles all S3 data operations via the credential provider (no AWS SDK dependency).
 */
public class S3DataService {
    private final SporTriviaCredentialProvider credentialProvider;

    public S3DataService(SporTriviaCredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    public byte[] download(String s3Key) throws Exception {
        SporTriviaLogger.debug("Requesting presigned GET URL for: " + s3Key);
        String urlStr;
        try {
            urlStr = credentialProvider.getPresignedGetUrl(s3Key);
            SporTriviaLogger.debug("Got presigned URL: " + urlStr.substring(0, Math.min(80, urlStr.length())) + "...");
        } catch (Exception e) {
            SporTriviaLogger.error("Credential provider failed for GET '" + s3Key + "': " + e.getMessage());
            throw e;
        }

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            SporTriviaLogger.error("Download failed for '" + s3Key + "': HTTP " + responseCode);
            throw new Exception("Failed to download '" + s3Key + "' (HTTP " + responseCode + "). Check that your credential provider returns valid presigned URLs.");
        }

        InputStream is = conn.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        is.close();
        conn.disconnect();

        SporTriviaLogger.info("Downloaded '" + s3Key + "' (" + baos.size() + " bytes)");
        return baos.toByteArray();
    }

    public void upload(String s3Key, byte[] data) throws Exception {
        String urlStr = credentialProvider.getPresignedPutUrl(s3Key);
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        OutputStream os = conn.getOutputStream();
        os.write(data);
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        conn.disconnect();
        if (responseCode < 200 || responseCode >= 300) {
            throw new Exception("Failed to upload to S3: " + s3Key + " (HTTP " + responseCode + ")");
        }
    }

    public byte[] downloadAnswerKey(String customFileName) throws Exception {
        return download("answer_keys/custom/" + customFileName + ".json");
    }

    public byte[] downloadPlayerList(Sport sport) throws Exception {
        return download("answer_keys/" + sport.getCode() + "/all_" + sport.getCode() + "_players.json");
    }

    public byte[] downloadTeamImage(Sport sport, String teamAbbr) throws Exception {
        String key = "team_images/" + sport.getSportCategory() + "/" + sport.getLeagueKey() + "/" + teamAbbr + ".png";
        return download(key);
    }

    public void uploadGameResults(Sport sport, String teamName, String suffix, byte[] data) throws Exception {
        String folder = "custom/" + sport.getLeagueKey() + "/" + teamName + "/" + suffix;
        String key = folder + "/" + UUID.randomUUID().toString() + ".json";
        upload(key, data);
    }
}
