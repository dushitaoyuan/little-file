package com.taoyuanx.littlefile.combine.config;

import com.taoyuanx.littlefile.combine.dto.Result;
import com.taoyuanx.littlefile.combine.dto.ResultBuilder;
import com.taoyuanx.littlefile.combine.ex.ServiceException;
import com.taoyuanx.littlefile.combine.utils.ResponseUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;


/**
 * @author dushitaoyuan
 * @desc mvc相关配置
 * @date 2019/12/23
 */
@Configuration
public class FileMvcConfig implements WebMvcConfigurer {

    @ExceptionHandler(Throwable.class)
    public ModelAndView handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        Integer errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String errorMsg = "系统异常";
        if (e instanceof ServiceException) {
            errorMsg = e.getMessage();
            ServiceException serviceException = ((ServiceException) e);
            if (Objects.nonNull(serviceException.getErrorCode())) {
                errorCode = serviceException.getErrorCode();
            }
        }

        Result errorResult = ResultBuilder.failed(errorCode, errorMsg);
        //处理json请求
        ResponseUtil.responseJson(response, errorResult, httpStatus.value());
        return modelAndView;
    }
}
