package com.sportrivia.sdk.public_api;

/**
 * Configuration for initializing the SporTrivia SDK.
 */
public class SporTriviaConfiguration {
    private final String s3BucketName;
    private final SporTriviaCredentialProvider credentialProvider;
    private final SporTriviaTheme theme;

    private SporTriviaConfiguration(Builder builder) {
        this.s3BucketName = builder.s3BucketName;
        this.credentialProvider = builder.credentialProvider;
        this.theme = builder.theme;
    }

    public String getS3BucketName() { return s3BucketName; }
    public SporTriviaCredentialProvider getCredentialProvider() { return credentialProvider; }
    public SporTriviaTheme getTheme() { return theme; }

    public static class Builder {
        private String s3BucketName = "sportrivia";
        private SporTriviaCredentialProvider credentialProvider;
        private SporTriviaTheme theme;

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

        public SporTriviaConfiguration build() {
            if (credentialProvider == null) {
                throw new IllegalStateException("credentialProvider is required");
            }
            return new SporTriviaConfiguration(this);
        }
    }
}
