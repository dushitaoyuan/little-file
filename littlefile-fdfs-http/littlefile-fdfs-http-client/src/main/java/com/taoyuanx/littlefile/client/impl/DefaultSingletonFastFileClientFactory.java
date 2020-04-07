package com.taoyuanx.littlefile.client.impl;


import com.taoyuanx.littlefile.client.core.ClientConfig;
import com.taoyuanx.littlefile.client.core.FastFileClientFactory;
import com.taoyuanx.littlefile.client.core.FdfsFileClientConstant;
import com.taoyuanx.littlefile.client.ex.FdfsException;
import com.taoyuanx.littlefile.client.impl.interceptor.FileClientInterceptor;
import com.taoyuanx.littlefile.client.core.FdfsApi;
import com.taoyuanx.littlefile.fdfshttp.core.client.FileClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Slf4j
public class DefaultSingletonFastFileClientFactory implements FastFileClientFactory {
    private ClientConfig config;


    public DefaultSingletonFastFileClientFactory(ClientConfig config) {
        this.config = config;
    }

    public DefaultSingletonFastFileClientFactory() {
        this.config = new ClientConfig(ClientConfig.DEFAULT_CONFIG);
    }

    private static FileClient fileClient;

    @Override
    public FileClient fileClient() {
        if (null == fileClient) {
            synchronized (DefaultSingletonFastFileClientFactory.class) {
                if (null == fileClient) {
                    init();
                }
            }
        }
        return fileClient;
    }


    /**
     * 初始化
     */
    private void init() {
        try {
            //1 初始化http client
            SSLParams sslParams = new SSLParams();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager trustManager = new UnSafeTrustManager();
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            sslParams.sSLSocketFactory = sslContext.getSocketFactory();
            sslParams.trustManager = trustManager;
            Interceptor clientInterceptor = new FileClientInterceptor(config);
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder().hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            }).sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                    .connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(config.getMaxIdleConnections(), config.getKeepAliveDuration(), TimeUnit.SECONDS))
                    .retryOnConnectionFailure(false)
                    .addInterceptor(clientInterceptor)
                    .build();


            List<FdfsApi> fdfsApiList = Arrays.asList(FdfsApi.values());
            Map<FdfsApi, String> apiMap = new HashMap(fdfsApiList.size());
            fdfsApiList.stream().forEach(api -> {
                apiMap.put(api, FdfsFileClientConstant.FILE_CLIENT_BASE_URL + FdfsFileClientConstant.FILE_CLIENT_PATH_BASE + api.path);
            });
            config.setApiMap(apiMap);
            config.setOkHttpClient(okHttpClient);
            fileClient = new FileClientImpl(config);
        } catch (Exception e) {
            log.error("初始化失败", e);
            throw new FdfsException("配置初始化失败", e);
        }
    }


    public static class SSLParams {
        public SSLSocketFactory sSLSocketFactory;
        public X509TrustManager trustManager;
    }

    private static class UnSafeTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

}
