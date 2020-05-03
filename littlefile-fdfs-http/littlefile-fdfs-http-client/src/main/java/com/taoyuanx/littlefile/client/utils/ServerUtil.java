package com.taoyuanx.littlefile.client.utils;

import com.taoyuanx.littlefile.client.core.ClientConfig;
import com.taoyuanx.littlefile.client.core.FdfsFileClientConstant;
import com.taoyuanx.littlefile.client.core.FdfsApi;
import com.taoyuanx.littlefile.client.core.FileServer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;

/**
 * @author dushitaoyuan
 * @date 2020/4/515:22
 */
@Slf4j
public class ServerUtil {
    /**
     * 判断file server是否存活
     */
    public static boolean checkServerAlive(FileServer fileServer, ClientConfig clientConfig) {

        if (fileServer.isAlive()) {
            return true;
        }
        synchronized (fileServer) {
            if (!fileServer.isAlive()) {
                try {
                    OkHttpUtil.request(clientConfig.getOkHttpClient(), new Request.Builder()
                            .url(fileServer.getServerUrl() + FdfsFileClientConstant.FILE_CLIENT_PATH_BASE + FdfsApi.HELLO.path).get().tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), null);
                    fileServer.alive(true);
                    return true;
                } catch (Exception e) {
                    fileServer.alive(false);
                    return false;
                }
            }
        }
        return true;
    }

    public static void heartBeatCheck(ClientConfig clientConfig) {
        if (clientConfig.getFdfsServer() == null) {
            return;
        }
        clientConfig.getFdfsServer().stream().filter(fileServer -> {
            return !fileServer.isAlive();
        }).forEach(fileServer -> {
            try {
                OkHttpUtil.request(clientConfig.getOkHttpClient(), new Request.Builder()
                        .url(fileServer.getServerUrl() + FdfsFileClientConstant.FILE_CLIENT_PATH_BASE + FdfsApi.HELLO.path).get().tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), null);
                fileServer.alive(true);
            } catch (Exception e) {
                log.warn("fileserver ->{}, heart error", fileServer.getServerUrl());
            }
        });

    }
}
