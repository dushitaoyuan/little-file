package com.taoyuanx.littlefile.combine.config;

import com.taoyuanx.littlefile.combine.core.store.minio.MinioConfig;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author dushitaoyuan
 * @desc 文件服务配置
 * @date 2020/2/17
 */
@Configuration
@ConfigurationProperties(prefix = "littlefile")
@Data
public class FileProperties implements InitializingBean {


    /**
     * 文件签名密码
     */
    private String hmacFilePassword;
    /**
     * 文件服务暴露地址
     */
    private String fileEndpoint;

    private boolean gzip;

    private String token;

    private String allowType;

    private Set<String> typeSet;

    /**
     * sftp配置
     */
    private SftpConfig sftp;
    /**
     * ftp配置
     */
    private FtpConfig ftp;
    /**
     * fastdfs配置
     */
    private FdfsConfig fdfs;
    /**
     * aliyun 配置
     */
    private AliyunOssConfig aliyunOssConfig;
    /**
     * minio配置
     */
    private MinioConfig minioConfig;
    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.hasText(allowType)) {
            typeSet = Arrays.stream(allowType.split(",")).map(type -> {
                return type.toLowerCase();
            }).collect(Collectors.toSet());
        }
    }


    @Data
    public static class SftpConfig {
        private String username;
        private String password;
        private String host;
        private Integer port;
        private String workDir;
    }

    @Data
    public static class FtpConfig {
        private String username;
        private String password;
        private String host;
        private Integer port;
        private String workDir;
    }

    @Data
    public static class FdfsConfig {
        private String configPath;
    }

    @Data
    public static class AliyunOssConfig {
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
    }


}
