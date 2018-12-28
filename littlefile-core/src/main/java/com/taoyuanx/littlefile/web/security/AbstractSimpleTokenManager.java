package com.taoyuanx.littlefile.web.security;

/**
 * @author 都市桃源
 * 2018年12月28日
 * 简单安全控制
 */
public abstract class AbstractSimpleTokenManager {
	public abstract String create(String str);
	public abstract boolean verify(String str,String mac);
}
