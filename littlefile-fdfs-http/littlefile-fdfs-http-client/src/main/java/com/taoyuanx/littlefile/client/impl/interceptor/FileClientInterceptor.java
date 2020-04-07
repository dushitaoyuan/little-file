package com.taoyuanx.littlefile.client.impl.interceptor;

import com.taoyuanx.littlefile.client.core.ClientConfig;
import com.taoyuanx.littlefile.client.core.FdfsFileClientConstant;
import com.taoyuanx.littlefile.client.ex.FdfsException;
import com.taoyuanx.littlefile.client.core.FileServer;
import com.taoyuanx.littlefile.client.impl.loadbalance.ILoadbalance;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FileClientInterceptor implements Interceptor {

    private ClientConfig clientConfig;

    private ILoadbalance loadbalance;

    public FileClientInterceptor(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.loadbalance = clientConfig.getLoadbalance();
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request userRequest = chain.request();
        Object requestTag = userRequest.tag();
        HttpUrl requestUrl = userRequest.url();
        String stringUrl = requestUrl.toString();
        boolean needToken = Objects.nonNull(requestTag) && FdfsFileClientConstant.REQUEST_TOKEN_TAG.equals(requestTag);
        boolean needBase = Objects.nonNull(stringUrl) && stringUrl.startsWith(FdfsFileClientConstant.FILE_CLIENT_BASE_URL);
        FileServer choseServer = null;

        if (needBase || needToken) {
            /**
             *   添加token,header
             */
            Request.Builder requestBuilder = userRequest.newBuilder();
            if (needToken) {
                requestBuilder.addHeader(FdfsFileClientConstant.REQUEST_TOKEN_KEY, clientConfig.getToken());
            }
            /**
             * 替换base url
             */
            if (needBase) {
                choseServer = choseServer();
                requestBuilder.url(buildRealUrl(choseServer, requestUrl));
            }
            userRequest = requestBuilder.build();
            if (needBase) {
                return doProceed(chain, userRequest, choseServer, null);
            }
        }
        return chain.proceed(userRequest);
    }


    private Map<FileServer, HttpUrl> serverUrlCache = new ConcurrentHashMap<>();

    /**
     * 递归执行,直到所有存活的fileServer中有一个执行成功
     */
    private Response doProceed(Chain chain, Request userRequest, FileServer choseServer, List<FileServer> excludeFileServer) throws IOException {
        try {
            return chain.proceed(userRequest);
        } catch (ConnectException e) {
            log.warn("file server[{}] connect error", choseServer.getServerUrl());
            /**
             * 连接失败 标记server 不可用
             */
            if (Objects.nonNull(choseServer)) {
                choseServer.alive(false);
            }
            if (Objects.isNull(excludeFileServer)) {
                excludeFileServer = new ArrayList<>();
            }
            excludeFileServer.add(choseServer);
            FileServer newChoseServer = loopForAlive(excludeFileServer);
            return doProceed(chain, userRequest.newBuilder().url(buildRealUrl(newChoseServer, userRequest.url())).build(), newChoseServer, excludeFileServer);
        }
    }

    private HttpUrl buildRealUrl(FileServer choseServer, HttpUrl oldUrl) {
        HttpUrl choseServerUrl = getFileServerHttpUrl(choseServer);
        choseServerUrl = oldUrl.newBuilder().scheme(choseServerUrl.scheme()).host(choseServerUrl.host()).port(choseServerUrl.port()).build();
        return choseServerUrl;
    }

    private FileServer choseServer() {
        FileServer choseServer = loadbalance.chose(clientConfig.getFdfsServer());
        if (choseServer.isAlive()) {
            return choseServer;
        } else if (clientConfig.getFdfsServer().size() > 1) {
            return loopForAlive(Arrays.asList(choseServer));
        }
        throw new FdfsException("no alive fileServer");
    }


    private HttpUrl getFileServerHttpUrl(FileServer fileServer) {
        if (!serverUrlCache.containsKey(fileServer)) {
            serverUrlCache.put(fileServer, HttpUrl.parse(fileServer.getServerUrl()));
        }
        return serverUrlCache.get(fileServer);
    }

    private FileServer loopForAlive(List<FileServer> excludeFileServerList) {

        Optional<FileServer> anyAliveFileServer = clientConfig.getFdfsServer().stream().filter(fileServer -> {
            for (FileServer excludeServer : excludeFileServerList) {
                if (excludeServer.equals(fileServer)) {
                    return false;
                }
            }
            return fileServer.isAlive();
        }).findAny();
        if (anyAliveFileServer.isPresent()) {
            return anyAliveFileServer.get();
        }
        throw new FdfsException("no alive fileServer");

    }


}
