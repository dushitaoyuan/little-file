package com.taoyuanx.littlefile.server.security;

import com.taoyuanx.littlefile.server.anno.NeedToken;
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

    @Override
    public void afterCompletion(HttpServletRequest req,
                                HttpServletResponse resp, Object handler, Exception e)
            throws Exception {
    }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse resp,
                           Object handler, ModelAndView view) throws Exception {

    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp,
                             Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        NeedToken tokenAnno = getAnno(handlerMethod);
        if (null != tokenAnno) {
            if (tokenAnno.need()) {
                String token = getToken(req);
            }
            resp.sendError(HttpStatus.UNAUTHORIZED.value(), "token非法或缺少header[token]");
            return false;
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
