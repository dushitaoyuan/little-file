package com.taoyuanx.littlefile.client.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.InputStream;

/**
 * @author dushitaoyuan
 * @date 2020/4/122:36
 */
@ToString
@Getter
public class FileChunk {
    private Long start;
    private Long end;
    private Long len;
    private Integer chunkIndex;
    @Setter
    private InputStream chunkStream;

    public FileChunk(Long start, Long end, Integer chunkIndex) {
        this.start = start;
        this.end = end;
        this.len = end - start + 1;
        this.chunkIndex = chunkIndex;
    }
}
