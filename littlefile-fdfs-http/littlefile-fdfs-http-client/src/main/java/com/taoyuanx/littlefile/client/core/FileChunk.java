package com.taoyuanx.littlefile.client.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.InputStream;

/**
 * @author dushitaoyuan
 * @date 2020/4/122:36
 */
@Getter
@ToString
public class FileChunk {
    private Long strat;
    private Long end;
    @Setter
    private InputStream chunkStream;
    public FileChunk(Long strat, Long end) {
        this.strat = strat;
        this.end = end;
    }




}
