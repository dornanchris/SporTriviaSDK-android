package com.sportrivia.sdk.public_api;

import com.sportrivia.sdk.internal.services.SigV4Signer;

/**
 * Default credential provider that generates presigned S3 URLs using
 * partner-issued AWS credentials.
 */
public final class DefaultCredentialProvider implements SporTriviaCredentialProvider {
    private final SigV4Signer signer;
    private final String bucketName;
    private final int expirationSeconds;

    public DefaultCredentialProvider(SporTriviaCredentials credentials, String bucketName) {
        this(credentials, bucketName, 300);
    }

    public DefaultCredentialProvider(SporTriviaCredentials credentials, String bucketName, int expirationSeconds) {
        this.signer = new SigV4Signer(
            credentials.accessKey,
            credentials.secretKey,
            credentials.region,
            credentials.sessionToken
        );
        this.bucketName = bucketName;
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public String getPresignedGetUrl(String s3Key) throws Exception {
        return signer.presignedURL("GET", bucketName, s3Key, expirationSeconds);
    }

    @Override
    public String getPresignedPutUrl(String s3Key) throws Exception {
        return signer.presignedURL("PUT", bucketName, s3Key, expirationSeconds);
    }
}
