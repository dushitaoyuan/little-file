package com.taoyuanx.littlefile.client.core;

import com.taoyuanx.littlefile.client.ex.FdfsException;
import com.taoyuanx.littlefile.client.utils.StrUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

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
     * 连接超时时间 默认 5 秒
     */
    private Integer connectTimeout;
    /**
     * 连接数 默认 100
     */
    private Integer maxIdleConnections;
    /**
     * 连接保持时间 默认 15秒
     */
    private Integer keepAliveDuration;
    /**
     * toekn
     */
    private String token;


    /**
     * 下载分块大小 默认4m
     */
    private Long downLoadChunkSize;
    /**
     * 上传分块大小 默认4m
     */
    private Long uploadChunkSize;
    /**
     * 大文件分片下载线程数 默认3
     */
    private Integer downLoadThreadNum = 3;
    private ThreadPoolExecutor downLoadPool;

    public ClientConfig(String configPath) {
        try {
            Properties config = new Properties();
            config.load(ClientConfig.class.getClassLoader().getResourceAsStream(configPath));
            this.fdfsHttpBaseUrl = getProperty(config, "fdfsHttpBaseUrl");
            this.connectTimeout = getProperty(config, Integer.class, "connectTimeout", 5);
            this.maxIdleConnections = this.connectTimeout = getProperty(config, Integer.class, "maxIdleConnections", 100);
            this.keepAliveDuration = this.maxIdleConnections = this.connectTimeout = getProperty(config, Integer.class, "keepAliveDuration", 15);
            this.token = getProperty(config, "token");
            this.downLoadChunkSize = getProperty(config, Long.class, "downLoadChunkSize", 4L << 20);
            this.uploadChunkSize = getProperty(config, Long.class, "uploadChunkSize", 4L << 20);
            this.downLoadThreadNum= getProperty(config, Integer.class, "uploadChunkSize", 3);
        } catch (FdfsException e) {
            throw e;
        } catch (Exception e) {
            log.error("加载默认配置[" + DEFAULT_CONFIG + "]失败", e);
            throw new FdfsException("加载默认配置[" + DEFAULT_CONFIG + "]失败", e);
        }
    }


    private <T> T getProperty(Properties config, Class<T> type, String key, T defaultValue) {

        String value = config.getProperty(key);
        if (StrUtil.isEmpty(value)) {
            return defaultValue;
        }
        if (type.equals(String.class)) {
            return (T) value;
        }
        try {
            if (type.equals(Long.class)) {
                Long result = Long.parseLong(value);
                return (T) result;
            }
            if (type.equals(Integer.class)) {
                Integer result = Integer.parseInt(value);
                return (T) result;
            }
            if (type.equals(Boolean.class)) {
                Boolean result = Boolean.valueOf(value);
                return (T) result;
            }

        } catch (Exception e) {
        }
        return defaultValue;

    }

    private String getProperty(Properties config, String key) {
        String value = config.getProperty(key);
        if (StrUtil.isEmpty(value)) {
            throw new FdfsException("config 异常:" + key);
        }
        return value;

    }

}
