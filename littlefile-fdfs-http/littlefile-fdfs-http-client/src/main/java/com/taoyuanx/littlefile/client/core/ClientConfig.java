package com.taoyuanx.littlefile.client.core;

import com.taoyuanx.littlefile.client.ex.FdfsException;
import com.taoyuanx.littlefile.client.impl.loadbalance.FdfsApi;
import com.taoyuanx.littlefile.client.impl.loadbalance.FileServer;
import com.taoyuanx.littlefile.client.impl.loadbalance.ILoadbalance;
import com.taoyuanx.littlefile.client.impl.loadbalance.LoadbalanceEnum;
import com.taoyuanx.littlefile.client.utils.ServerUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
     * 心跳监测 定时器
     */
    private static Timer fileServerTimer = new Timer();

    public ClientConfig(String configPath) {
        try {
            Properties config = new Properties();
            config.load(ClientConfig.class.getClassLoader().getResourceAsStream(configPath));
            if (config.containsKey("fdfsServer")) {
                this.fdfsServer = Arrays.asList(config.getProperty("fdfsServer").split(",")).stream().map(FileServer::new).collect(Collectors.toList());
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

            if (config.containsKey("loadbalance")) {
                this.loadbalance = LoadbalanceEnum.valueOf(config.getProperty("loadbalance")).getLoadbalance();
            } else {
                this.loadbalance = LoadbalanceEnum.Round.getLoadbalance();
            }
            if (config.containsKey("heartIdleTime")) {
                this.heartIdleTime = Long.parseLong(config.getProperty("heartIdleTime"));
                ;
            } else {
                this.heartIdleTime = 5000L;
            }
            ClientConfig myClientConfig = this;
            fileServerTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        fdfsServer.stream().filter(server -> {
                            return !server.isAlive();
                        }).forEach(server -> {
                            ServerUtil.checkServerAlive(server, myClientConfig);
                        });
                    } catch (Exception e) {
                        log.warn("server check error", e);
                    }
                }
            }, 30 * 1000L, this.heartIdleTime);
        } catch (Exception e) {
            log.error("加载默认配置[" + DEFAULT_CONFIG + "]失败", e);
            throw new FdfsException("加载默认配置[" + DEFAULT_CONFIG + "]失败", e);
        }
    }


}
