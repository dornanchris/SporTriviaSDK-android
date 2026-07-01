package com.sportrivia.sdk.public_api;

/**
 * Configuration for initializing the SporTrivia SDK.
 */
public class SporTriviaConfiguration {
    private final String s3BucketName;
    private final SporTriviaCredentialProvider credentialProvider;
    private final SporTriviaTheme theme;
    private final String partnerId;
    private final String redirectBaseUrl;
    private final SporTriviaDelegate defaultDelegate;

    private SporTriviaConfiguration(Builder builder) {
        this.s3BucketName = builder.s3BucketName;
        this.credentialProvider = builder.credentialProvider;
        this.theme = builder.theme;
        this.partnerId = builder.partnerId;
        this.redirectBaseUrl = builder.redirectBaseUrl;
        this.defaultDelegate = builder.defaultDelegate;
    }

    public String getS3BucketName() { return s3BucketName; }
    public SporTriviaCredentialProvider getCredentialProvider() { return credentialProvider; }
    public SporTriviaTheme getTheme() { return theme; }

    /**
     * The partner id assigned by the SporTrivia team. Used to build the
     * deep-link scheme ({@code sportrivia-<partnerId>}) and to claim games
     * saved off on the SDK redirect page before install.
     */
    public String getPartnerId() { return partnerId; }

    /**
     * Base URL of the SporTrivia redirect service (e.g.
     * {@code https://sportrivia-app.com}). Required only for deferred
     * post-install game claiming.
     */
    public String getRedirectBaseUrl() { return redirectBaseUrl; }

    /**
     * Optional delegate used when a game is launched from a deep link handled
     * by {@link com.sportrivia.sdk.internal.ui.SporTriviaDeepLinkActivity}.
     */
    public SporTriviaDelegate getDefaultDelegate() { return defaultDelegate; }

    public static class Builder {
        private String s3BucketName = "sportrivia";
        private SporTriviaCredentialProvider credentialProvider;
        private SporTriviaTheme theme;
        private String partnerId;
        private String redirectBaseUrl;
        private SporTriviaDelegate defaultDelegate;

        public Builder(SporTriviaCredentialProvider credentialProvider) {
            this.credentialProvider = credentialProvider;
        }

        /**
         * Convenience constructor for licensed SDK partners.
         */
        public Builder(SporTriviaCredentials credentials) {
            this.credentialProvider = new DefaultCredentialProvider(credentials, "sportrivia");
        }

        public Builder s3BucketName(String bucketName) {
            this.s3BucketName = bucketName;
            return this;
        }

        public Builder theme(SporTriviaTheme theme) {
            this.theme = theme;
            return this;
        }

        public Builder partnerId(String partnerId) {
            this.partnerId = partnerId;
            return this;
        }

        public Builder redirectBaseUrl(String redirectBaseUrl) {
            this.redirectBaseUrl = redirectBaseUrl;
            return this;
        }

        public Builder defaultDelegate(SporTriviaDelegate delegate) {
            this.defaultDelegate = delegate;
            return this;
        }

        public SporTriviaConfiguration build() {
            if (credentialProvider == null) {
                throw new IllegalStateException("credentialProvider is required");
            }
            return new SporTriviaConfiguration(this);
        }
    }
}
