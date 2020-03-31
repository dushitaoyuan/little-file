package com.taoyuanx.littlefile.combine;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author dushitaoyuan
 * @date 2019/9/2622:46
 * @desc: 启动类
 */
@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = "com.taoyuanx.littlefile.combine.dao")
public class LittleFileServiceBootApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(LittleFileServiceBootApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(LittleFileServiceBootApplication.class);
    }


}
