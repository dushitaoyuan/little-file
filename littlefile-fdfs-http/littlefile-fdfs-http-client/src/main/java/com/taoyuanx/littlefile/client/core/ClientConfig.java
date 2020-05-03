package com.taoyuanx.littlefile.client.core;

import com.taoyuanx.littlefile.client.ex.FdfsException;
import com.taoyuanx.littlefile.client.impl.loadbalance.ILoadbalance;
import com.taoyuanx.littlefile.client.impl.loadbalance.LoadbalanceEnum;
import com.taoyuanx.littlefile.client.utils.ServerUtil;
import com.taoyuanx.littlefile.client.utils.StrUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
    private List<FileServer> fdfsServer;
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
     * 心跳监测时间间隔 默认5秒
     */
    private Long heartIdleTime;
    /**
     * 负载策略 支持Random(随机),Round(轮询)
     */
    private ILoadbalance loadbalance;
    /**
     * client Holder
     */
    private OkHttpClient okHttpClient;
    /**
     * client api
     */
    private Map<FdfsApi, String> apiMap;


    /**
     * 下载分块大小 默认4m
     */
    private Long downLoadChunkSize;
    /**
     * 上传分块大小 默认4m
     */
    private Long uploadChunkSize;


    public ClientConfig(String configPath) {
        try {
            Properties config = new Properties();
            config.load(ClientConfig.class.getClassLoader().getResourceAsStream(configPath));
            this.fdfsServer = Arrays.asList(getProperty(config, "fdfsServer").split(",")).stream().map(FileServer::new).collect(Collectors.toList());
            this.connectTimeout = getProperty(config, Integer.class, "connectTimeout", 5);
            this.maxIdleConnections = this.connectTimeout = getProperty(config, Integer.class, "maxIdleConnections", 100);
            this.keepAliveDuration = this.maxIdleConnections = this.connectTimeout = getProperty(config, Integer.class, "keepAliveDuration", 15);
            this.token = getProperty(config, "token");
            this.downLoadChunkSize = getProperty(config, Long.class, "downLoadChunkSize", 4L << 20);
            this.uploadChunkSize = getProperty(config, Long.class, "uploadChunkSize", 4L << 20);
            this.loadbalance = LoadbalanceEnum.valueOf(getProperty(config, String.class, "loadbalance", LoadbalanceEnum.Round.name())).getLoadbalance();
            this.heartIdleTime = getProperty(config, Long.class, "heartIdleTime", 5L);
            /**
             * 定时心跳监测
             */
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            ClientConfig clientConfig = this;
            scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    ServerUtil.heartBeatCheck(clientConfig);
                }
            }, 5000L, this.heartIdleTime, TimeUnit.SECONDS);
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
