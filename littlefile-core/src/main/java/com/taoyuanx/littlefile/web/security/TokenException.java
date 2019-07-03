package com.taoyuanx.littlefile.web.security;

/**
 * @author dushitaoyuan
 * @desc token 验证异常
 * @date 2019/7/3
 */
public class TokenException extends  RuntimeException {
    public TokenException(String message) {
        super(message);
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
