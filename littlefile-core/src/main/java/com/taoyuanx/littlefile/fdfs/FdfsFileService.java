package com.taoyuanx.littlefile.fdfs;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.DownloadStream;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.ProtoCommon;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.UploadStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 都市桃源 time:2018 
 * 上午10:09:31 
 * 使用要点: 
 * 1.fastdfs 操作 大文件不要使用byte数组形式上传下载
 * 2.如果需要保留文件名,传递文件名即可,这时一个文件对应两个磁盘文件a.源文件b.metamap文件(文件属性键值对,可自定义)
 * 3.名词解释 fileName 文件名 metaMap 文件对应的业务属性, 
  * 主从文件 一个主文件会对应多个从文件,但是删除主文件不会删除从文件 ,如果要实现这种业务特性,可通过fastdfs metaMap属性间接实现
 * 
 * 
 * 
 */
public class FdfsFileService {
	private static final Logger LOG = LoggerFactory.getLogger(FdfsFileService.class);

	public FdfsFileService() {
		try {
			ClientGlobal.initByProperties("fdfs.properties");
			LOG.info("fastdfs配置信息为:{}",ClientGlobal.configInfo());
		} catch (Exception e) {
			LOG.error("默认配置文件fdfs.properties 初始化配置失敗，请检查fdfs.properties 配置");
			throw new RuntimeException(e);
		}
	}

	public FdfsFileService(String configPath) {
		try {
			if (configPath.startsWith("classpath")) {
				Properties pro = new Properties();
				pro.load(FdfsFileService.class.getClassLoader()
						.getResourceAsStream(configPath.replaceFirst("classpath:", "")));
				ClientGlobal.initByProperties(pro);
			} else {
				ClientGlobal.init(configPath);
			}
			LOG.info("初始化fastdfs配置信息为：{}", ClientGlobal.configInfo());
		} catch (Exception e) {
			LOG.error("配置文件 {} 初始化配置失敗，请检查配置,异常信息为{}", configPath, e);
			throw new RuntimeException(e);
		}
	}

	
	static TrackerClient tracker = null;

	public StorageClient1 getClient() throws Exception {
		return new StorageClient1();
	}
	public String upload(byte[] data,String ext,Map<String,String> metaMap) throws Exception {
			NameValuePair[] metaList = buildMeta(metaMap);
			String fileExtName = FdfsUtil.getExtension(metaMap,ext);
			return getClient().upload_file1(data, fileExtName, metaList);
	}
	

	public String upload(InputStream input,String ext,Map<String,String> metaMap) throws Exception {
			NameValuePair[] metaList = buildMeta(metaMap);
			String fileExtName = FdfsUtil.getExtension(metaMap,ext);
			return getClient().upload_file1(null, input.available(), new UploadStream(input, input.available()),
					fileExtName, metaList);
	}

	public String[] upload(InputStream[] inputs,String[] exts,Map<String,String>[] metaMaps) throws Exception {
		List<String> filePaths = new ArrayList<>();
		StorageClient1 client = getClient();
		for (int i = 0, len = inputs.length; i < len; i++) {
			NameValuePair[] metaList = buildMeta(metaMaps[i]);
			String fileExtName = FdfsUtil.getExtension(metaMaps[i],exts[i]);
			filePaths.add(client.upload_file1(null, inputs[i].available(),
					new UploadStream(inputs[i], inputs[i].available()), fileExtName, metaList));
		}
		return filePaths.toArray(new String[filePaths.size()]);
	}

	public String upload(String file,Map<String,String> metaMap) throws Exception {
		String fileName=FdfsUtil.getFileName(file);
		NameValuePair[] metaList = buildMeta(metaMap);
		String fileExt=FdfsUtil.getExtension(fileName);
		return getClient().upload_file1(file, fileExt, metaList);
	}

	

	/**
	 * @param masterFileId 主文件id
	 * @param slaves 从文件本地路径
	 * @param metaMaps 从文件元信息数组 可为空数组
	 * @param setMmeta 是否使用metaMap建议主从文件关联
	 * @return
	 * @throws Exception
	 */
	public String[] uploadSlave(String masterFileId,String[] slaves,Map<String,String>[] metaMaps,boolean setMmeta) throws Exception{
		if(null==slaves||slaves.length==0){
			return null;
		}
		StorageClient1 client = getClient();
		String fileName=null,pre=null,ext=null;
		String[] slave_meta=new String[slaves.length];
		int count=0;
		boolean flag=metaMaps==null;
		for(String slave:slaves){
			fileName=FdfsUtil.getFileName(slave);
			pre=FdfsUtil.getPrefix(fileName);
			ext=FdfsUtil.getExtension(fileName);
			NameValuePair[] meta=null;
			if(!flag) {
				meta=buildMeta(metaMaps[count]);
			}
			slave_meta[count]=client.upload_file1(masterFileId, pre, slave, ext, meta);
			count++;
		}
		if(setMmeta) {
			//将新增的从文件信息添加到主文件meta信息中
			NameValuePair[] merage = FdfsUtil.merage(client.get_metadata1(masterFileId), null, slave_meta);
			client.set_metadata1(masterFileId, merage, ProtoCommon.STORAGE_SET_METADATA_FLAG_OVERWRITE);
		}
		return slave_meta;
		
	}


	/** 
	 * @param masterFile 主文件本地路径
	 * @param masterMetaMap 主文件元信息
	 * @param slaves 从文件数组
	 * @param metaMaps 从文件源信息数组
	 * @param setMmeta 是否使用主文件meta信息建立主从关联
	 * @return
	 * @throws Exception
	 */
	public MasterSlave uploadMasterSlave(String masterFile,Map<String,String> masterMetaMap,String[] slaves,Map<String,String>[] metaMaps,boolean setMmeta) throws Exception{
		StorageClient1 client = getClient();
		String fileName=null,pre=null,ext=null,masterFileName="";
		NameValuePair[] meta=null;
		//上传主文件
		masterFileName=FdfsUtil.getFileName(masterFile);
		meta=null;
		String masterId=client.upload_file1(masterFile, FdfsUtil.getExtension(masterFileName),null);
		String[] slave_meta=new String[slaves.length];
		int count=0;
		boolean flag=metaMaps==null;
		for(String slave:slaves){
			fileName=FdfsUtil.getFileName(slave);
			pre=FdfsUtil.getPrefix(fileName);
			ext=FdfsUtil.getExtension(fileName);
			if(!flag) {
				meta=buildMeta(metaMaps[count]);
			}
			slave_meta[count] = client.upload_file1(masterId, pre, slave, ext, meta);
			count++;
		}
		if(setMmeta) {
			//主文件meta信息中存放 主文件名和从文件数组
			meta= FdfsUtil.merage(buildMeta(masterMetaMap), masterFileName, slaves);
			client.set_metadata1(masterId,meta, ProtoCommon.STORAGE_SET_METADATA_FLAG_OVERWRITE);
		}
		MasterSlave ms=new MasterSlave();
		ms.setMasterId(masterId);
		ms.setSlaves(slave_meta);
		return ms;
		
	}
	
	/**如果主文件中meta信息中包含从文件信息,然后级联删除从文件
	 * @param masterId
	 * @throws Exception
	 */
	public void deleteMasterSlave(String masterId) throws Exception{
		//删除主文件
		 StorageClient1 client = getClient();
		NameValuePair[] nv =client.get_metadata1(masterId);
		client.delete_file1(masterId);
		//删除从文件
		if(nv!=null) {
			String[] slaves=null;
			for(NameValuePair v:nv) {
				if(v.getName().equals(FileAttr.SLAVE_KEY)) {
					slaves=v.getValue().split(FileAttr.SLAVE_SPLIT);
					break;
				}
			}
			if(slaves!=null) {
				for(String s:slaves){
					client.delete_file1(s);
				}
				
			}
		}
	}

	
	

	/**如果主文件中meta信息中包含从文件信息,然后级联删除从文件
	 * @param masterId  主文件
	 * @param slaves 从文件
	 * @throws Exception
	 */
	public void deleteMasterSlave(String masterId,String ...slaves) throws Exception{
		//删除主文件
		 StorageClient1 client = getClient();
		client.delete_file1(masterId);
		//删除从文件
		if(null!=slaves) {
			for(String s:slaves) {
				client.delete_file1(s);
			}
		}
	}
	
	public void delete(String ...fileId) throws Exception {// 0成功
		 StorageClient1 client = getClient();
		 if(fileId!=null&&fileId.length>0) {
			 for(String f:fileId){
				client.delete_file1(f); 
			 }
		 }
	}
	public void download(String fileId, OutputStream out) throws Exception {
		getClient().download_file1(fileId, new DownloadStream(out));
	}



	public int download(String fileId, String destFile) throws Exception {// 0成功
		return getClient().download_file1(fileId, destFile);
	}

	public int download(String fileId, File destFile) throws Exception {// 0成功
		return getClient().download_file1(fileId, destFile.getAbsolutePath());
	}
	
	public byte[] download(String fileId) throws Exception {
		return getClient().download_file1(fileId);
	}

	/**
	 * 获取文件大小
	 * 
	 * @param fileId
	 * @return
	 * @throws Exception
	 */
	public Long getFileByteSize(String fileId) throws Exception {
		FileInfo fileInfo = getClient().get_file_info1(fileId);
		if (null == fileInfo)
			return null;
		return fileInfo.getFileSize();
	}

	/**
	 * @param fileId 文件id
	 * @param withMeta 是否包含 meta信息
	 * @return 文件信息
	 * @throws Exception
	 */
	public FileAttr getFileInfo(String fileId,boolean withMeta)throws Exception{
		StorageClient1 client = getClient();
		String[] gf=new String[2];
		StorageClient1.split_file_id(fileId, gf);
		FileInfo info = client.get_file_info(gf[0], gf[1]);
		if(info==null) {
			return null;
		}
		if(withMeta){
			NameValuePair[] meta = client.get_metadata(gf[0], gf[1]);
			if(meta==null) {
				return null;
			}
			return new FileAttr(info, meta);
		}else{
			return new FileAttr(info);
		}
	}
	public FileAttr getFileInfo(String fileId)throws Exception{
		return getFileInfo(fileId, false);
	}
	
	public NameValuePair[] buildMeta(Map<String,String> metaMap) {
		if(metaMap!=null) {
			NameValuePair[] nv=new NameValuePair[metaMap.size()];
			int count=0;
			for(String key:metaMap.keySet()) {
				nv[count++]=new NameValuePair(key,metaMap.get(key));
			}
			return nv;
		}
		return null;
	}
	
	public  FdfsFileService metaMap(Map<String,String> metaMap,String key,String value){
		metaMap.put(key, value);
		return this;
	}
	
	
}
