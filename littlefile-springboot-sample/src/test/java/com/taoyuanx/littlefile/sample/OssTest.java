package com.taoyuanx.littlefile.sample;

import com.taoyuanx.littlefile.alioss.AliyunOssFileService;
import com.taoyuanx.littlefile.alioss.OssFileStorageTypeEnum;

/**
 * @author dushitaoyuan
 * @date 2019/9/2211:06
 */
public class OssTest {

    public static void main(String[] args) throws Exception {
        String AccessKeyID = "";
        String AccessKeySecret = "";
        String bucketName = "";
        String endpoint = "";
        AliyunOssFileService aliyunOssFileService = new AliyunOssFileService(endpoint, AccessKeyID, AccessKeySecret, bucketName);
        String s = aliyunOssFileService.buildfileKey(OssFileStorageTypeEnum.SYSTEM_FILE, "11.png");
        aliyunOssFileService.upload(OssFileStorageTypeEnum.SYSTEM_FILE, s, "g://11.png");
        aliyunOssFileService.destory();
        String fileKey = "o_s/2019-9/11.png";

        System.out.println(s);
    }
}
