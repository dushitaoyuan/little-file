package com.taoyuanx.littlefile.client;

/**
 * @author 都市桃源
 * 文件客户端工厂
 */
public interface FastFileClientFactory {
	void setConfig(ClientConfig config);
	FileClient getFileClient();
}
