package com.taoyuanx.littlefile.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taoyuanx.littlefile.fdfs.FdfsUtil;
import com.taoyuanx.littlefile.support.FileDownStrategy;
import com.taoyuanx.littlefile.util.Utils;
import com.taoyuanx.littlefile.web.security.AbstractSimpleTokenManager;

public class FileHandler {

	
	public static final String DOWN="0",LOOK="1";
	public static Logger LOG=LoggerFactory.getLogger(FileHandler.class);
	private String cacheDir;
	private FileDownStrategy fileDownStrategy;
	private boolean isGzip=false;
	private AbstractSimpleTokenManager tokenManager;
	public FileHandler(String cacheDir, FileDownStrategy fileDownStrategy,
			boolean isGzip,AbstractSimpleTokenManager tokenManager) {
		super();
		this.cacheDir = cacheDir;
		this.fileDownStrategy = fileDownStrategy;
		this.isGzip=isGzip;
		this.tokenManager=tokenManager;
	}
	public void handleFile(HttpServletResponse resp, HttpServletRequest req) {
		String type = req.getParameter(Constant.REQUEST_PARAM_TYPE_KEY);
		String filePath=req.getParameter(Constant.REQUEST_PARAM_FILE_KEY);
		try {
			if(tokenManager!=null) {
				String token=req.getParameter(Constant.REQUEST_PARAM_TOKEN_KEY);
				if(Utils.isEmpty(token)||!tokenManager.verify(filePath, token)) {
					resp.getWriter().println("token illegal");
					return;
				}
			}
			File absoluteFile = new File(cacheDir,filePath);
			//文件不存在或损坏,下载
			if(!Utils.isFileNotBad(absoluteFile)) {
				//父级目录不存在,创建
				File parentFile = absoluteFile.getParentFile();
				if(!parentFile.exists()){
					parentFile.mkdirs();
				}
				String dest =absoluteFile.getAbsolutePath();
				LOG.debug("download file:[{}] local path:[{}]",filePath,dest);
				fileDownStrategy.down(filePath, dest);
			}
			switch (type) {
			case LOOK: {// 查看
				resp.setContentType(req.getServletContext().getMimeType(absoluteFile.getName()));
			};break;
			case DOWN:{// 下载
				resp.setContentType(req.getServletContext().getMimeType(absoluteFile.getName()));
				resp.setHeader("Content-type", "application/octet-stream");
				resp.setHeader("Content-Disposition",
						"attachment;fileName=" +URLEncoder.encode(FdfsUtil.getFileName(filePath), "UTF-8"));
				resp.setContentType(req.getServletContext().getMimeType(absoluteFile.getName()));
			
			};break;
			}
			//gzip 压缩
			if(isGzip) {
				resp.setHeader("Content-Encoding", "gzip");
				GZIPOutputStream gzip=new GZIPOutputStream(resp.getOutputStream());
				handle(gzip, absoluteFile);
				gzip.close();
			}else {
				ServletOutputStream out = resp.getOutputStream();
				handle(out, absoluteFile);
				out.flush();
			}
		} catch (Exception e) {
			LOG.error("处理文件[{}]异常{}",filePath,e);
			try {
				resp.getWriter().println("file "+filePath+" error"+e.getMessage());
			} catch (IOException e1) {
			}
		}

	}
	private void handle(OutputStream out,File localFile) throws Exception {
		InputStream input=new FileInputStream(localFile);
		byte[] buf=new byte[1024*1024];
		int len=0;
		while((len=input.read(buf))!=-1) {
			out.write(buf,0,len);
		}
		input.close();
	}
	
}
