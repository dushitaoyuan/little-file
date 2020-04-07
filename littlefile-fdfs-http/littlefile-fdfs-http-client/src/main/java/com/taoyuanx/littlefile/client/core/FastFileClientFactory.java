package com.taoyuanx.littlefile.client.core;

import com.taoyuanx.littlefile.client.core.ClientConfig;
import com.taoyuanx.littlefile.fdfshttp.core.client.FileClient;

/**
 * @author 都市桃源
 * 文件客户端工厂
 */
public interface FastFileClientFactory {
    FileClient fileClient();

}
