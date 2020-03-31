package com.taoyuanx.littlefile.client.impl.security;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class TokenInterceptor implements Interceptor {
    static Logger logger = LoggerFactory.getLogger(TokenInterceptor.class);
    private String tokenId;
    private String tokenGetUrl;

    private String token;

    public TokenInterceptor(String token) {
        this.token = token;
    }



    @Override
    public Response intercept(Chain chain) throws IOException {
        Request userRequest = chain.request();
        if ("needToken".equals(userRequest.tag())) {
            Request.Builder requestBuilder = userRequest.newBuilder();
            //为所有标记了Token的请求,添加token,header
            requestBuilder.addHeader("token", token);
            userRequest = requestBuilder.build();
        }
        return chain.proceed(userRequest);

    }



}
