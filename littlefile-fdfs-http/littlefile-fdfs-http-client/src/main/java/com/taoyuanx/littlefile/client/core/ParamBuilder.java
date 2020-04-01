package com.taoyuanx.littlefile.client.core;

import java.util.HashMap;
import java.util.Map;

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
}
