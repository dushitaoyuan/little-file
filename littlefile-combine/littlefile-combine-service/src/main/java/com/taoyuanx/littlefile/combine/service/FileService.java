package com.taoyuanx.littlefile.combine.service;

import com.taoyuanx.littlefile.combine.entity.FileEntity;

/**
 * @author dushitaoyuan
 * @date 2020/2/17
 */
public interface FileService {
    boolean save(FileEntity fileEntity);

    FileEntity getById(Long id);

    FileEntity getByPath(String fileId);

    void deleteById(Long id);
}
