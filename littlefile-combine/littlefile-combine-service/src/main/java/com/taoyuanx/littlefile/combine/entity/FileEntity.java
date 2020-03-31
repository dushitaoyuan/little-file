package com.taoyuanx.littlefile.combine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author dushitaoyuan
 * @desc file
 * @date 2020/2/17
 */
@Data
@TableName("file")
public class FileEntity implements Serializable {
    @TableId(type=IdType.ID_WORKER)
    private Long id;
    /**
     * 文件路径
     */
    private String path;
    /**
     * 存储类型
     */
    private Integer storeType;
    /**
     * 文件附加信息
     */
    private String fileInfo;
    /**
     * 文件类型
     */
    private Integer fileType;
    /**
     * 创建时间
     */
    private Date createTime;
}
