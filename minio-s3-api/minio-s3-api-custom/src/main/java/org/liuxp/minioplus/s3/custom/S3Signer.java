package org.liuxp.minioplus.s3.custom;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class S3Signer {

    private static final String serviceName = "s3";
    private static final String US_EAST_1 = "us-east-1";
    private static final Set<String> IGNORED_HEADERS = CollUtil.set(true,"accept-encoding", "authorization", "content-type", "content-length", "user-agent");

    public static String build(String accessKey,String secretKey,ZonedDateTime date,Map<String, List<String>> headers,String params,String method,String path,String zeroSha256Hash){

        try {
            // 计算scope
            String scope = buildScope(date);

            // 计算signedHeaders
            Map<String, String> canonicalHeaders = buildCanonicalHeaders(headers);

            // 计算signedHeaders
            String signedHeaders = buildSignedHeaders(canonicalHeaders);

            // 计算canonicalQueryString
            String canonicalQueryString = buildCanonicalQueryString(params);

            // 计算buildCanonicalRequest
            String canonicalRequestHash = buildCanonicalRequest(canonicalHeaders,signedHeaders,canonicalQueryString,method,path,zeroSha256Hash);

            // 计算stringToSign
            String stringToSign = buildStringToSign(date,scope,canonicalRequestHash);

            // 计算signingKey
            byte[] signingKey = buildSigningKey(date,secretKey);

            // 计算signature
            String signature = buildSignature(signingKey,stringToSign);

            // 计算authorization
            String authorization = buildAuthorization(accessKey,scope,signedHeaders,signature);

            log.debug("httpRequest.headers()="+headers);
            log.debug("scope="+scope);
            log.debug("canonicalHeaders="+canonicalHeaders);
            log.debug("signedHeaders="+signedHeaders);
            log.debug("canonicalQueryString="+canonicalQueryString);
            log.debug("canonicalRequestHash="+canonicalRequestHash);
            log.debug("stringToSign="+stringToSign);
            log.debug("signature="+signature);
            log.debug("authorization="+authorization);
            
            return authorization;
        }catch (Exception e){
            throw new RuntimeException("S3签名失败", e);
        }
    }

    // 计算scope
    private static String buildScope(ZonedDateTime date){
        return date.format(Time.SIGNER_DATE_FORMAT) + "/" + US_EAST_1 + "/" + serviceName + "/aws4_request";
    }

    private static Map<String, String> buildCanonicalHeaders(Map<String, List<String>> headers){
        Map<String, String> canonicalHeaders = new TreeMap<>();

        for (String name : headers.keySet()) {
            String signedHeader = name.toLowerCase(Locale.US);
            if (!IGNORED_HEADERS.contains(signedHeader)) {
                // Convert and add header values as per
                // https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
                // * Header having multiple values should be converted to comma separated values.
                // * Multi-spaced value of header should be trimmed to single spaced value.
                canonicalHeaders.put(
                        signedHeader,
                        headers.get(name).stream()
                                .map(
                                        value -> {
                                            return value.replaceAll("( +)", " ");
                                        })
                                .collect(Collectors.joining(",")));
            }
        }
        return canonicalHeaders;
    }

    /**
     * 计算signedHeaders
     * @return
     */
    private static String buildSignedHeaders(Map<String, String> canonicalHeaders) {
        return StrUtil.join(";", canonicalHeaders.keySet());
    }

    private static String buildCanonicalQueryString(String params){

        if(StrUtil.isBlank(params)){
            return "";
        }

        return params;

        //TODO

//        MapUtil

        // Building a multimap which only order keys, ordering values is not performed
        // until MinIO server supports it.
//        Multimap<String, String> signedQueryParams =
//                MultimapBuilder.treeKeys().arrayListValues().build();
//
//        for (String queryParam : params.split("&")) {
//            String[] tokens = queryParam.split("=");
//            if (tokens.length > 1) {
//                signedQueryParams.put(tokens[0], tokens[1]);
//            } else {
//                signedQueryParams.put(tokens[0], "");
//            }
//        }
//
//        return Joiner.on("&").withKeyValueSeparator("=").join(signedQueryParams.entries());

    }

    private static String buildCanonicalRequest(Map<String, String> canonicalHeaders,String signedHeaders
            ,String canonicalQueryString,String method,String path,String zeroSha256Hash) throws NoSuchAlgorithmException {

        StringBuilder headers = new StringBuilder();
        for (String key : canonicalHeaders.keySet()) {

            headers.append(key);
            headers.append(":");
            headers.append(canonicalHeaders.get(key));
            headers.append("\n");
        }

        String canonicalRequest = method + "\n" + path + "\n" + canonicalQueryString + "\n"
                + headers + "\n" + signedHeaders + "\n" + zeroSha256Hash;
        log.debug("canonicalRequest="+canonicalRequest);

        byte[] data = canonicalRequest.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
        sha256Digest.update(data, 0, data.length);

        return HexUtil.encodeHexStr(sha256Digest.digest());
    }

    /**
     * 计算stringToSign
     * @param date
     * @param scope
     * @param canonicalRequestHash
     * @return
     */
    private static String buildStringToSign(ZonedDateTime date,String scope,String canonicalRequestHash){
        return  "AWS4-HMAC-SHA256" + "\n" + date.format(Time.AMZ_DATE_FORMAT) + "\n" + scope + "\n" + canonicalRequestHash;
    }

    /**
     * 计算signingKey
     * @param date
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private static byte[] buildSigningKey(ZonedDateTime date,String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        String aws4SecretKey = "AWS4" + secretKey;
        byte[] dateKey = sumHmac(aws4SecretKey.getBytes(StandardCharsets.UTF_8), date.format(Time.SIGNER_DATE_FORMAT).getBytes(StandardCharsets.UTF_8));
        byte[] dateRegionKey = sumHmac(dateKey, US_EAST_1.getBytes(StandardCharsets.UTF_8));
        byte[] dateRegionServiceKey = sumHmac(dateRegionKey, serviceName.getBytes(StandardCharsets.UTF_8));
        return sumHmac(dateRegionServiceKey, "aws4_request".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算signature
     * @param signingKey
     * @param stringToSign
     * @return
     */
    private static String buildSignature(byte[] signingKey,String stringToSign) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] digest = sumHmac(signingKey, stringToSign.getBytes(StandardCharsets.UTF_8));
        return HexUtil.encodeHexStr(digest);
    }

    /**
     * 计算authorization
     * @param accessKey
     * @param scope
     * @param signedHeaders
     * @param signature
     * @return
     */
    private static String buildAuthorization(String accessKey,String scope,String signedHeaders,String signature){
        return "AWS4-HMAC-SHA256 Credential=" + accessKey + "/" + scope + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;
    }

    private static byte[] sumHmac(byte[] key, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");

        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        mac.update(data);

        return mac.doFinal();
    }

    public static String md5Hash(byte[] data, int length) {
        try {
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            md5Digest.update(data, 0, length);
            return Base64.getEncoder().encodeToString(md5Digest.digest());
        }catch (Exception e){
            throw new RuntimeException("md5Hash失败", e);
        }

    }
}
