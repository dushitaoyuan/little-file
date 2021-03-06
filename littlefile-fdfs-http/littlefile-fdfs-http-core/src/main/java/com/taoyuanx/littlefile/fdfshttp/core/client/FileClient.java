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
     * 断点上传
     */
    String uploadAppendFile(InputStream fileInput, String fileName);

    /**
     * 追加
     */
    void appendFile(InputStream fileInput, String fileName, String fileId);

    /**
     * 覆盖追加
     */
    void coverAppendFile(InputStream fileInput, String fileName, Long start, String fileId);


    /**
     * 上传本地图片
     */
    MasterAndSlave uploadImage(String localFile, String cutSize);
    MasterAndSlave uploadImage(byte[] fileBytes, String fileName, String cutSize);

    MasterAndSlave uploadImage(InputStream fileInput, String fileName, String cutSize);


    /**
     * 从文件上传
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
     */
    void downLoad(String fileId, String destFile);

    void downLoad(String fileId, OutputStream output);
    byte[] downLoad(String fileId);

    /**
     * 断点下载
     */
    void downLoadRange(String fileId, Long start, Long end, OutputStream output);

    byte[] downLoadRange(String fileId, Long start, Long end);

    /**
     * 获取文件信息
     *
     * @param fileId 文件id
     */
    FileInfo getFileInfo(String fileId);

    Object getClientConfig();

}
