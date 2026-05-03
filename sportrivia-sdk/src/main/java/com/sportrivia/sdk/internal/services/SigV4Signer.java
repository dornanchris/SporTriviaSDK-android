package com.sportrivia.sdk.internal.services;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Generates AWS Signature Version 4 presigned URLs for S3.
 * <p>
 * Reference: https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-query-string-auth.html
 * <p>
 * Self-contained implementation — the SDK has no AWS SDK dependency.
 * Uses only javax.crypto.Mac (HMAC-SHA256) and java.security.MessageDigest (SHA-256).
 */
public class SigV4Signer {
    private final String accessKey;
    private final String secretKey;
    private final String region;
    private final String sessionToken;
    private static final String SERVICE = "s3";

    public SigV4Signer(String accessKey, String secretKey, String region, String sessionToken) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.sessionToken = sessionToken;
    }

    public String presignedURL(String method, String bucket, String key, int expiresInSeconds) throws Exception {
        Date now = new Date();
        SimpleDateFormat amzDateFmt = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US);
        amzDateFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat dateStampFmt = new SimpleDateFormat("yyyyMMdd", Locale.US);
        dateStampFmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        String amzDate = amzDateFmt.format(now);
        String dateStamp = dateStampFmt.format(now);

        String host = bucket + ".s3." + region + ".amazonaws.com";

        StringBuilder canonicalURI = new StringBuilder();
        for (String segment : key.split("/")) {
            if (segment.isEmpty()) continue;
            canonicalURI.append("/").append(uriEncode(segment, true));
        }
        if (canonicalURI.length() == 0) canonicalURI.append("/");

        String credentialScope = dateStamp + "/" + region + "/" + SERVICE + "/aws4_request";

        List<String[]> queryParams = new ArrayList<>();
        queryParams.add(new String[]{"X-Amz-Algorithm", "AWS4-HMAC-SHA256"});
        queryParams.add(new String[]{"X-Amz-Credential", accessKey + "/" + credentialScope});
        queryParams.add(new String[]{"X-Amz-Date", amzDate});
        queryParams.add(new String[]{"X-Amz-Expires", String.valueOf(expiresInSeconds)});
        queryParams.add(new String[]{"X-Amz-SignedHeaders", "host"});
        if (sessionToken != null) {
            queryParams.add(new String[]{"X-Amz-Security-Token", sessionToken});
        }
        Collections.sort(queryParams, new Comparator<String[]>() {
            @Override public int compare(String[] a, String[] b) { return a[0].compareTo(b[0]); }
        });

        StringBuilder canonicalQuery = new StringBuilder();
        for (int i = 0; i < queryParams.size(); i++) {
            if (i > 0) canonicalQuery.append("&");
            canonicalQuery.append(uriEncode(queryParams.get(i)[0], true))
                .append("=")
                .append(uriEncode(queryParams.get(i)[1], true));
        }

        String canonicalHeaders = "host:" + host + "\n";
        String signedHeaders = "host";
        String payloadHash = "UNSIGNED-PAYLOAD";
        String canonicalRequest = method + "\n"
            + canonicalURI + "\n"
            + canonicalQuery + "\n"
            + canonicalHeaders + "\n"
            + signedHeaders + "\n"
            + payloadHash;

        String stringToSign = "AWS4-HMAC-SHA256\n"
            + amzDate + "\n"
            + credentialScope + "\n"
            + sha256Hex(canonicalRequest);

        byte[] kDate = hmac(("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8), dateStamp.getBytes(StandardCharsets.UTF_8));
        byte[] kRegion = hmac(kDate, region.getBytes(StandardCharsets.UTF_8));
        byte[] kService = hmac(kRegion, SERVICE.getBytes(StandardCharsets.UTF_8));
        byte[] kSigning = hmac(kService, "aws4_request".getBytes(StandardCharsets.UTF_8));

        byte[] signatureBytes = hmac(kSigning, stringToSign.getBytes(StandardCharsets.UTF_8));
        String signature = toHex(signatureBytes);

        return "https://" + host + canonicalURI + "?" + canonicalQuery + "&X-Amz-Signature=" + signature;
    }

    private static String uriEncode(String input, boolean encodeSlash) {
        try {
            String encoded = URLEncoder.encode(input, "UTF-8")
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
            if (!encodeSlash) {
                encoded = encoded.replace("%2F", "/");
            }
            return encoded;
        } catch (Exception e) {
            return input;
        }
    }

    private static String sha256Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return toHex(digest);
    }

    private static byte[] hmac(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
