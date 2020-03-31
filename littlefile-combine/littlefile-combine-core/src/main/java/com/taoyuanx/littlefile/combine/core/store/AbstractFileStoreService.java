package com.taoyuanx.littlefile.combine.core.store;

/**
 * @author dushitaoyuan
 * @date 2020/2/27
 */
public abstract class AbstractFileStoreService implements FileStoreService {
    protected String protocol;

    public AbstractFileStoreService(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String getStoreProtocol() {
        return protocol;
    }
}
