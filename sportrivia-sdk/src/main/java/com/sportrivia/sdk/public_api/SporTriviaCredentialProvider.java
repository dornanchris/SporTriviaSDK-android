package com.sportrivia.sdk.public_api;

/**
 * Interface that host apps implement to provide S3 access.
 * <p>
 * The SDK never handles raw AWS credentials. Instead, the host app
 * generates presigned URLs using whatever auth mechanism it prefers.
 * <p>
 * Methods are called on background threads.
 */
public interface SporTriviaCredentialProvider {
    String getPresignedGetUrl(String s3Key) throws Exception;
    String getPresignedPutUrl(String s3Key) throws Exception;
}
