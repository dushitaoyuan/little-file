package com.taoyuanx.littlefile.server.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author 都市桃源
 * json 转换
 */
public class JSONUtil {
	private static ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
	}

	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public static String toJsonString(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static byte[] toJsonBytes(Object object) {
		try {
			return objectMapper.writeValueAsBytes(object);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static <T> T parseObject(String json, Class<T> type) {
		try {
			return objectMapper.readValue(json, type);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static <T> T parseObject(byte[] json, Class<T> type) {
		try {
			return objectMapper.readValue(json, type);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}
