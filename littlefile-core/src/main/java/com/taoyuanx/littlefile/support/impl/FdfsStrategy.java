package com.taoyuanx.littlefile.support.impl;

import com.taoyuanx.littlefile.fdfs.FdfsFileService;
import com.taoyuanx.littlefile.support.FileDownStrategy;

public class FdfsStrategy implements FileDownStrategy {
	private FdfsFileService fdfsFileService;
	
	public FdfsStrategy(FdfsFileService fdfsFileService) {
		this.fdfsFileService = fdfsFileService;
	}

	@Override
	public void down(String src, String dest) throws Exception {
		fdfsFileService.download(src, dest);
	}

}
