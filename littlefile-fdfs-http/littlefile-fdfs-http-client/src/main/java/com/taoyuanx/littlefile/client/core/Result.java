package com.taoyuanx.littlefile.client.core;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import okhttp3.Response;

import java.io.Serializable;
import java.util.Objects;

/**
 * 统一返回结果
 * <p>
 * code 错误码
 * msg 错误消息
 * success 请求成功标识 1成功,0失败
 * data 结果体
 * ext 扩展信息
 */
@Getter
@Setter
public class Result implements Serializable {
    private Integer code;
    private String msg;
    private Integer success;
    private String data;
    private String ext;
    /**
     * 流 response
     */
    private Response response;

    public  static  final Integer SUCCESS_CODE=1;

    public boolean success() {
        return Objects.nonNull(success) && success.equals(1);
    }
}
