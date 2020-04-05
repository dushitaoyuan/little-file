package com.taoyuanx.littlefile.client.core;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author dushitaoyuan
 * @desc map 请求参数
 * @date 2019/12/17
 */
public class ParamBuilder {
    private Map<String, Object> paramMap;

    private ParamBuilder() {
        paramMap = new HashMap<>();
    }

    private ParamBuilder(int size) {
        paramMap = new HashMap<>(size);
    }

    public static ParamBuilder newBuilder(int size) {
        return new ParamBuilder(size);
    }

    public static ParamBuilder newBuilder() {
        return new ParamBuilder();
    }

    public ParamBuilder addParam(String key, Object value) {
        paramMap.put(key, value);
        return this;
    }

    public Map<String, Object> build() {
        return paramMap;
    }

    public String buildUrl(String baseUrl) {
        if (paramMap.isEmpty()) {
            return baseUrl;
        }
        StringBuilder urlBuilder = new StringBuilder("?");
        paramMap.entrySet().stream().filter(entry -> {
            Object value = entry.getValue();
            if (Objects.nonNull(value) && Objects.nonNull(entry.getKey())) {
                return !(value instanceof InputStream || value instanceof byte[] || value instanceof File);
            }
            return false;
        }).forEach(entry -> {
            urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        });
        if (urlBuilder.length() == 1) {
            return baseUrl;
        } else if (urlBuilder.charAt(urlBuilder.length() - 1) == '&') {
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        return baseUrl + urlBuilder.toString();
    }
}
