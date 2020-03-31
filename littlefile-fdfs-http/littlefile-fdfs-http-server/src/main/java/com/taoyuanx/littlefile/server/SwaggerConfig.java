package com.taoyuanx.littlefile.server;

import com.google.common.base.Predicate;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.regex.Pattern;


@Configuration
@EnableSwagger2 // 启用 Swagger
//url->http://localhost:8081/swagger-ui.html
@ConditionalOnProperty(name="swagger.enable",havingValue="true")
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        Predicate<RequestHandler> predicate = new Predicate<RequestHandler>() {
            @Override
            public boolean apply(RequestHandler input) {
                Class<?> declaringClass = input.declaringClass();
                boolean isController = declaringClass.isAnnotationPresent(Controller.class);
                boolean isRestController = declaringClass.isAnnotationPresent(RestController.class);
                String className = declaringClass.getName();
             
                String pattern = "com\\.taoyuanx\\.littlefile\\.server\\.controller..*Controller";
                boolean has =  Pattern.matches(pattern, className);
                if(has){
                    if(isController){
                        if(input.isAnnotatedWith(ResponseBody.class)){
                            return true;
                        }
                    }
                    if(isRestController){
                        return true;
                    }
                    return false;
                }

                return false;
            }
        };
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false)
                .select()
                .apis(predicate)
                .build();
    }



    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("文件服务")
                .version("1.0")
                .build();
    }
}