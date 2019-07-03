package com.taoyuanx.littlefile.web.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.HmacUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 都市桃源
 * 2018年12月28日
 * 简单安全控制
 */
public abstract class AbstractSimpleTokenManager {
	protected static final String END_KEY = "end";
	protected static final String TOKEN_FMT = "%s.%s";
	/**
	 *
	 * @param signData 签名内容
	 * @param expire 过期时间
	 * @param timeUnit
	 * @return token
	 */
	public  abstract String create(Map<String, Object> signData, Long expire, TimeUnit timeUnit);

	/**
	 *
	 * @param token
	 * @return 签名内容
	 */
	public abstract Map<String, Object> vafy(String token);








}
