package com.taoyuanx.littlefile.server.service.impl;

import com.taoyuanx.littlefile.server.utils.FdfsFileUtil;
import com.taoyuanx.littlefile.server.utils.FilenameUtils;
import com.taoyuanx.littlefile.server.dto.FileInfo;
import com.taoyuanx.littlefile.server.dto.ImageWH;
import com.taoyuanx.littlefile.server.dto.MasterAndSlave;
import com.taoyuanx.littlefile.server.ex.ParamException;
import com.taoyuanx.littlefile.server.ex.ServiceException;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.taoyuanx.littlefile.server.service.FastdfsService;
import com.taoyuanx.littlefile.server.service.FileValidateService;
import com.taoyuanx.littlefile.server.utils.CodeUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Service
public class FastdfsServiceImpl implements FastdfsService {
	@Autowired
	private FileValidateService fileValidateService;
	@Autowired
	FdfsFileUtil fdfsFileUtil;



	@Override
	public String uploadFile(MultipartFile file) throws ServiceException {
		try {
			fileValidateService.validateFile(file);
			long fileSize = file.getSize();
			if (fileSize <= 0) {
				throw new ParamException("file is null.");
			}
			String path= fdfsFileUtil.upload(file.getInputStream(), file.getOriginalFilename());
			if (path == null) {
				throw new ServiceException("upload error.");
			}
			return path;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public String uploadSlaveFile(String masterFileId, MultipartFile file) throws ServiceException {
		try {
			fileValidateService.validateFile(file);
			long fileSize = file.getSize();
			if (fileSize <= 0) {
				throw new ParamException("file is null.");
			}
			String fileName=file.getOriginalFilename();
			//随机生成从文件前缀,防止重名异常
			String filePrefixName= FilenameUtils.getPrefixRandom(fileName);
			String path=fdfsFileUtil.uploadSlave(masterFileId, file.getBytes(), filePrefixName, fileName);
			if (path == null) {
				throw new ServiceException("slave upload error.");
			}
			return path;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public String uploadSlaveFile(String masterFilename,String prefixName,MultipartFile file) throws ServiceException {
		try {
			fileValidateService.validateFile(file);
			long fileSize = file.getSize();
			if (fileSize <= 0) {
				throw new ParamException("file is null.");
			}
			prefixName=prefixName+"_"+FilenameUtils.generateShortUuid();
			String path=fdfsFileUtil.uploadSlave(masterFilename,file.getBytes(),prefixName,file.getOriginalFilename());
			if (path == null) {
				throw new ServiceException("slave upload error.");
			}
			return path;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	
	@Override
	public MasterAndSlave uploadImageAndThumb(String cutSize, MultipartFile file) throws ServiceException {
		try {
			fileValidateService.validateFile(file);
			long fileSize = file.getSize();
			if (fileSize <= 0) {
				throw new ParamException("file is null.");
			}
			if (StringUtils.isEmpty(cutSize)) {
				throw new ParamException("cutSize is null.");
			}
			String ext = FilenameUtils.getExtension(file.getOriginalFilename());
			//复制一份主图
			File sourceFile = new File(CodeUtil.getUUID());
			FileUtils.copyInputStreamToFile(file.getInputStream(), sourceFile);
			
			//生成缩略图
			List<ImageWH> whs = loadCutSize(cutSize);
			int len=whs.size();
			InputStream[] slaveInputs=new InputStream[len];
			List<String> slaveNames=new ArrayList<>(len) ;
			for(int i=0;i<len;i++){
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageWH wh=whs.get(i);
				Thumbnails.of(sourceFile).size(wh.getW(), wh.getH()).toOutputStream(out);
				slaveInputs[i]=new ByteArrayInputStream(out.toByteArray());
				slaveNames.add(String.format("%dx%d", wh.getW(), wh.getH())+"."+ext); 
			}
			MasterAndSlave uploadMasterAndSlave = fdfsFileUtil.uploadMasterAndSlave(null,file.getInputStream(), 
					file.getOriginalFilename(), 
					slaveNames, slaveInputs);
			//删除临时
			FileUtils.deleteQuietly(sourceFile);
			return uploadMasterAndSlave;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public boolean removeFile(String fileName) throws ServiceException {
		if (StringUtils.isEmpty(fileName)) {
			throw new ParamException("fileName is null");
		}
		try {
		    fdfsFileUtil.delete(fileName);
			return true;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void download(String fileId, String destFile) throws ServiceException {
		if (StringUtils.isEmpty(fileId)) {
			throw new ParamException("fileId is null");
		}
		try {
			fdfsFileUtil.download(fileId, destFile);
		} catch (Exception e) {
			throw new ServiceException(e);
		}
		
	}

	private List<ImageWH> loadCutSize(String cutSize) throws ParamException {
		List<ImageWH> whs = null;
		if (!StringUtils.isEmpty(cutSize)) {
			try {
				List<String> sizes = Arrays.asList(cutSize.split(","));
				whs = new ArrayList<>();
				for (String size : sizes) {
					String vals[] = size.split("x");
					int w = Integer.parseInt(vals[0]);
					int h = Integer.parseInt(vals[1]);
					whs.add(new ImageWH(w, h));
				}

			} catch (Exception e) {
				throw new ParamException("cutSize is error");
			}
		}
		return whs;
	}

	@Override
	public FileInfo getFileInfo(String fileId) throws ServiceException {
		if (StringUtils.isEmpty(fileId)) {
			throw new ParamException("fileId is null");
		}
		try {
			org.csource.fastdfs.FileInfo fileInfo = fdfsFileUtil.getFileInfo(fileId);
			if(null==fileInfo){
				throw  new ServiceException("文件不存在"); 
			}
			FileInfo info= new FileInfo();
			info.setCrc32(fileInfo.getCrc32());
			info.setCreate_timestamp(fileInfo.getCreateTimestamp().getTime());
			info.setFile_size(fileInfo.getFileSize());
			return info ;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	
}
