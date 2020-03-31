package com.taoyuanx.littlefile.combine.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件存储类型
 */
public enum FileStoreTypeEnum {
    FTP(1, "ftp://", "ftp文件存储"),
    SFTP(2, "sftp://", "ftp文件存储"),
    FDFS(3, "fdfs://", "fastdfs文件存储"),
    AliYunOSS(4, "alioss://", "阿里云oss存储");
    public int code;
    public String desc;
    public String protocol;
    private static final Map<Object, FileStoreTypeEnum> enumHolder =new HashMap<>();

    static {
        FileStoreTypeEnum[] typeArray = FileStoreTypeEnum.values();
        Arrays.stream(typeArray).forEach(typeEnum -> {
            enumHolder.put(typeEnum.code, typeEnum);
            enumHolder.put(typeEnum.protocol, typeEnum);
        });
    }

    FileStoreTypeEnum(int code, String protocol, String desc) {
        this.code = code;
        this.desc = desc;
        this.protocol = protocol;
    }

    public static FileStoreTypeEnum type(Integer code) {
        return enumHolder.get(code);

    }
    public static FileStoreTypeEnum protocol(String protocol) {
        return enumHolder.get(protocol);

    }


}