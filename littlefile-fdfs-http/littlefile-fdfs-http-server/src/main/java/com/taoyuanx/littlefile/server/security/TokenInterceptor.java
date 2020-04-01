package com.taoyuanx.littlefile.server.security;

import com.taoyuanx.littlefile.server.anno.NeedToken;
import com.taoyuanx.littlefile.server.config.FileProperties;
import com.taoyuanx.littlefile.server.ex.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author 都市桃源
 * token校验
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    FileProperties fileProperties;


    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp,
                             Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            NeedToken tokenAnno = getAnno(handlerMethod);
            if (Objects.nonNull(tokenAnno) && tokenAnno.need()) {
                String token = getToken(req);
                if (Objects.nonNull(fileProperties.getToken()) && !fileProperties.getToken().equals(token)) {
                    throw new ServiceException(401, "token 非法");
                }
            }

        }


        return true;
    }


    public NeedToken getAnno(HandlerMethod handlerMethod) {
        NeedToken methodTokenAnno = handlerMethod.getMethod().getAnnotation(NeedToken.class);
        if (Objects.nonNull(methodTokenAnno)) {
            return methodTokenAnno;
        } else {
            return handlerMethod.getBeanType().getAnnotation(NeedToken.class);
        }
    }

    public String getToken(HttpServletRequest req) {
        String token = req.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            token = req.getParameter("token");
        }
        return token;

    }

}
