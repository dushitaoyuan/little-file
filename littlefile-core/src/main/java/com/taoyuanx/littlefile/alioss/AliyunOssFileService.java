package com.taoyuanx.littlefile.alioss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;

import javax.annotation.PreDestroy;
import java.io.*;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author dushitaoyuan
 * @description aliyun oss 存储
 * @date 2019/6/27
 */
public class AliyunOssFileService {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private OSS ossClient;

    public AliyunOssFileService(String endpoint, String accessKeyId, String accessKeySecret, String bucketName) {
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.bucketName = bucketName;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        } catch (Exception e) {
            throw new RuntimeException("ossclient 创建失败", e);
        }
    }

    /**
     * @param type    文件存储类型
     * @param fileKey 服务端文件组织路径
     * @param file    文件路径
     * @return 服务端文件路径
     */
    public void upload(OssFileStorageTypeEnum type, String fileKey, String file) {
        ObjectMetadata objectMetadata = buildObjectMetadataForType(type);
        ossClient.putObject(bucketName, fileKey, new File(file), objectMetadata);
    }


    /**
     * @param type       文件存储类型
     * @param fileKey    服务端文件组织路径
     * @param fileStream 文件流
     * @return 服务端文件路径
     */
    public void upload(OssFileStorageTypeEnum type, String fileKey, InputStream fileStream) {
        ObjectMetadata objectMetadata = buildObjectMetadataForType(type);
        ossClient.putObject(bucketName, fileKey, fileStream, objectMetadata);
    }

    /**
     * @param type      文件存储类型
     * @param fileKey   服务端文件组织路径
     * @param fileBytes 文件字节数组
     * @return 服务端文件路径
     */
    public void upload(OssFileStorageTypeEnum type, String fileKey, byte[] fileBytes) {
        ObjectMetadata objectMetadata = buildObjectMetadataForType(type);
        ossClient.putObject(bucketName, fileKey, new ByteArrayInputStream(fileBytes), objectMetadata);
    }


    /**
     * 上传临时文件,过期删除
     *
     * @param fileName   文件名
     * @param fileStream 文件流
     * @return 服务端文件路径
     */
    public String uploadTempFile(String fileName, InputStream fileStream, Long expire, TimeUnit timeUnit) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        Date expireDate = new Date(System.currentTimeMillis() + timeUnit.toMillis(expire));
        objectMetadata.setExpirationTime(expireDate);
        String fileKey = OssFileStorageTypeEnum.TEMP_FILE.getStorePrefix() + "/" + UUID.randomUUID() + fileName;
        ossClient.putObject(bucketName, fileKey, fileStream, objectMetadata);
        return fileKey;
    }

    /**
     * 上传系统文件防止重复,自动生成文件路径
     *
     * @param fileName   文件名
     * @param fileStream 文件流
     * @return 服务端文件路径
     */
    public String uploadSystemFile(String fileName, InputStream fileStream) {
        ObjectMetadata objectMetadata = buildObjectMetadataForType(OssFileStorageTypeEnum.SYSTEM_FILE);
        String fileKey = OssFileStorageTypeEnum.SYSTEM_FILE.getStorePrefix() + "/" + getYearMonth() + "/" + UUID.randomUUID() + fileName;
        ossClient.putObject(bucketName, fileKey, fileStream, objectMetadata);
        return fileKey;
    }


    /**
     * 文件下载
     *
     * @param fileKey
     * @param dest
     * @throws Throwable
     */
    public void download(String fileKey, String dest) {
        ossClient.getObject(new GetObjectRequest(bucketName, fileKey), new File(dest));
    }

    /**
     * 文件下载
     *
     * @param fileKey
     * @throws Throwable
     */
    public void download(String fileKey, OutputStream outputStream, int buffSize) throws IOException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, fileKey);
        OSSObject object = ossClient.getObject(getObjectRequest);
        InputStream objectContent = object.getObjectContent();
        byte[] buffer = new byte[buffSize];
        int len = 0;
        while ((len = objectContent.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
            outputStream.flush();
        }
        objectContent.close();

    }

    /**
     * 范围下载
     *
     * @param fileKey
     * @param satrt   开始点
     * @param end     结束点 闭区间
     * @return
     * @throws IOException
     */
    public byte[] downloadRange(String fileKey, int satrt, int end) throws IOException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, fileKey);
        getObjectRequest.withRange(satrt, end);
        getObjectRequest.addHeader("Accept-Encoding", "gzip");
        OSSObject object = ossClient.getObject(getObjectRequest);
        InputStream objectContent = object.getObjectContent();
        int buffSize = 2 << 20;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(end - satrt + 1);
        byte[] buffer = new byte[buffSize];
        int len = 0;
        while ((len = objectContent.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }
        return outputStream.toByteArray();
    }


    /**
     * 文件属性获取
     *
     * @param fileKey
     * @return 文件属性
     */
    public ObjectMetadata getMeta(String fileKey) {
        ObjectMetadata objectMetadata = ossClient.getObjectMetadata(bucketName, fileKey);
        return objectMetadata;
    }

    /**
     * 文件属性获取
     *
     * @param fileKey
     * @return 文件属性
     */
    public Long getFileSize(String fileKey) {
        ObjectMetadata objectMetadata = ossClient.getObjectMetadata(bucketName, fileKey);
        long contentLength = objectMetadata.getContentLength();
        return contentLength;
    }

    /**
     * 文件删除
     *
     * @param fileKey
     */
    public void delete(String fileKey) {
        ossClient.deleteObject(bucketName, fileKey);
    }


    /**
     * 构造文件访问链接
     *
     * @param fileKey
     */
    public String accessUrl(String fileKey, long expire, TimeUnit timeUnit) {
        Date expireDate = new Date(System.currentTimeMillis() + timeUnit.toMillis(expire));
        URL url = ossClient.generatePresignedUrl(bucketName, fileKey, expireDate);
        return url.toString();
    }


    /**
     * 按年月组织文件
     *
     * @param type    文件类型
     * @param fileKey 文件组织路径
     * @return 构造文件key
     */
    public String buildfileKey(OssFileStorageTypeEnum type, String fileKey) {
        String prefix = type.getStorePrefix();
        return prefix + "/" + getYearMonth() + "/" + fileKey;
    }


    private ObjectMetadata buildObjectMetadataForType(OssFileStorageTypeEnum type) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        switch (type) {
            case SYSTEM_FILE: {
                //非公开访问
                objectMetadata.setObjectAcl(CannedAccessControlList.Private);
            }
            break;
            case PUBLIC_FILE: {
                //公开访问
                objectMetadata.setObjectAcl(CannedAccessControlList.PublicRead);
            }
            break;
            default:
                throw new RuntimeException("文件存储类型不支持");
        }
        return objectMetadata;
    }

    @PreDestroy
    public void destory() {
        if (ossClient != null) {
            ossClient.shutdown();
            ossClient = null;
        }
    }

    private String getYearMonth() {
        Calendar instance = Calendar.getInstance();
        int year = instance.get(Calendar.YEAR);
        int month = instance.get(Calendar.MONTH) + 1;
        return year + "-" + month;
    }


}
