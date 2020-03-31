package com.taoyuanx.littlefile.combine.core.store.alioss;

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

import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author dushitaoyuan
 * @description aliyun oss 存储
 * @date 2019/6/27
 */
public class AliyunOssFileService extends AbstractFileStoreService implements FileStoreService {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private OSS ossClient;


    public AliyunOssFileService(String endpoint, String accessKeyId, String accessKeySecret, String bucketName) {
        super(FileStoreTypeEnum.AliYunOSS.protocol);
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.bucketName = bucketName;
        try {
            ossClient = new OSSClientBuilder().build(this.endpoint, this.accessKeyId, this.accessKeySecret);
        } catch (Exception e) {
            throw new RuntimeException("ossclient 创建失败", e);
        }
    }

    @Override
    public String store(InputStream inputStream, String fileName) {
        ObjectMetadata objectMetadata = buildObjectMetadata();
        String filePath = Utils.buildFilePath(Utils.newFileName(fileName));
        ossClient.putObject(bucketName, filePath, inputStream, objectMetadata);
        return Utils.addFileStoreProtocol(getStoreProtocol(), filePath);
    }


    @Override
    public void delete(String fileId) {
        ossClient.deleteObject(bucketName, Utils.removeFileStoreProtocol(fileId));
    }


    @Override
    public void downLoad(String fileId, OutputStream outputStream) throws Exception {
        int buffSize = 2 << 20;
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, Utils.removeFileStoreProtocol(fileId));
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
     * 构造文件访问链接 oss 特有
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
     * @param fileId 文件组织路径
     * @return 构造文件key
     */
    private String buildfileKey(String fileId) {
        return getYearMonth() + "/" + fileId;
    }


    private ObjectMetadata buildObjectMetadata() {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setObjectAcl(CannedAccessControlList.Private);
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
