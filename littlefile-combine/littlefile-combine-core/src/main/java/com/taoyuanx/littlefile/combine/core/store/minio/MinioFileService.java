package com.taoyuanx.littlefile.combine.core.store.minio;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.taoyuanx.littlefile.combine.core.FileStoreTypeEnum;
import com.taoyuanx.littlefile.combine.core.store.AbstractFileStoreService;
import com.taoyuanx.littlefile.combine.core.store.FileStoreService;
import com.taoyuanx.littlefile.combine.core.utils.Utils;
import io.minio.*;
import io.minio.http.Method;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import javax.annotation.PreDestroy;
import javax.net.ssl.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author dushitaoyuan
 * @description minio 存储
 * @date 2019/09/18
 */
public class MinioFileService extends AbstractFileStoreService implements FileStoreService {
    private MinioClient minioClient;
    private MinioConfig minioConfig;

    public MinioFileService(MinioConfig minioConfig) {
        super(FileStoreTypeEnum.MINIO.protocol);
        this.minioClient = minioClient(minioConfig);
        this.minioConfig = minioConfig;
    }
    @Override
    public String store(InputStream inputStream, String fileName) throws Exception {
        String objectName = Utils.buildFilePath(Utils.newFileName(fileName));
        upload(objectName, inputStream);
        return Utils.addFileStoreProtocol(getStoreProtocol(), objectName);
    }

    @Override
    public void delete(String objectName) {
        try {
            objectName = Utils.removeFileStoreProtocol(objectName);
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void downLoad(String fileId, OutputStream outputStream) throws Exception {
        String objectName = Utils.removeFileStoreProtocol(fileId);
        download(objectName, outputStream);
    }

    public String accessUrl(String objectName, int expire, TimeUnit timeUnit) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().object(objectName).expiry(expire, timeUnit).method(Method.GET).bucket(minioConfig.getBucketName()).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    int BUFF_SIZE = 2 << 20;

    public void download(String objectName, OutputStream outputStream) {
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).build();
            InputStream object = minioClient.getObject(getObjectArgs);
            Utils.copyStream(object, outputStream, BUFF_SIZE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private MinioClient minioClient(MinioConfig minioConfig) {
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
