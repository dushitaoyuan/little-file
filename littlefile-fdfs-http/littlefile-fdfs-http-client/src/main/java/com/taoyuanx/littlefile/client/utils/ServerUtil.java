package com.taoyuanx.littlefile.client.utils;

import com.taoyuanx.littlefile.client.core.ClientConfig;
import com.taoyuanx.littlefile.client.core.FdfsFileClientConstant;
import com.taoyuanx.littlefile.client.core.FdfsApi;
import com.taoyuanx.littlefile.client.core.FileServer;
import okhttp3.Request;

/**
 * @author dushitaoyuan
 * @date 2020/4/515:22
 */
public class ServerUtil {
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
}
