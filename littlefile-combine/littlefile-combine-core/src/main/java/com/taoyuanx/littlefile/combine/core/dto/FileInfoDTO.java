package com.taoyuanx.littlefile.combine.core.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author dushitaoyuan
 * @desc 文件附加信息
 * @date 2020/2/27
 */
@Getter
@Setter
@ToString
public class FileInfoDTO implements Serializable {
    private Long byteSize;
    private String fileName;
}
