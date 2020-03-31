package com.taoyuanx.littlefile.server.fdfs;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.FileInfo;

public class FileAttr {
	/**
	 * file_size 文件大小
	 * create_timestamp 文件创建时间
	 * crc32 文件crc32校验码
	 * metaMap 文件附带源信息
	 */
	private long file_size;
	private Date create_timestamp;
	private long crc32;
	Map<String,String> metaMap;
	public static final String SLAVE_KEY="slave";
	public static final String FILENAME_KEY="filename";
	public static final String SLAVE_SPLIT=",";

	public FileAttr() {
		super();
	}
	public FileAttr(FileInfo fileInfo) {
		super();
		copyInfo(fileInfo);
	}
	public FileAttr(FileInfo fileInfo, NameValuePair[] nameValuePairs) {
		super();
		copyInfo(fileInfo);
		copyMetaMap(nameValuePairs);
			
	}
	
	private void copyInfo(FileInfo fileInfo){
		this.file_size=fileInfo.getFileSize();
		this.create_timestamp=fileInfo.getCreateTimestamp();
		this.crc32=fileInfo.getCrc32();
	}
	private void copyMetaMap(NameValuePair[] nameValuePairs){
		if(null==nameValuePairs||nameValuePairs.length==0){
			return;
		}
		metaMap=new HashMap<>(nameValuePairs.length);
		for(NameValuePair n:nameValuePairs){
			metaMap.put(n.getName(), n.getValue());
		}
	}
	
	public long getFile_size() {
		return file_size;
	}
	public void setFile_size(long file_size) {
		this.file_size = file_size;
	}
	public Date getCreate_timestamp() {
		return create_timestamp;
	}
	public void setCreate_timestamp(Date create_timestamp) {
		this.create_timestamp = create_timestamp;
	}
	public long getCrc32() {
		return crc32;
	}
	public void setCrc32(long crc32) {
		this.crc32 = crc32;
	}
	public Map<String, String> getMetaMap() {
		return metaMap;
	}
	public void setMetaMap(Map<String, String> metaMap) {
		this.metaMap = metaMap;
	}
	public String getMetaValue(String key){
		if(metaMap==null||metaMap.isEmpty()){
			return null;
		}
		return metaMap.get(key);
	}
	public String getFileName() {
		if(metaMap==null||metaMap.isEmpty()){
			return null;
		}
		return metaMap.get(FILENAME_KEY);
	}
	
	public String[] getSlaves() {
		if(metaMap==null||metaMap.isEmpty()){
			return null;
		}
		if(metaMap.containsKey(SLAVE_KEY)) {
			return metaMap.get(SLAVE_KEY).split(SLAVE_SPLIT);
		}else {
			return null;
		}
	}
	
}
