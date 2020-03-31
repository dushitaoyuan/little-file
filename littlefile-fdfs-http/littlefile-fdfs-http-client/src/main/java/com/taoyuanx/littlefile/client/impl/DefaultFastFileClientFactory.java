package com.taoyuanx.littlefile.client.impl;


import com.taoyuanx.littlefile.client.ClientConfig;
import com.taoyuanx.littlefile.client.FastFileClientFactory;
import com.taoyuanx.littlefile.client.FileClient;
import com.taoyuanx.littlefile.client.impl.security.TokenInterceptor;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 都市桃源 工厂实现
 */
public class DefaultFastFileClientFactory implements FastFileClientFactory {
    private ClientConfig config;

    public static FileClient fileClient;

    @Override
    public FileClient getFileClient() {
        if (null == fileClient) {
            throw new RuntimeException("DefaultFastFileClientFactory 未进行初始化");
        }
        return fileClient;
    }

    /**
     * 初始化okhttp client,支持https
     */
    public void init() {
        try {
            //1 初始化http client
            SSLParams sslParams = new SSLParams();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager trustManager = new UnSafeTrustManager();
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            sslParams.sSLSocketFactory = sslContext.getSocketFactory();
            sslParams.trustManager = trustManager;
            Interceptor interceptor = new TokenInterceptor(config.getToken());
            OkHttpClient client = new OkHttpClient().newBuilder().hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    //强行返回true 即验证成功
                    return true;
                }
            }).sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                    .connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS)//连接超时时间
                    .connectionPool(new ConnectionPool(config.getConnectTimeout(), config.getKeepAliveDuration(), TimeUnit.SECONDS))//连接数量,保持连接时间
                    .retryOnConnectionFailure(false)//重试策略
                    .addInterceptor(interceptor)
                    //.addInterceptor(new LoggerInterceptor("ou",true))
                    .build();

            //2. 初始化 文件客户端
            Map<String, String> fileClientImplBaseUrls = new HashMap<String, String>();
            String fileClientBaseUrl = config.getServerUrl() + "file/";
            //文件上传
            fileClientImplBaseUrls.put("upload", fileClientBaseUrl + "upload");
            //从文件上传
            fileClientImplBaseUrls.put("uploadSlave", fileClientBaseUrl + "uploadSlave");
            //文件删除
            fileClientImplBaseUrls.put("delete", fileClientBaseUrl + "removeFile");
            //文件下载
            fileClientImplBaseUrls.put("download", fileClientBaseUrl + "download");
            //图片上传(可自定义是否生成预览图)
            fileClientImplBaseUrls.put("uploadImage", fileClientBaseUrl + "image/upload");
            //获取文件信息
            fileClientImplBaseUrls.put("getFileInfo", fileClientBaseUrl + "getFileInfo");


            FileClientImpl.client = client;
            FileClientImpl.baseUrls = fileClientImplBaseUrls;


            if (null == fileClient) {
                fileClient = new FileClientImpl();
            }
        } catch (Exception e) {
            System.err.println(e);
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

    public ClientConfig getConfig() {
        if (config == null) config = new ClientConfig();
        return config;
    }

    public void setConfig(ClientConfig config) {
        this.config = config;
    }


}
