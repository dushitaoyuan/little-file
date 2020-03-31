package com.taoyuanx.littlefile.server.config;

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
@ConfigurationProperties(prefix = "littlefile.fdfs.server")
@Data
public class FileProperties {

    private Long datacenterId;
    private Long machineId;

    private String allowType;
    private String fileCacheDir;


}
