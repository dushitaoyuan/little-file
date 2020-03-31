package com.taoyuanx.littlefile.combine.dto;

/**
 * 结果构造
 */
public class ResultBuilder {

    public static final int SUCCESS = 1, FAILED = 0;



    public static Result success(Object data) {
        return Result.build().buildData(data).buildSuccess(SUCCESS);
    }

    public static Result successResult(String data) {
        return Result.build().buildData(data).buildSuccess(SUCCESS);
    }

    public static Result success() {
        return Result.build().buildSuccess(SUCCESS);
    }

    public static Result success(String msg) {
        return Result.build().buildSuccess(SUCCESS).buildMsg(msg);
    }


    public static Result failed(String msg) {
        return Result.build().buildSuccess(FAILED).buildMsg(msg);
    }


    public static Result failed(Integer code, String msg) {
        return Result.build().buildSuccess(FAILED)
                .buildCode(code).buildMsg(msg);
    }
}
