package com.taoyuanx.littlefile.server.service;

import com.taoyuanx.littlefile.fdfshttp.core.dto.FileInfo;
import com.taoyuanx.littlefile.fdfshttp.core.dto.MasterAndSlave;
import com.taoyuanx.littlefile.server.ex.ServiceException;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;


public interface FastdfsService {
	
	/**
	 * @param file 文件
	 * @return
	 * @throws ServiceException
	 */
	String uploadFile(MultipartFile file) throws ServiceException;

	String uploadAppendFile(MultipartFile file) throws ServiceException;

	void appendFile(String fileId, MultipartFile file) throws ServiceException;
	void modifyFile(MultipartFile file, Long offset, String fileId);

	/**
     * @param masterFileId 主文件地址
     * @param file  文件
     * @return
     * @throws ServiceException
     */
    String uploadSlaveFile(String masterFileId, MultipartFile file) throws ServiceException;
    /**
     *
     * @param masterFileId
     * @param prefixName 指定文件名称
     * @param file
     * @return
     * @throws ServiceException
     */
    String uploadSlaveFile(String masterFileId, String prefixName, MultipartFile file) throws ServiceException;


    /**
     * @param cutSize 生成缩略图尺寸 
     * @param file 文件
     * @return
     * @throws ServiceException
     */
    MasterAndSlave uploadImageAndThumb(String cutSize, MultipartFile file) throws ServiceException;
    /**
     * @param fileId 删除文件名称 ,如果存在从文件,从文件也删除
     * @return
     * @throws ServiceException
     */
    boolean removeFile(String fileId) throws ServiceException;
	/**文件下载
	 * @param fileId
	 * @param outputStream
	 * @throws ServiceException
	 */
	void download(String fileId, OutputStream outputStream) throws ServiceException;


	/**文件断点 下载
	 * @param fileId
	 * @param outputStream
	 * @param start 起始位置
	 * @param len 下载长度
	 * @throws ServiceException
	 */
	void download(String fileId, Long start,Long len,OutputStream outputStream) throws ServiceException;




	/** 通过文件id 获取文件信息
	 * @param fileId
	 * @return
	 * @throws ServiceException
	 */
	FileInfo getFileInfo(String fileId) throws ServiceException;


}
