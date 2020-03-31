package com.taoyuanx.littlefile.client;

/**
 * @author 都市桃源
 * 客户端必要配置文件
 */
public class ClientConfig {
	
	private String serverUrl="http://localhost:8080/"; //服务地址
	private Integer connectTimeout=5;//连接超时时间
	private Integer maxIdleConnections=50;//连接数
	private Integer keepAliveDuration=6;//连接保持时间
	private String token;
	
	

	public ClientConfig() {
		super();
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
