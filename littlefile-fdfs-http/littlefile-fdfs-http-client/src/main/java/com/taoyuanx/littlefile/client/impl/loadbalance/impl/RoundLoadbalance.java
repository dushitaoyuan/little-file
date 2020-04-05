package com.taoyuanx.littlefile.client.impl.loadbalance.impl;

import com.taoyuanx.littlefile.client.impl.loadbalance.FileServer;
import com.taoyuanx.littlefile.client.impl.loadbalance.ILoadbalance;

import java.util.List;
import java.util.concurrent.atomic.LongAdder;

public class RoundLoadbalance implements ILoadbalance {

    private LongAdder position = new LongAdder();

    @Override
    public FileServer chose(List<FileServer> serverList) {
        if (serverList == null || serverList.isEmpty()) {
            return null;
        }
        FileServer choseServer = null;
        int len = serverList.size();
        if (len == 1) {
            choseServer = serverList.get(0);
        } else {
            choseServer = serverList.get(round(len));
        }
        return choseServer;
    }

    private int round(Integer size) {
        Integer index = position.intValue();
        if (index < size) {
            return index;
        } else {
            position.reset();
            position.increment();
            return 0;
        }
    }


}
