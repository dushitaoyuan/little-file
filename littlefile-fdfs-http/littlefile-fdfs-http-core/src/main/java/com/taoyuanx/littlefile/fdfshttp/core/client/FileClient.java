package com.taoyuanx.littlefile.fdfshttp.core.client;

import com.taoyuanx.littlefile.fdfshttp.core.dto.FileInfo;
import com.taoyuanx.littlefile.fdfshttp.core.dto.MasterAndSlave;

import java.io.InputStream;
import java.io.OutputStream;

public interface FileClient {
    /**
     * 文件上传
     */
    String upload(String localFile);
    String upload(byte[] fileBytes, String fileName);

    String upload(InputStream fileInput, String fileName);



    /**
     * 上传本地图片
     *
     * @param localFile
     * @param cutSize   预览图尺寸 如:20x20,30x30,100x100 不传,不生成预览图,可生成多个预览图
     * @return
     */
    MasterAndSlave uploadImage(String localFile, String cutSize);

    /**
     * 上传本地图片
     *
     * @param cutSize 预览图尺寸  如:20x20,30x30,100x100 不传,不生成预览图,可生成多个预览图
     * @return
     */
    MasterAndSlave uploadImage(byte[] fileBytes, String fileName, String cutSize);

    /**
     * 上传本地图片
     *
     * @param cutSize 预览图尺寸  如:20x20,30x30,100x100 不传,不生成预览图,可生成多个预览图
     * @return
     */
    MasterAndSlave uploadImage(InputStream fileInput, String fileName, String cutSize);

    /**
     * 从文件上传
     *
     */
    String uploadSlave(String localFile, String fileId);
    String uploadSlave(byte[] fileBytes, String fileName, String fileId);
    String uploadSlave(InputStream fileInput, String fileName, String fileId);

    /**
     * 删除文件id
     *
     * @param fileId
     */
    void delete(String fileId);


    /**
     * 文件下载
     *
     * @param fileId
     * @param destFile
     */
    void downLoad(String fileId, String destFile);
    void downLoad(String fileId, OutputStream output);
    byte[] downLoad(String fileId);

    /**
     * 断点下载
     */
    void downLoadRange(String fileId, Long start, Long len, OutputStream output);
    byte[] downLoadRange(String fileId, Long start, Long len);

    /**
     * 获取文件信息
     *
     * @param fileId 文件id
     */
    FileInfo getFileInfo(String fileId);
}
