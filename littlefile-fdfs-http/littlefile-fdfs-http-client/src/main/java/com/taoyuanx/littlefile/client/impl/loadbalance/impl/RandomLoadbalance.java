package com.taoyuanx.littlefile.client.impl.loadbalance.impl;

import com.taoyuanx.littlefile.client.impl.loadbalance.FileServer;
import com.taoyuanx.littlefile.client.impl.loadbalance.ILoadbalance;
import com.taoyuanx.littlefile.client.utils.ServerUtil;

import java.util.List;
import java.util.Random;

public class RandomLoadbalance implements ILoadbalance {
    Random random = new Random();

    @Override
    public FileServer chose(List<FileServer> serverList) {
        if (serverList == null || serverList.isEmpty()) {
            return null;
        }
        FileServer choseServer = null;
        if (serverList.size() == 1) {
            choseServer = serverList.get(0);
        } else {
            choseServer = serverList.get(random.nextInt(serverList.size()));
        }
        return choseServer;
    }
}