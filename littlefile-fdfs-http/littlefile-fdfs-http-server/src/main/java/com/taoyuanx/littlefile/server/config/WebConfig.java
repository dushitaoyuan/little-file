package com.taoyuanx.littlefile.server.config;

import com.taoyuanx.littlefile.server.idgen.snowflake.SnowflakeIdWorker;
import com.taoyuanx.littlefile.server.service.FileValidateService;
import com.taoyuanx.littlefile.server.service.impl.FileValidateServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.taoyuanx.littlefile.server.security.TokenInterceptor;

import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Autowired
    TokenInterceptor tokenInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/file/**");
    }

    @Bean
    public SnowflakeIdWorker snowflakeIdWorker(FileProperties fileProperties) {
        return new SnowflakeIdWorker(fileProperties.getDatacenterId(), fileProperties.getMachineId());
    }

    @Bean
    public FileValidateService fileValidateService(FileProperties fileProperties) {
        return new FileValidateServiceImpl(Arrays.stream(fileProperties.getAllowType().split(",")).collect(Collectors.toSet()));
    }
}