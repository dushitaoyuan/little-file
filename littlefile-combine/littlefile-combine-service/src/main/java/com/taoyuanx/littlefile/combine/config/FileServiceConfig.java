package com.taoyuanx.littlefile.combine.config;


import com.taoyuanx.littlefile.combine.core.FileStoreTypeEnum;
import com.taoyuanx.littlefile.combine.core.sign.SimpleFileManager;
import com.taoyuanx.littlefile.combine.core.store.AbstractFileStoreService;
import com.taoyuanx.littlefile.combine.core.store.FileStoreCombineService;
import com.taoyuanx.littlefile.combine.core.store.FileStoreService;
import com.taoyuanx.littlefile.combine.core.store.alioss.AliyunOssFileService;
import com.taoyuanx.littlefile.combine.core.store.fdfs.FdfsFileService;
import com.taoyuanx.littlefile.combine.core.store.ftp.FtpFileService;
import com.taoyuanx.littlefile.combine.core.store.minio.MinioConfig;
import com.taoyuanx.littlefile.combine.core.store.minio.MinioFileService;
import com.taoyuanx.littlefile.combine.core.store.sftp.SftpFileService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;

/**
 * @author dushitaoyuan
 * @date 2019/9/2623:27
 * @desc: 系统配置
 */
@Configuration
public class FileServiceConfig {


    @Bean
    public SimpleFileManager simpleFileManager(FileProperties fileProperties) {
        return new SimpleFileManager(fileProperties.getHmacFilePassword(), fileProperties.getFileEndpoint());
    }

    /**
     * -----------------------------------文件服务---------------------------------
     */

    @Bean(name = "sftpFileService")
    public AbstractFileStoreService sftpFileService(FileProperties fileProperties) {
        FileProperties.SftpConfig sftpConfig = fileProperties.getSftp();
        AbstractFileStoreService sftpStoreService = new SftpFileService(sftpConfig.getHost(), sftpConfig.getPort(), sftpConfig.getUsername(),
                sftpConfig.getPassword(), sftpConfig.getWorkDir(), null, null);
        return sftpStoreService;
    }

    @Bean(name = "ftpFileService")
    public AbstractFileStoreService ftpFileService(FileProperties fileProperties) {
        FileProperties.FtpConfig ftpConfig = fileProperties.getFtp();
        AbstractFileStoreService ftpStoreService = new FtpFileService(ftpConfig.getHost(), ftpConfig.getPort(), ftpConfig.getUsername(),
                ftpConfig.getPassword(), ftpConfig.getWorkDir());
        return ftpStoreService;
    }

    @Bean(name = "fdfsFileService")
    public AbstractFileStoreService fdfsFileService(FileProperties fileProperties) {
        FileProperties.FdfsConfig fdfsConfig = fileProperties.getFdfs();
        AbstractFileStoreService fdfsFileService = new FdfsFileService(fdfsConfig.getConfigPath());
        return fdfsFileService;
    }

    @Bean(name = "aliyunOssFileService")
    public AbstractFileStoreService aliyunOssFileService(FileProperties fileProperties) {
        FileProperties.AliyunOssConfig aliyunOssConfig = fileProperties.getAliyunOssConfig();
        AbstractFileStoreService aliyunOssFileService = new AliyunOssFileService(aliyunOssConfig.getEndpoint(),
                aliyunOssConfig.getAccessKeyId(), aliyunOssConfig.getAccessKeySecret(), aliyunOssConfig.getBucketName());
        return aliyunOssFileService;
    }

    @Bean(name = "minioFileService")
    public AbstractFileStoreService minioFileService(FileProperties fileProperties) {
        MinioConfig minioConfig = fileProperties.getMinioConfig();
        AbstractFileStoreService minioFileService = new MinioFileService(minioConfig);
        return minioFileService;
    }


    @Bean(name = "fileStoreCombineService")
    @Primary
    public FileStoreCombineService fileStoreCombineService(ApplicationContext applicationContext) {
        FileStoreService sftpUploadService = (FileStoreService) applicationContext.getBean("sftpFileService");
        FileStoreCombineService fileStoreCombineService = new FileStoreCombineService(sftpUploadService);
        Map<String, AbstractFileStoreService> storeServiceMap = applicationContext.getBeansOfType(AbstractFileStoreService.class);
        storeServiceMap.keySet().stream().forEach(key -> {
            FileStoreService fileStoreService = storeServiceMap.get(key);
            fileStoreCombineService.registStoreService(FileStoreTypeEnum.protocol(fileStoreService.getStoreProtocol()), fileStoreService);
        });
        return fileStoreCombineService;
    }
}
