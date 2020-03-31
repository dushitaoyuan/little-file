package com.taoyuanx.littlefile.client;

import java.io.InputStream;

public interface FileClient {
	/**本地文件上传
	 * @param localFile
	 * @return
	 */
	String upload(String localFile);
	
	/**文件上传
	 */
	String upload(byte[] fileBytes, String fileName);
	
	/**文件上传

	 */
	String upload(InputStream fileInput, String fileName);
	
	/**上传本地图片
	 * @param localFile 
	 * @param cutSize 预览图尺寸 如:20x20,30x30,100x100 不传,不生成预览图,可生成多个预览图
	 * @return
	 */
	String uploadImage(String localFile, String cutSize);
	
	/**上传本地图片
	 * @param cutSize 预览图尺寸  如:20x20,30x30,100x100 不传,不生成预览图,可生成多个预览图
	 * @return
	 */
	String uploadImage(byte[] fileBytes, String fileName, String cutSize);
	/**上传本地图片
	 * @param cutSize 预览图尺寸  如:20x20,30x30,100x100 不传,不生成预览图,可生成多个预览图
	 * @return
	 */
	String uploadImage(InputStream fileInput, String fileName, String cutSize);
	/**文件从上传
	 * @param localFile
	 */
	String uploadSlave(String localFile, String fileId);
	
	/**文件从上传
	 */
	String uploadSlave(byte[] fileBytes, String fileName, String fileId);
	
	/**文件从上传
	 */
	String uploadSlave(InputStream fileInput, String fileName, String fileId);
	/**删除文件id
	 * @param fileId 
	 */
	boolean delete(String fileId);
	
	
	/**文件下载
	 * @param fileId
	 * @param destFile
	 */
	void downLoad(String fileId, String destFile);
	
	byte[] downLoad(String fileId);
	/**
	 * 获取文件信息
	 * @param fileId 文件id
	 * @return json
	 */
    String getFileInfo(String fileId);
}
