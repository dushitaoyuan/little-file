package com.taoyuanx.littlefile.ftp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taoyuanx.littlefile.util.Utils;

public class LittleFileFtp {
	private static final Logger LOG = LoggerFactory.getLogger(LittleFileFtp.class);
	private String host, username, password, workDir;
	private int port = 21;
	private KeyManager keyManager;
	private TrustManager trustManager;
	private boolean isWorkDirNotEmpty = false, isSSL = false;
	private int default_timeout=3000;
	
	ThreadLocal<FTPClient> clientLocal = new ThreadLocal<FTPClient>();
	public LittleFileFtp(String host , int port,String username,String password,  String workDir, String keyManagerPath, 		String keyManagerPassword,
			String trustManagerPath, String trustManagerPassword) {
		super();
		this.host = host;
		this.username = username;
		this.password = password;
		this.workDir = workDir;
		this.isWorkDirNotEmpty = Utils.isNotEmpty(workDir)?"/".equals(workDir):false;
		try {
			if(Utils.isNotEmpty(keyManagerPath)) {
				keyManager = Utils.getKeyManager(keyManagerPath, keyManagerPassword);
				trustManager = Utils.getTrustManager(trustManagerPath, trustManagerPassword);
				isSSL = true;
			}
		} catch (Exception e) {
			throw new RuntimeException("load keyManager,trustManager failed,异常:{}", e);
		}
	
		this.port = port;
	}

	public FTPClient getFtpClient() throws Exception {
		try {
			FTPClient local = clientLocal.get();
			if(local==null) {
				FTPClient ftpClient = null;
				if(isSSL) {
					FTPSClient ftpsClient=new FTPSClient(true);
					ftpsClient.setKeyManager(keyManager);
					ftpsClient.setTrustManager(trustManager);
					//ftpsClient.execPBSZ(0);
					//ftpsClient.execPROT("P");
					ftpClient=ftpsClient;
				}else {
					ftpClient=new FTPClient();
				}
				ftpClient.setDefaultTimeout(default_timeout);
				ftpClient.connect(host, port);
				ftpClient.login(username, password);
				int reply = ftpClient.getReplyCode();
				if (!FTPReply.isPositiveCompletion(reply)) {
					LOG.info("{} ftp server 拒绝连接", host);
				}
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
				if (isWorkDirNotEmpty) {
					ftpClient.changeWorkingDirectory(workDir);
				}
				ftpClient.setControlEncoding("UTF-8");
				clientLocal.set(ftpClient);
				return ftpClient;
			}else {
				return local;
			}
			
		} catch (Exception e) {
			LOG.error("{} ftp server connect failed,异常", e);
			throw e;
		}
	}

	public void upload(InputStream input, String dest) throws Exception {
		try {
			FTPClient client = getFtpClient();
			mkdirs(dest);
			client.storeFile(dest, input);
		} catch (Exception e) {
			LOG.error("upload  file  failed,异常", e);
			throw e;
		}
	}

	public void upload(String src, String dest) throws Exception {
		try {
			FTPClient client = getFtpClient();
			mkdirs(dest);
			client.storeFile(dest, new FileInputStream(src));
		} catch (Exception e) {
			LOG.error("upload  file {}  failed,异常", src, e);
			throw e;
		}
	}

	public void download(String src, String dest) throws Exception {
		try {
			FTPClient client = getFtpClient();
			client.retrieveFile(src, new FileOutputStream(dest));
		} catch (Exception e) {
			LOG.error("download  file {}  failed,异常", dest, e);
			throw e;
		}
	}

	public void download(String src, OutputStream out) throws Exception {
		try {
			FTPClient client = getFtpClient();
			client.retrieveFile(src, out);
		} catch (Exception e) {
			LOG.error("download  file {}  failed,异常", src, e);
			throw e;
		}
	}

	public void mkdirs(String path) throws Exception {
		try {
			FTPClient client = getFtpClient();
			int index = path.lastIndexOf("/");
			if (index > 0) {
				client.makeDirectory(path.substring(0, index));
			} else if (path.endsWith("/")) {
				client.makeDirectory(path);
			}
		} catch (Exception e) {
			LOG.error("mkdirs {}  failed,异常", path, e);
			throw e;
		}
	}

	

	public void close() {
		FTPClient ftpClient = clientLocal.get();
		try {
			
			if(ftpClient!=null) {
				ftpClient.disconnect();
			}
			clientLocal.remove();
		} catch (Exception e) {
			LOG.error("close error", e);
		}
	}


}
