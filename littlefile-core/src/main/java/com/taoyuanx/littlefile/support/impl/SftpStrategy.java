package com.taoyuanx.littlefile.support.impl;

import com.taoyuanx.littlefile.sftp.LittleFileSftp;
import com.taoyuanx.littlefile.support.FileDownStrategy;

public class SftpStrategy implements FileDownStrategy {
	private LittleFileSftp littleFileSftp;
	
	public SftpStrategy(LittleFileSftp littleFileSftp) {
		this.littleFileSftp = littleFileSftp;
	}

	@Override
	public void down(String src, String dest) throws Exception {
		littleFileSftp.download(src, dest);
	}

}
