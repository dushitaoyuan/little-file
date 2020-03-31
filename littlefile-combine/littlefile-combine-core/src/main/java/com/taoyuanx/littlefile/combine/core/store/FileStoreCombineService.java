package com.taoyuanx.littlefile.combine.core.store;

import com.taoyuanx.littlefile.combine.core.FileStoreTypeEnum;
import com.taoyuanx.littlefile.combine.core.utils.Utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dushitaoyuan
 * @desc 文件聚合服务
 * @date 2020/2/20
 */
public class FileStoreCombineService implements FileStoreService {

    private Map<FileStoreTypeEnum, FileStoreService> serviceMap = new ConcurrentHashMap<>();
    private FileStoreService uploadService;

    public FileStoreCombineService(FileStoreService uploadService) {
        this.uploadService = uploadService;
    }

    public void registStoreService(FileStoreTypeEnum fileStoreTypeEnum, FileStoreService storeService) {
        serviceMap.put(fileStoreTypeEnum, storeService);
    }

    public void removeStoreService(FileStoreTypeEnum fileStoreTypeEnum) {
        serviceMap.remove(fileStoreTypeEnum);
    }

    @Override
    public String store(InputStream inputStream, String fileName) throws Exception {
        return uploadService.store(inputStream, fileName);
    }

    @Override
    public void delete(String fileId) throws Exception {
        chose(fileId).delete(fileId);
    }

    @Override
    public void downLoad(String fileId, OutputStream outputStream) throws Exception {
        chose(fileId).downLoad(fileId, outputStream);
    }

    private FileStoreService chose(String fileId) {
        String storeProtocol = Utils.getStoreProtocol(fileId);
        FileStoreTypeEnum protocol = FileStoreTypeEnum.protocol(storeProtocol);
        if (protocol == null) {
            throw new RuntimeException(storeProtocol + "存储协议不支持");
        }
        return serviceMap.get(protocol);
    }

    @Override
    public String getStoreProtocol() {
        return uploadService.getStoreProtocol();
    }
}
