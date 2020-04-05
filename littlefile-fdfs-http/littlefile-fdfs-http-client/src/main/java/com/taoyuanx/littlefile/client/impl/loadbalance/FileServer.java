package com.taoyuanx.littlefile.client.impl.loadbalance;

import com.taoyuanx.littlefile.client.utils.OkHttpUtil;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author dushitaoyuan
 * @date 2020/4/515:01
 * @desc: 文件服务对象
 */
public class FileServer {
    private String serverUrl;
    private boolean alive = true;

    public boolean isAlive() {
        return alive;
    }

    public FileServer(String serverUrl) {
        this.serverUrl = serverUrl;
    }


    public String getServerUrl() {
        return serverUrl;
    }

    public void alive(boolean alive) {
        if (this.alive != alive) {
            synchronized (this) {
                this.alive = alive;
            }
        }
    }


    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileServer that = (FileServer) o;
        return Objects.equals(serverUrl, that.serverUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverUrl);
    }
}
