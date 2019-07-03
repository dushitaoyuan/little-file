package com.taoyuanx.littlefile.web.security;

import javax.crypto.Mac;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.HmacUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HmacTokenManager extends AbstractSimpleTokenManager {
	Mac mac = null;

	public HmacTokenManager(MacEum hmac, String password) {
		switch (hmac) {
			case MD5:
				mac = HmacUtils.getHmacMd5(password.getBytes());
				break;
			case SHA1:
				mac = HmacUtils.getHmacSha1(password.getBytes());
				break;
			case SHA256:
				mac = HmacUtils.getHmacSha256(password.getBytes());
				break;
		}
	}
	@Override
	public String create(Map<String, Object> signMap, Long expire, TimeUnit timeUnit) {
		Long end = System.currentTimeMillis() + timeUnit.toMillis(expire);
		if (signMap == null || signMap.isEmpty()) {
			signMap = new HashMap<>(1);
		}
		signMap.put(END_KEY, end);
		byte[] signData = JSON.toJSONBytes(signMap);
		String token = String.format(TOKEN_FMT,Base64.encodeBase64URLSafeString(signData),Base64.encodeBase64URLSafeString(mac.doFinal(signData)));
		return token;
	}

	@Override
	public Map<String, Object> vafy(String token) {
		try {
			if (null == token || "".equals(token)) {
				return null;
			}
			String[] split = token.split("\\.");
			if (split.length > 2) {
				throw new TokenException("token格式非法");
			}
			byte[] signData = Base64.decodeBase64(split[0].getBytes());
			JSONObject signObj = JSON.parseObject(StringUtils.newString(signData, "UTF-8"));
			Long end = signObj.getLong(END_KEY);
			if (end < System.currentTimeMillis()) {
				throw new TokenException("token过期");
			}
			String tokenC = Base64.encodeBase64URLSafeString(mac.doFinal(signData));
			if (tokenC.equals(split[1])) {
				return signObj;
			}
			throw new TokenException("token校验失败");
		} catch (Exception e) {
			if (e instanceof TokenException) {
				throw e;
			} else {
				throw new TokenException("token校验失败", e);
			}
		}
	}
}
