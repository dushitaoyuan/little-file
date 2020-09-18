package com.taoyuanx.littlefile.alioss;

/**
 * @author dushitaoyuan
 * @description oss文件存储类别区分
 * @date 2019/6/28
 */
public enum OssFileStorageTypeEnum {
    SYSTEM_FILE(1, "o_s", "系统文件存储"),
    PUBLIC_FILE(2, "o_p", "公开文件存储"),
    //需指定过期时间
    TEMP_FILE(3, "o_t", "临时文件存储");
    private int type;
    private String prefix;
    private String desc;

    OssFileStorageTypeEnum(int type, String prefix, String desc) {
        this.type = type;
        this.desc = desc;
        this.prefix = prefix;
    }

    public int getType() {
        return type;
    }

    public String getStorePrefix() {
        return prefix;
    }
}
