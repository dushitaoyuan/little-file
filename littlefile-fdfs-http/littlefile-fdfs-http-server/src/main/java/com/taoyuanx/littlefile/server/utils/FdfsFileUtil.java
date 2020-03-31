package com.taoyuanx.littlefile.server.utils;

import com.taoyuanx.littlefile.server.dto.MasterAndSlave;
import com.taoyuanx.littlefile.server.ex.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.List;
/**
 * @author ncs-spf
 *可以下载对应的 fdfs包,可以兼容所有的fdfs
 */
@Component
@Slf4j
public class FdfsFileUtil {
	static {
		try {
			ClientGlobal.initByProperties("fdfs.properties");
			log.info("fdfs config:{}",ClientGlobal.configInfo());
			System.out.println(ClientGlobal.configInfo());
		} catch (Exception e) {
			log.error("fastdfs 初始化配置失敗，请检查fdfs.properties 配置");
		}

	}

	static TrackerClient tracker = new TrackerClient();

	public StorageClient1 getClient() throws Exception {
		return new StorageClient1(tracker.getConnection(), null);
	}

	/**上传文件
	 * @param data
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public String upload(byte[] data, String fileName) throws Exception {
		NameValuePair[] metaList = new NameValuePair[] { new NameValuePair("fileName", fileName) };
		String fileExtName = FilenameUtils.getExtension(fileName);
		return getClient().upload_file1(data, fileExtName, metaList);
	}
	/**上传文件
	 * @param data
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public String upload(String group, byte[] data, String fileName) throws Exception {
		NameValuePair[] metaList = new NameValuePair[] { new NameValuePair("fileName", fileName) };
		String fileExtName = FilenameUtils.getExtension(fileName);
		return getClient().upload_file1(group, data, fileExtName, metaList);
	}
	/**上传文件
	 * @param input
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public String upload(InputStream input, String fileName) throws Exception {
		NameValuePair[] metaList = new NameValuePair[] { new NameValuePair("fileName", fileName) };
		String fileExtName = FilenameUtils.getExtension(fileName);
		return getClient().upload_file1(streamToArray(input), fileExtName, metaList);
	}
	/**上传文件
	 * @param input
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public String upload(String group, InputStream input, String fileName) throws Exception {
		NameValuePair[] metaList = new NameValuePair[] { new NameValuePair("fileName", fileName) };
		String fileExtName = FilenameUtils.getExtension(fileName);
		return getClient().upload_file1(group, streamToArray(input), fileExtName, metaList);
	}
	/**上传从文件
	 */
	public String uploadSlave(String masterFileId, InputStream input, String fileName) throws Exception {
		NameValuePair[] metaList = new NameValuePair[] { new NameValuePair("fileName", fileName) };
		String fileExtName = FilenameUtils.getExtension(fileName);
		String filePrefixName = FilenameUtils.getPrefix(fileName);
		return getClient().upload_file1(masterFileId, filePrefixName, streamToArray(input), fileExtName, metaList);
	}
	/**上传从文件
	 */
	public String uploadSlave(String masterFileId, InputStream input, String filePrefixName, String fileName)
			throws Exception {
		NameValuePair[] metaList = new NameValuePair[] { new NameValuePair("fileName", fileName) };
		String fileExtName = FilenameUtils.getExtension(fileName);
		return getClient().upload_file1(masterFileId, filePrefixName, streamToArray(input), fileExtName, metaList);
	}
	/**上传从文件
	 */
	public String uploadSlave(String masterFileId, byte[] input, String filePrefixName, String fileName)
			throws Exception {
		NameValuePair[] metaList = new NameValuePair[] { new NameValuePair("fileName", fileName) };
		String fileExtName = FilenameUtils.getExtension(fileName);
		return getClient().upload_file1(masterFileId, filePrefixName, input, fileExtName, metaList);
	}
	/**上传主从文件
	 */
	public MasterAndSlave uploadMasterAndSlave(String localMaster, String... localSlave) throws Exception {
		StorageClient1 client = getClient();
		File localMasterFile = new File(localMaster);
		File localSlaveFile = null;
		String fileName = localMasterFile.getName();
		NameValuePair[] metaList = new NameValuePair[] { new NameValuePair("fileName", fileName) };
		String fileExtName = FilenameUtils.getExtension(fileName);
		MasterAndSlave ms = MasterAndSlave.create(localSlave.length);
		// 上传主
		String master = client.upload_file1(fileName, fileExtName, metaList);
		ms.setMaster(master);
		// 上传从
		for (String s : localSlave) {
			localSlaveFile = new File(s);
			fileName = localSlaveFile.getName();
			metaList = new NameValuePair[] { new NameValuePair("fileName", fileName) };
			fileExtName = FilenameUtils.getExtension(fileName);
			String filePrefixName = FilenameUtils.getExtension(fileName);
			try {
				ms.addSlave(client.upload_file1(master, filePrefixName, s, fileExtName, metaList));
				;
			} catch (Exception e) {
				System.err.println(s + "从文件上传失败");
			}
		}

		return ms;
	}
	/**上传主从文件
	 */
	public MasterAndSlave uploadMasterAndSlave(String group, InputStream masterInput, String masterName,
											   List<String> slaveNames, InputStream... slaveInputs) throws Exception {
		StorageClient1 client = getClient();
		String fileName = masterName;
		NameValuePair[] metaList = new NameValuePair[] { new NameValuePair("fileName", fileName) };
		String fileExtName = FilenameUtils.getExtension(fileName);
		MasterAndSlave ms = MasterAndSlave.create(slaveNames.size());
		// 上传主
		String master = null;
		if (StringUtils.isEmpty(group)) {
			master = client.upload_file1(streamToArray(masterInput), fileExtName, metaList);
		} else {
			master = client.upload_file1(group, streamToArray(masterInput), fileExtName, metaList);
		}
		ms.setMaster(master);
		// 上传从
		for (int i = 0, len = slaveNames.size(); i < len; i++) {
			fileName = slaveNames.get(i);
			metaList = new NameValuePair[] { new NameValuePair("fileName", fileName) };
			fileExtName = FilenameUtils.getExtension(fileName);
			String filePrefixName = FilenameUtils.getPrefix(fileName);
			try {
				ms.addSlave(client.upload_file1(master, filePrefixName, streamToArray(slaveInputs[i]), fileExtName,
						metaList));
			} catch (Exception e) {
				System.err.println(fileName + "从文件上传失败");
			}
		}
		return ms;
	}
	/**上传本地文件
	 */
	public String upload(String file, String fileName) throws Exception {
		NameValuePair[] metaList = new NameValuePair[] { new NameValuePair("fileName", fileName) };
		return getClient().upload_file1(file, null, metaList);
	}
	/**上传本地文件
	 */
	public String upload(String file) throws Exception {
		String fileName = new File(file).getName();
		NameValuePair[] metaList = new NameValuePair[] { new NameValuePair("fileName", fileName) };
		return getClient().upload_file1(file, null, metaList);
	}
	/**下载文件
	 */
	public void download(String fileId, OutputStream out) throws Exception {
		byte[] file = getClient().download_file1(fileId);
		out.write(file);
		out.close();
	}
	/**下载文件
	 */
	public int download(String fileId, String destFile) throws Exception {// 0成功
		 int res = getClient().download_file1(fileId, destFile);
		 if(0==res){
			 throw new ServiceException("文件下载失败");
		 }
		return res;
	}
	/**下载文件
	 */
	public int download(String fileId, File destFile) throws Exception {// 0成功
		int res = getClient().download_file1(fileId, destFile.toString());
		 if(0==res){
			 throw new ServiceException("文件下载失败");
		 }
		return res;
	}
	/**删除
	 */
	public int delete(String fileId) throws Exception {// 0成功
		return getClient().delete_file1(fileId);
	}
/**
 * 文件流转字节数组
 * @param input
 * @return
 * @throws IOException
 */
	public byte[] streamToArray(InputStream input) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(input.available());
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = input.read(buf)) != -1) {
			out.write(buf, 0, len);
		}
		input.close();
		return out.toByteArray();
	}

	/**
	 * 获取文件信息
	 * 
	 * @param fileId
	 * @return
	 * @throws Exception
	 */
	public FileInfo getFileInfo(String fileId) throws Exception {
		return getClient().get_file_info1(fileId);
	}
	
	
	
	/**
	 * @param client 分片上传共同使用同一个连接
	 * @param chunkData 分片数据
	 * @return
	 * @throws Exception
	 */
	public String chunksUpload(StorageClient1 client,byte[] chunkData) throws Exception{
		return  client.upload_file1(chunkData, null, null);
	}
	
	/**分片下载
	 * @param client
	 * @param chunkPath
	 * @return
	 * @throws Exception
	 */
	public byte[] chunksDownload(StorageClient1 client,String chunkPath) throws Exception{
		return  client.download_file1(chunkPath);
	}
	
	/**分片下载
	 * @param client
	 * @param chunkPath
	 * @return
	 * @throws Exception
	 */
	public int chunksDelete(StorageClient1 client,String chunkPath) throws Exception{
		return  client.delete_file1(chunkPath);
	}

	

	
}
