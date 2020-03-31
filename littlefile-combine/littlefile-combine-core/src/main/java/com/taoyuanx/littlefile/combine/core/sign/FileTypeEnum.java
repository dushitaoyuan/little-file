package com.taoyuanx.littlefile.combine.core.sign;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件类型
 */
public enum FileTypeEnum {
    //无需过期时间
    PUBLIC(1, "公共文件"),
    //访问签名,带过期时间
    SYETEM(2, "系统文件");
    public int code;
    public String desc;
    private static final Map<Integer, FileTypeEnum> enumHolder = new HashMap<>();

    static {
        FileTypeEnum[] typeArray = FileTypeEnum.values();
        Arrays.stream(typeArray).forEach(typeEnum -> {
            enumHolder.put(typeEnum.code, typeEnum);
        });
    }

    FileTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FileTypeEnum type(Integer type) {
        return enumHolder.get(type);

    }
}