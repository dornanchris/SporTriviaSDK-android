package com.sportrivia.sdk;

import com.sportrivia.sdk.internal.services.S3DataService;
import com.sportrivia.sdk.public_api.Sport;
import com.sportrivia.sdk.public_api.SporTriviaCredentialProvider;

import org.junit.Test;

import static org.junit.Assert.*;

public class S3DataServiceTest {

    /**
     * Records the key requested for a presigned PUT, then returns an
     * unparseable URL so the upload throws before any network I/O.
     */
    private static class CapturingProvider implements SporTriviaCredentialProvider {
        String lastPutKey;

        @Override
        public String getPresignedGetUrl(String s3Key) {
            return "bogus://stop";
        }

        @Override
        public String getPresignedPutUrl(String s3Key) {
            lastPutKey = s3Key;
            return "bogus://stop";
        }
    }

    @Test
    public void uploadGameResultsUsesResponsePath() {
        CapturingProvider provider = new CapturingProvider();
        S3DataService service = new S3DataService(provider);

        try {
            service.uploadGameResults("custom/MLB/CD Test/who-holds-the-home-run-record/responses/", new byte[0]);
        } catch (Exception ignored) {
            // expected: the bogus URL aborts the upload after key construction
        }

        assertNotNull(provider.lastPutKey);
        assertTrue(provider.lastPutKey.startsWith("custom/MLB/CD Test/who-holds-the-home-run-record/responses/"));
        assertFalse("trailing slash in response_path must not produce a double slash",
                provider.lastPutKey.contains("//"));
        assertTrue(provider.lastPutKey.endsWith(".json"));
    }

    @Test
    public void uploadGameResultsResponsePathWithoutTrailingSlash() {
        CapturingProvider provider = new CapturingProvider();
        S3DataService service = new S3DataService(provider);

        try {
            service.uploadGameResults("custom/NHL/New York Islanders/some-question/responses", new byte[0]);
        } catch (Exception ignored) {
        }

        assertNotNull(provider.lastPutKey);
        assertTrue(provider.lastPutKey.startsWith("custom/NHL/New York Islanders/some-question/responses/"));
        assertFalse(provider.lastPutKey.contains("//"));
    }

    @Test
    public void legacyUploadGameResultsPathUnchanged() {
        CapturingProvider provider = new CapturingProvider();
        S3DataService service = new S3DataService(provider);

        try {
            service.uploadGameResults(Sport.MLB, "New York Yankees", "NYM", new byte[0]);
        } catch (Exception ignored) {
        }

        assertNotNull(provider.lastPutKey);
        assertTrue(provider.lastPutKey.startsWith("custom/MLB/New York Yankees/NYM/"));
        assertTrue(provider.lastPutKey.endsWith(".json"));
    }
}
