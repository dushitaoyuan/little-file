package com.taoyuanx.littlefile.server.config;

import com.taoyuanx.littlefile.server.dto.Result;
import com.taoyuanx.littlefile.server.dto.ResultBuilder;
import com.taoyuanx.littlefile.server.ex.ServiceException;
import com.taoyuanx.littlefile.server.security.TokenInterceptor;
import com.taoyuanx.littlefile.server.service.FileValidateService;
import com.taoyuanx.littlefile.server.service.impl.FileValidateServiceImpl;
import com.taoyuanx.littlefile.server.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@ControllerAdvice
@Slf4j
public class WebConfig implements WebMvcConfigurer {


    @Autowired
    TokenInterceptor tokenInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/file/**");
    }


    @Bean
    public FileValidateService fileValidateService(FileProperties fileProperties) {
        return new FileValidateServiceImpl(Arrays.stream(fileProperties.getAllowType().split(",")).collect(Collectors.toSet()));
    }

    @ExceptionHandler(Throwable.class)
    public ModelAndView handleException(Throwable e, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        Integer errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String errorMsg = "系统异常:" + e.getMessage();
        if (e instanceof ServiceException) {
            errorMsg = e.getMessage();
            ServiceException serviceException = ((ServiceException) e);
            if (Objects.nonNull(serviceException.getErrorCode())) {
                errorCode = serviceException.getErrorCode();
            }
        }else {
            log.error("系统异常",e);
        }
        Result errorResult = ResultBuilder.failed(errorCode, errorMsg);
        //返回json
        ResponseUtil.responseJson(response, errorResult, httpStatus.value());
        return modelAndView;
    }
}