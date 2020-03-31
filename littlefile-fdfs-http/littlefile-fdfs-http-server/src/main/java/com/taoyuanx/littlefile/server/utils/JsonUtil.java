package com.taoyuanx.littlefile.server.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author 都市桃源
 * json 转换
 */
public class JsonUtil {
	public static  ObjectMapper MAPPER;
	
	static{
		MAPPER=new ObjectMapper();
		MAPPER.setSerializationInclusion(Include.NON_NULL); 
	}
	public static String toJson(Object obj) throws JsonProcessingException{
		return MAPPER.writeValueAsString(obj);
	}
		
}
