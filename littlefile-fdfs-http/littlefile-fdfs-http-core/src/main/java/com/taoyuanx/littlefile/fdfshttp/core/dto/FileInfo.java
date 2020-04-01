package com.taoyuanx.littlefile.fdfshttp.core.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 文件信息
 */
@Getter
@Setter
@ToString
public class FileInfo implements Serializable {
    /**
     * 文件大小
     */
    private Long file_size;
    /**
     * 创建时间戳
     */
    private Long create_timestamp;
    /**
     * crc32 校验码
     */
    private Long crc32;

}
