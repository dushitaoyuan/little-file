package com.taoyuanx.littlefile.web.security;

import javax.crypto.Mac;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;

public class HmacTokenManager extends AbstractSimpleTokenManager {
	Mac mac=null;
	public HmacTokenManager(MacEum hmac,String password) {
		switch (hmac) {
		case MD5:
			mac=HmacUtils.getHmacMd5(password.getBytes());
			break;
		case SHA1:
			mac=HmacUtils.getHmacSha1(password.getBytes());
			break;
		case SHA256:
			mac=HmacUtils.getHmacSha256(password.getBytes());
			break;
		}
	}
	@Override
	public String create(String str) {
		return Base64.encodeBase64URLSafeString(mac.doFinal(str.getBytes()));
	}

	@Override
	public boolean verify(String str,String mac) {
		return mac.equals(create(str));
	}
	public static void main(String[] args) {
		HmacTokenManager m=new HmacTokenManager(MacEum.MD5, "md5");
		System.out.println(m.create("123.png"));
	}

}
