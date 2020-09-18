package com.taoyuanx.littlefile.minio;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.taoyuanx.littlefile.alioss.OssFileStorageTypeEnum;
import com.taoyuanx.littlefile.util.Utils;
import io.minio.*;
import io.minio.http.Method;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.commons.io.IOUtils;

import javax.annotation.PreDestroy;
import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author dushitaoyuan
 * @description Minio 存储
 * @date 2019/09/18
 */
public class MinioFileService {
    private MinioClient minioClient;
    private MinioConfig minioConfig;

    public MinioFileService(MinioConfig minioConfig) {
        this.minioClient = minioClient(minioConfig);
        this.minioConfig = minioConfig;
    }

    public void upload(String objectName, String file) {
        try {
            UploadObjectArgs.Builder builder =
                    UploadObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).filename(file);
            minioClient.uploadObject(builder.build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void upload(String objectName, InputStream fileStream) {
        try {
            Integer size = fileStream.available();
            PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(fileStream, size.longValue(), -1).build();
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void download(String objectName, String localFile) {
        try {
            download(objectName, new FileOutputStream(localFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void download(String objectName, OutputStream outputStream) {
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).build();
            InputStream object = minioClient.getObject(getObjectArgs);
            IOUtils.copy(object, outputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String accessUrl(String objectName, int expire, TimeUnit timeUnit) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().object(objectName).expiry(expire, timeUnit).method(Method.GET).bucket(minioConfig.getBucketName()).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MinioClient minioClient(MinioConfig minioConfig) {
        try {
            MinioClient.Builder minioClientBuilder = MinioClient.builder()
                    .endpoint(minioConfig.getEndpoint())
                    .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey());
            if (Utils.isNotEmpty(minioConfig.getRegion())) {
                minioClientBuilder.region(minioConfig.getRegion());
            }
            minioClientBuilder.httpClient(getOkhttpClient(minioConfig));
            return minioClientBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException("配置初始化失败", e);
        }

    }


    private OkHttpClient getOkhttpClient(MinioConfig minioConfig) throws Exception {
        OkHttpClient okHttpClient = minioConfig.getOkHttpClient();
        if (okHttpClient != null) {
            return okHttpClient;
        }
        /**
         * 初始化 http client
         */
        SSLParams sslParams = new SSLParams();
        SSLContext sslContext = SSLContext.getInstance("TLS");
        X509TrustManager trustManager = new UnSafeTrustManager();
        sslContext.init(null, new TrustManager[]{trustManager}, null);
        sslParams.sSLSocketFactory = sslContext.getSocketFactory();
        sslParams.trustManager = trustManager;
        okHttpClient = new OkHttpClient().newBuilder().hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }).sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .connectTimeout(minioConfig.getConnectTimeout(), TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(minioConfig.getMaxIdleConnections(), minioConfig.getKeepAliveDuration(), TimeUnit.SECONDS))
                .retryOnConnectionFailure(false)
                .build();
        minioConfig.setOkHttpClient(okHttpClient);
        return okHttpClient;
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
