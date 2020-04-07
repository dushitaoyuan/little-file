package com.taoyuanx.littlefile.client.impl.loadbalance.impl;

import com.taoyuanx.littlefile.client.core.FileServer;
import com.taoyuanx.littlefile.client.impl.loadbalance.ILoadbalance;

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