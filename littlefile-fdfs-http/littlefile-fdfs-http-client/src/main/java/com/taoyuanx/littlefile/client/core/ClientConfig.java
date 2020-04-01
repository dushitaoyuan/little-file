package com.taoyuanx.littlefile.client.core;

import com.taoyuanx.littlefile.client.ex.FdfsException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * @author 都市桃源
 * 客户端必要配置文件
 */
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class ClientConfig {
    public static final String DEFAULT_CONFIG = "fdfs_http.properties";
    /**
     * 服务地址
     */
    private String fdfsHttpBaseUrl;
    /**
     * 连接超时时间
     */
    private Integer connectTimeout = 5;
    /**
     * 连接数
     */
    private Integer maxIdleConnections = 100;
    /**
     * 连接保持时间
     */
    private Integer keepAliveDuration = 6;
    /**
     * toekn
     */
    private String token;

    public ClientConfig(String configPath) {
        try {
            Properties config = new Properties();
            config.load(ClientConfig.class.getClassLoader().getResourceAsStream(configPath));
            if (config.containsKey("fdfsHttpBaseUrl")) {
                this.fdfsHttpBaseUrl = config.getProperty("fdfsHttpBaseUrl");
            }
            if (config.containsKey("connectTimeout")) {
                this.connectTimeout = Integer.parseInt(config.getProperty("connectTimeout"));
            }
            if (config.containsKey("maxIdleConnections")) {
                this.maxIdleConnections = Integer.parseInt(config.getProperty("maxIdleConnections"));
            }
            if (config.containsKey("keepAliveDuration")) {
                this.keepAliveDuration = Integer.parseInt(config.getProperty("keepAliveDuration"));
            }
            if (config.containsKey("token")) {
                this.token = config.getProperty("token");
            }


        } catch (Exception e) {
            log.error("加载默认配置[" + DEFAULT_CONFIG + "]失败", e);
            throw new FdfsException("加载默认配置[" + DEFAULT_CONFIG + "]失败", e);
        }
    }
}
