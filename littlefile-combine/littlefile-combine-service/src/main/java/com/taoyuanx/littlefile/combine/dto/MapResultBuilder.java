package com.taoyuanx.littlefile.combine.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dushitaoyuan
 * @desc 构造map结果
 * @date 2019/12/17
 */
public class MapResultBuilder {
    private Map<String, Object> mapResult;

    private MapResultBuilder() {
        mapResult = new HashMap<>();
    }

    private MapResultBuilder(int size) {
        mapResult = new HashMap<>(size);
    }

    public static MapResultBuilder newBuilder(int size) {
        return new MapResultBuilder(size);
    }

    public static MapResultBuilder newBuilder() {
        return new MapResultBuilder();
    }

    public MapResultBuilder put(String key, Object value) {
        mapResult.put(key, value);
        return this;
    }

    public Map<String, Object> build() {
        return mapResult;
    }
}
