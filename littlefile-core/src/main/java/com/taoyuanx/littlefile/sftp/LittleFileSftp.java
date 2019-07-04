package com.taoyuanx.littlefile.sftp;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.taoyuanx.littlefile.util.Utils;

@SuppressWarnings("all")
public class LittleFileSftp {

	private String host, username, password, workDir,privateKey,privateKeyPassword;
	private int port;
	private boolean usePassword=true,isWorkDirNotEmpty=false;
	ThreadLocal<ChannelSftp> clientLocal = new ThreadLocal<ChannelSftp>();
	private JSch jsch;
	/**
	 * 私钥认证
	 * @param host
	 * @param port
	 * @param username
	 * @param privateKey
	 * @param privateKeyPassword
	 * @param workDir
	 */
	public LittleFileSftp(String host, int port, String username,String password,String workDir,String privateKey, String privateKeyPassword) {
		super();
		this.host = host;
		this.port = port;
		this.username = username;
		this.workDir = workDir;
		this.password=password;
		this.isWorkDirNotEmpty=Utils.isNotEmpty(workDir);
		if(Utils.isNotEmpty(privateKey)) {
			this.privateKey=privateKey;
			this.privateKeyPassword=privateKeyPassword;
			usePassword=false;
		}
		jsch = new JSch(); // 创建JSch对象
	}


	public void upload(InputStream input, String dest) throws Exception {
		ChannelSftp channel = getSftpChannel();
		String file = mkdirs(dest, channel);
		channel.put(input, file);
	}

	public void upload(String src, String dest) throws Exception {
		ChannelSftp channel = getSftpChannel();
		String file = mkdirs(dest, channel);
		channel.put(src, file);
	}

	public void download(String src, String dest) throws Exception {
		ChannelSftp channel = getSftpChannel();
		channel.get(src, dest);
	}

	public void download(String src, OutputStream out) throws Exception {
		ChannelSftp channel = getSftpChannel();
		channel.get(src, out);
	}
	public List<String> list(String dir) throws Exception{
		ChannelSftp channel = getSftpChannel();
		Vector<LsEntry> ls = channel.ls(dir);
		Iterator<LsEntry> it = ls.iterator();
		List<String> child=new ArrayList<String>(); 
		while(it.hasNext()){
			LsEntry next = (LsEntry) it.next();
			if(!next.getFilename().equals(".")&&!next.getFilename().equals("..")){//去除无效文件
				child.add(next.getFilename());
			}
		}
		return child;
	}
	
	/**
	 * 递归创建目录
	 * 
	 * @param filePath
	 * @param channel
	 * @return
	 * @throws Exception
	 */
	private String mkdirs(String filePath, ChannelSftp channel) throws Exception {
		if(filePath.indexOf("/")>-1) {
			String dirs[] = filePath.split("/");
			int i = 0;
			for (i = 0; i < dirs.length - 1; i++) {
				try {
					channel.cd(dirs[i]);
				} catch (Exception e) {
					channel.mkdir(dirs[i]);
					channel.cd(dirs[i]);
				}
			}
			return dirs[i];
		}
		return filePath;
		
	}

	/**
	 * 关闭channel
	 * 
	 * @param channel
	 * @throws Exception
	 */
	public void closeChannel() throws Exception {
		ChannelSftp channel = clientLocal.get();
		
		if (channel != null) {
			channel.disconnect();
			if (channel.getSession() != null) {
				channel.getSession().disconnect();
			}
		}
	}
	
	/**
	 * 打开stfp 通道
	 * 
	 * @return sftp通道
	 * @throws Exception
	 */
	private ChannelSftp getSftpChannel() throws Exception {
		ChannelSftp local = clientLocal.get();
		if(local==null) {
			Session session = jsch.getSession(username, host, port);
			if(usePassword) {
				session.setPassword(password);
			}else {
				jsch.addIdentity(privateKey, privateKeyPassword);
			}
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setTimeout(3000);
			session.setConfig(config);
			session.connect();
			ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
			channel.connect();
			if(isWorkDirNotEmpty) {
				channel.cd(workDir);
			}
			clientLocal.set(channel);
			return  channel;
		}else {
			return local;
		}
		
	}


}
