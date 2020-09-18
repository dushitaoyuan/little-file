package com.taoyuanx.littlefile.support.impl;

import com.taoyuanx.littlefile.alioss.AliyunOssFileService;
import com.taoyuanx.littlefile.minio.MinioFileService;
import com.taoyuanx.littlefile.support.FileDownStrategy;

public class MinioStrategy implements FileDownStrategy {
    private MinioFileService minioFileService;

    public MinioStrategy(MinioFileService minioFileService) {
        this.minioFileService = minioFileService;
    }

    @Override
    public void down(String src, String dest) throws Exception {
        minioFileService.download(src, dest);
    }

}
