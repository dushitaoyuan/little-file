package com.taoyuanx.littlefile.combine.core.store.minio;

import okhttp3.OkHttpClient;

/**
 * @author dushitaoyuan
 * @desc minio配置
 * @date 2020/9/18
 */

public class MinioConfig {
    private String bucketName;
    private String accessKey;

    private String secretKey;

    private String endpoint;
    private String region;

    private OkHttpClient okHttpClient;


    private Integer connectTimeout = 3;
    private Integer maxIdleConnections = 150;
    private Integer keepAliveDuration = 30;

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(Integer maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public Integer getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public void setKeepAliveDuration(Integer keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
