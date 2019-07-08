package com.taoyuanx.littlefile.sample;

import lombok.AllArgsConstructor;
import lombok.Data;
import okhttp3.Response;

/**
 * @author dushitaoyuan
 * @date 2019/7/823:30
 * @desc: 范围下载结果
 */
@Data
@AllArgsConstructor
public class RangeResponse {

    private Long start;
    private Response response;
}
