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
    private Long start;
    private Long end;
    private Long len;
    @Setter
    private InputStream chunkStream;

    public FileChunk(Long start, Long end) {
        this.start = start;
        this.end = end;
        this.len = end - start + 1;
    }


}
