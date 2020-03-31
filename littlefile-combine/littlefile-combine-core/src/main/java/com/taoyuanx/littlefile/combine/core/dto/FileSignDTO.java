package com.taoyuanx.littlefile.combine.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author dushitaoyuan
 * @desc 文件url签名实体(单字变量减少签名长度)
 * @date 2020/2/17
 */
@Getter
@Setter
@ToString
public class FileSignDTO implements Serializable {
    /**
     * 文件类型
     */
    @JsonProperty(value = "t")
    private Integer type;
    /**
     * 文件路径
     */
    @JsonProperty(value = "p")

    private String path;
    /**
     * 文件id
     */
    @JsonProperty(value = "id")
    private Long id;
    /**
     * 起止时间
     */
    @JsonProperty(value = "c")

    private Long createTime;
    @JsonProperty(value = "e")
    private Long endTime;

    /**
     * 数据签名
     */
    @JsonIgnore
    private String sign;
    /**
     * 文件数据
     */
    @JsonIgnore
    private byte[] data;
}
