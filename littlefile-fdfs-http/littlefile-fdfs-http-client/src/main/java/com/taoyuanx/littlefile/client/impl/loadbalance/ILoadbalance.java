package com.taoyuanx.littlefile.client.impl.loadbalance;

import java.util.List;

/**
 * @author dushitaoyuan
 * @date 2020/4/512:40
 * 负载接口
 */
public interface ILoadbalance {
    FileServer chose(List<FileServer> serverList);
}
