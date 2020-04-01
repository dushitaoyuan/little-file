package com.taoyuanx.littlefile.client.impl.security;

import java.io.IOException;
import java.util.Objects;

import com.taoyuanx.littlefile.client.core.FdfsFileClientConstant;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class TokenInterceptor implements Interceptor {

    private String token;

    public TokenInterceptor(String token) {
        this.token = token;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        /**
         *   添加token,header
         */
        Request userRequest = chain.request();
        Object requestTag = userRequest.tag();
        if (Objects.nonNull(requestTag) && FdfsFileClientConstant.REQUEST_TOKEN_TAG.equals(requestTag)) {
            Request.Builder requestBuilder = userRequest.newBuilder();
            requestBuilder.addHeader(FdfsFileClientConstant.REQUEST_TOKEN_KEY, token);

            userRequest = requestBuilder.build();
        }
        return chain.proceed(userRequest);

    }


}
