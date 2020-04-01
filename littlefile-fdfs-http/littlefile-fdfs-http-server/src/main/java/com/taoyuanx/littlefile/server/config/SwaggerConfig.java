package com.taoyuanx.littlefile.server.config;

import com.google.common.base.Predicate;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.regex.Pattern;

/**
 * 访问url: //url->http://localhost:8081/swagger-ui.html
 */
@Configuration
@EnableSwagger2 // 启用 Swagger
@ConditionalOnProperty(name = "swagger.enable", havingValue = "true")
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false)
                .groupName("fastdfs-http")
                .forCodeGeneration(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.taoyuanx.littlefile.server.controller"))
                .build();
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("文件服务")
                .version("1.0")
                .description("fastdfs 文件存储 http包装服务")
                .build();
    }
}