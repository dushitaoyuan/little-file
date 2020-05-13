package com.taoyuanx.littlefile.client.core;

import com.taoyuanx.littlefile.client.ex.FdfsException;
import com.taoyuanx.littlefile.client.impl.loadbalance.ILoadbalance;
import com.taoyuanx.littlefile.client.impl.loadbalance.LoadbalanceEnum;
import com.taoyuanx.littlefile.client.utils.ServerUtil;
import com.taoyuanx.littlefile.client.utils.StrUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 都市桃源
 * 客户端必要配置文件
 */
@Getter
@Setter
@Slf4j
public class ClientConfig {
    public static final String DEFAULT_CONFIG = "littlefile-fdfs.properties";
    public static final String CONFIG_PREFIX = "littlefile.client.";
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
     * 心跳监测时间间隔 默认30秒
     */
    private Integer heartIdleTime;
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
     * 心跳监测 定时器
     */
    private static ScheduledExecutorService fileServerCheckPool = Executors.newScheduledThreadPool(1);

    /**
     * 下载分块大小 默认4m
     */
    private Long downLoadChunkSize;
    /**
     * 上传分块大小 默认4m
     */
    private Long uploadChunkSize;

    public ClientConfig() {
        this(DEFAULT_CONFIG);
    }

    public ClientConfig(String configPath) {
        try {
            Properties config = new Properties();
            config.load(ClientConfig.class.getClassLoader().getResourceAsStream(configPath));
            this.fdfsServer = Arrays.asList(getProperty(config, CONFIG_PREFIX, "fdfsServer").split(",")).stream().map(FileServer::new).collect(Collectors.toList());
            this.connectTimeout = getProperty(config, Integer.class, CONFIG_PREFIX, "connectTimeout", 5);
            this.maxIdleConnections = this.connectTimeout = getProperty(config, Integer.class, CONFIG_PREFIX, "maxIdleConnections", 100);
            this.keepAliveDuration = this.maxIdleConnections = this.connectTimeout = getProperty(config, Integer.class, CONFIG_PREFIX, "keepAliveDuration", 15);
            this.token = getProperty(config, CONFIG_PREFIX, "token");
            this.downLoadChunkSize = getProperty(config, Long.class, CONFIG_PREFIX, "downLoadChunkSize", 4L << 20);
            this.uploadChunkSize = getProperty(config, Long.class, CONFIG_PREFIX, "uploadChunkSize", 4L << 20);
            this.loadbalance = LoadbalanceEnum.valueOf(getProperty(config, String.class, CONFIG_PREFIX, "loadbalance", LoadbalanceEnum.Round.name())).getLoadbalance();
            this.heartIdleTime = getProperty(config, Integer.class, CONFIG_PREFIX, "heartIdleTime", 30);
            ClientConfig myClientConfig = this;
            //定时心跳检测
            fileServerCheckPool.scheduleAtFixedRate(() -> {
                try {
                    ServerUtil.heartBeatCheck(clientConfig);
                } catch (Exception e) {
                    log.warn("server check error", e);
                }
            }, 30, this.heartIdleTime, TimeUnit.SECONDS);

        } catch (FdfsException e) {
            throw e;
        } catch (Exception e) {
            log.error("加载默认配置[" + DEFAULT_CONFIG + "]失败", e);
            throw new FdfsException("加载默认配置[" + DEFAULT_CONFIG + "]失败", e);
        }
    }


    private <T> T getProperty(Properties config, Class<T> type, String configPrefix, String key, T defaultValue) {
        if (StrUtil.isNotEmpty(configPrefix)) {
            key = configPrefix + key;
        }
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

    private String getProperty(Properties config, String configPrefix, String key) {
        if (StrUtil.isNotEmpty(configPrefix)) {
            key = configPrefix + key;
        }
        String value = config.getProperty(key);
        if (StrUtil.isEmpty(value)) {
            throw new FdfsException("config 异常:" + key);
        }
        return value;
    }

}
