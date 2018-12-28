package com.taoyuanx.littlefile.support.impl;

import com.taoyuanx.littlefile.ftp.LittleFileFtp;
import com.taoyuanx.littlefile.support.FileDownStrategy;

public class FtpStrategy implements FileDownStrategy {
	private LittleFileFtp littleFileFtp;
	
	public FtpStrategy(LittleFileFtp littleFileFtp) {
		this.littleFileFtp = littleFileFtp;
	}

	@Override
	public void down(String src, String dest) throws Exception {
		littleFileFtp.download(src, dest);
		littleFileFtp.close();
	}

}
