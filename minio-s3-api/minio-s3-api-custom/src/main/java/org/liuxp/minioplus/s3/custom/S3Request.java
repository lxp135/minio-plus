package org.liuxp.minioplus.s3.custom;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class S3Request {

    static String ZERO_MD5_HASH = "1B2M2Y8AsgTpgAmY7PhCfg==";
    static String ZERO_SHA256_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    public static HttpResponse request(String accessKey,String secretKey,String backend,String url, Object body, String path, String method, ZonedDateTime date,int length){


        String md5Hash = ZERO_MD5_HASH;
        if (body instanceof byte[]) {
            md5Hash = S3Signer.md5Hash((byte[]) body, length);
        }

        // 创建除Authorization外所有header
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Host", CollUtil.newArrayList(backend.replace("http://","").replace("https://","")));
        headers.put("Accept-Encoding", CollUtil.newArrayList("identity"));
        headers.put("User-Agent", CollUtil.newArrayList("MinIO (Windows 10; amd64) minio-java/8.3.3"));
        headers.put("Content-MD5", CollUtil.newArrayList(md5Hash));
        headers.put("x-amz-content-sha256", CollUtil.newArrayList(ZERO_SHA256_HASH));
        headers.put("x-amz-date", CollUtil.newArrayList(date.format(Time.AMZ_DATE_FORMAT)));

        // 计算authorization
        String authorization = S3Signer.build(accessKey,secretKey,date,headers,"",method,path,ZERO_SHA256_HASH);

        HttpRequest httpRequest = HttpUtil.createRequest(Method.HEAD, url);
        httpRequest.header(headers,true);
        httpRequest.header("Authorization", authorization);

        return httpRequest.execute();
    }





}
