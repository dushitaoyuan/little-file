package com.taoyuanx.littlefile.combine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoyuanx.littlefile.combine.dao.FileDao;
import com.taoyuanx.littlefile.combine.entity.FileEntity;

import com.taoyuanx.littlefile.combine.service.FileService;
import org.springframework.stereotype.Service;

/**
 * @author dushitaoyuan
 * @date 2020/2/17
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileDao, FileEntity> implements FileService {

    @Override
    public FileEntity getById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public FileEntity getByPath(String fileId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<FileEntity>().eq(FileEntity::getPath, fileId));
    }

    @Override
    public void deleteById(Long id) {
        baseMapper.deleteById(id);
    }

}
