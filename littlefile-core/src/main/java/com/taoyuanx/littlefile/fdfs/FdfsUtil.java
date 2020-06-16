package com.taoyuanx.littlefile.fdfs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.csource.common.NameValuePair;

import com.taoyuanx.littlefile.util.Utils;

public class FdfsUtil {

	/** 获取文件名
	 * @param file
	 * @return
	 */
	public static String getFileName(String file){
		int index = file.lastIndexOf("/") + 1;
		if(index>-1){
			return file.substring(index);
		}else{
			return new File(file).getName();
		}
	}

	/** 从文件名获取扩展名
	 * @param fileName
	 * @return
	 */
	public static String getExtension(String fileName){
		if(Utils.isEmpty(fileName)) {
			return null;
		}
		return fileName.substring(fileName.lastIndexOf(".")+1);
	}
	/** 从文件名获取扩展名
	 * @param ext
	 * @return
	 */
	public static String getExtension(Map<String,String> metaMap,String ext){
		if(Utils.isNotEmpty(ext)) {
			return ext;
		}
		String fileName= metaMap==null ? null:metaMap.get(FileAttr.FILENAME_KEY);
		if(Utils.isEmpty(fileName)) {
			return null;
		}
		return fileName.substring(fileName.lastIndexOf(".")+1);
	}
	/**
	 * 获取文件前缀
	 * @param fileName
	 * @return
	 */
	public static String getPrefix(String fileName){
		if(Utils.isEmpty(fileName)) {
			return null;
		}
		return fileName.substring(0,fileName.lastIndexOf("."));
	}

	/** slave 从文件 meta信息格式 xxx,xxx
	 * @param slaves
	 * @return
	 */
	public static String slaveToPair(String ...slaves){
		StringBuilder buf=new StringBuilder();
		int count=0,len=slaves.length-1;
		for(String slave:slaves){
			buf.append(slave);
			if(count<len){
				buf.append(FileAttr.SLAVE_SPLIT);
			}
			count++;
		}
		return buf.toString();
	}

	/** 合并文件 meta信息
	 * @param srcMeta
	 * @param fileName
	 * @param slaves
	 * @return
	 */
	public static NameValuePair[] merage(NameValuePair[] srcMeta,String fileName,String ...slaves){

		boolean fFlag=fileName!=null&&!"".equals(fileName);
		boolean sFlag=null!=slaves&&slaves.length>0;
		if(srcMeta==null||srcMeta.length==0){
			List<NameValuePair> nameValues=new ArrayList<>();
			if(fFlag){
				nameValues.add(new NameValuePair(FileAttr.FILENAME_KEY, fileName));
			}
			if(sFlag){
				nameValues.add(new NameValuePair(FileAttr.SLAVE_KEY, slaveToPair(slaves)));
			}
			return nameValues.toArray(new NameValuePair[nameValues.size()]);
		}else{
			Map<String,NameValuePair> temp=new HashMap<>();
			for(NameValuePair v:srcMeta){
				temp.put(v.getName(), v);
			}
			if(fFlag){
				temp.put(FileAttr.FILENAME_KEY, new NameValuePair(FileAttr.FILENAME_KEY, fileName));
			}
			if(sFlag){
				String slaveValue=null;
				if(temp.containsKey(FileAttr.SLAVE_KEY)){
					slaveValue=temp.get(FileAttr.SLAVE_KEY).getValue().concat(FileAttr.SLAVE_SPLIT).concat(slaveToPair(slaves));
				}else{
					slaveValue=slaveToPair(slaves);
				}
				temp.put(FileAttr.SLAVE_KEY, new NameValuePair(FileAttr.SLAVE_KEY,slaveValue));
			}
			return temp.values().toArray(new NameValuePair[temp.size()]);
		}
	}


}
