package com.taoyuanx.littlefile.server.utils;

import java.util.UUID;

/**
 * @author dushitaoyuan
 * 为了不依赖commonsio 自己重写工具类
 */
public class FilenameUtils {
	public static String[] chars = new String[] { "a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z" };
 
 
/** 生成8位短id
 * @return
 */
public static String generateShortUuid() {
    StringBuffer shortBuffer = new StringBuffer();
    String uuid = UUID.randomUUID().toString().replace("-", "");
    for (int i = 0; i < 8; i++) {
        String str = uuid.substring(i * 4, i * 4 + 4);
        int x = Integer.parseInt(str, 16);
        shortBuffer.append(chars[x % 0x3E]);
    }
    return shortBuffer.toString();
 
}
	private static final String filePattern="%s.%s";
	/** 从文件名获取扩展名
	 * @param fileName
	 * @return
	 */
	public static String getExtension(String fileName){
		return fileName.substring(fileName.lastIndexOf(".")+1);
	}
	/**
	 * 获取文件前缀
	 * @param fileName
	 * @return
	 */
	public static String getPrefix(String fileName){
		return fileName.substring(0,fileName.lastIndexOf("."));
	}
	
	/** 防止从文件重名
	 * @param fileName
	 * @return
	 */
	public static String getPrefixRandom(String fileName){
		return fileName.substring(0,fileName.lastIndexOf("."))+"_"+generateShortUuid();
	}
	
	/**合并
	 * @param prefix
	 * @param ext
	 * @return
	 */
	public static String merge(String prefix,String ext){
		return String.format(filePattern, prefix,ext);
	}
	public static void main(String[] args) {
		System.out.println(getPrefixRandom("11.pdf"));
	}
	
}
