package com.taoyuanx.littlefile.combine.core.store.fdfs;

import com.taoyuanx.littlefile.combine.core.FileStoreTypeEnum;
import com.taoyuanx.littlefile.combine.core.store.AbstractFileStoreService;
import com.taoyuanx.littlefile.combine.core.store.FileStoreService;
import com.taoyuanx.littlefile.combine.core.utils.Utils;
import org.csource.fastdfs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * fdfs 实现
 */
public class FdfsFileService extends AbstractFileStoreService implements FileStoreService {
    private static final Logger LOG = LoggerFactory.getLogger(FdfsFileService.class);

    public FdfsFileService() {
        super(FileStoreTypeEnum.FDFS.protocol);
        try {
            ClientGlobal.initByProperties("fdfs.properties");
            LOG.info("fastdfs配置信息为:{}", ClientGlobal.configInfo());
        } catch (Exception e) {
            LOG.error("默认配置文件fdfs.properties 初始化配置失敗，请检查fdfs.properties 配置");
            throw new RuntimeException(e);
        }
    }

    public FdfsFileService(String configPath) {
        super(FileStoreTypeEnum.FDFS.protocol);
        try {
            if (configPath.startsWith("classpath")) {
                Properties pro = new Properties();
                pro.load(FdfsFileService.class.getClassLoader()
                        .getResourceAsStream(configPath.replaceFirst("classpath:", "")));
                ClientGlobal.initByProperties(pro);
            } else {
                ClientGlobal.init(configPath);
            }
            LOG.info("初始化fastdfs配置信息为：{}", ClientGlobal.configInfo());
        } catch (Exception e) {
            LOG.error("配置文件 {} 初始化配置失敗，请检查配置,异常信息为{}", configPath, e);
            throw new RuntimeException(e);
        }
    }


    static TrackerClient tracker = null;

    public StorageClient1 getClient() throws Exception {
        return new StorageClient1();
    }

    @Override
    public String store(InputStream inputStream, String fileName) throws Exception {
        String fileExtName = Utils.getExtension(fileName);
        String fileId = getClient().upload_file1(null, inputStream.available(), new UploadStream(inputStream, inputStream.available()),
                fileExtName, null);
        return Utils.addFileStoreProtocol(getStoreProtocol(), fileId);
    }

    @Override
    public void delete(String fileId) throws Exception {
        fileId = Utils.removeFileStoreProtocol(fileId);
        StorageClient1 client = getClient();
        client.delete_file1(fileId);
    }

    @Override
    public void downLoad(String fileId, OutputStream outputStream) throws Exception {
        fileId = Utils.removeFileStoreProtocol(fileId);
        try {
            getClient().download_file1(fileId, new DownloadStream(outputStream));
        } finally {
            outputStream.close();
        }
    }


}
