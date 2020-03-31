package com.taoyuanx.littlefile.server.utils;

import java.io.File;

public class Utils {
	public static boolean isEmpty(String str){
		if(null==str||str.length()==0){
			return true;
		}
		return false;
	}
	public static boolean isNotEmpty(String str){
		if(null!=str&&str.trim().length()>0){
			return true;
		}
		return false;
	}
	
	
	/**
	 *  判断文件是否完整
	 * @param localFile
	 * @return
	 */
	public static boolean isFileNotBad(File localFile) {
		boolean isFileNotBad = localFile.exists() && localFile.length() > 1024;
		return isFileNotBad;
	}
}
