package com.taoyuanx.littlefile.support.impl;

import com.taoyuanx.littlefile.alioss.AliyunOssFileService;
import com.taoyuanx.littlefile.fdfs.FdfsFileService;
import com.taoyuanx.littlefile.support.FileDownStrategy;

public class AliyunOssStrategy implements FileDownStrategy {
	private AliyunOssFileService ossFileService;

	public AliyunOssStrategy(AliyunOssFileService ossFileService) {
		this.ossFileService = ossFileService;
	}

	@Override
	public void down(String src, String dest) throws Exception {
		ossFileService.download(src, dest);
	}

}
