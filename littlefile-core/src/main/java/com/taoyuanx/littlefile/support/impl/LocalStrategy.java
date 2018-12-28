package com.taoyuanx.littlefile.support.impl;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.taoyuanx.littlefile.support.FileDownStrategy;

public class LocalStrategy implements FileDownStrategy{
	private String[] localDirs;
	private boolean isEmpty=false;
	public LocalStrategy(String[] localDirs) {
		if(localDirs==null||localDirs.length==0) {
			isEmpty=true;
			return;
		}
		this.localDirs = localDirs;
	}
	@Override
	public void down(String src, String dest) throws Exception {
		if(isEmpty) {
			return;
		}
		File destFile=new File(dest);
		File srcFile=null;
		for(String dir:localDirs) {
			srcFile=new File(dir,src);
			if(srcFile.exists()) {
				FileUtils.copyFile(srcFile, destFile);
				return;
			}
		}
		throw new Exception("local file not find");
	}



}
