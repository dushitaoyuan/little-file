package com.taoyuanx.littlefile.client.core;

import lombok.Getter;
import lombok.ToString;

/**
 * @author dushitaoyuan
 * @date 2020/4/122:36
 */
@Getter
@ToString
public class ByteRange {
    private Long strat;
    private Long end;
    private Long len;

    public ByteRange(Long strat, Long end) {
        this.strat = strat;
        this.end = end;
        this.len=end-strat+1;
    }




}
