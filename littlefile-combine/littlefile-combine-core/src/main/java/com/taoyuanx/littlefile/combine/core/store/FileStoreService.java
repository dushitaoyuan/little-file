package com.taoyuanx.littlefile.combine.core.store;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author dushitaoyuan
 * @desc 文件存储
 * @date 2020/2/20
 */
public interface FileStoreService {
    /**
     * 文件存储
     *
     * @param inputStream 文件流数据
     * @param fileName    文件名称
     * @return fileId (文件路径)
     */
    String store(InputStream inputStream, String fileName) throws Exception;

    /**
     * 文件删除
     *
     * @param fileId
     */
    void delete(String fileId) throws Exception;

    /**
     * 文件下载
     *
     * @param fileId
     * @return
     */
    void downLoad(String fileId, OutputStream outputStream) throws Exception;

    /**
     * 获取存储协议
     */
    String getStoreProtocol();


}
