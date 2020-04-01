package com.taoyuanx.littlefile.client.core;

import lombok.Getter;

/**
 * @author dushitaoyuan
 * @date 2020/4/122:36
 */
@Getter
public class ByteRange {
    private Long strat;
    private Long end;

    public ByteRange(Long strat, Long end) {
        this.strat = strat;
        this.end = end;
    }
}
