package com.taoyuanx.littlefile.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class Utils {
	public static boolean isEmpty(String str){
		if(null==str||str.length()==0){
			return true;
		}
		return false;
	}
	public static boolean isNotEmpty(String str){
		if(null!=str&&str.trim().length()>0){
			return true;
		}
		return false;
	}
	
	
	
	public static final String CERT_TYPE_P12 = "PKCS12";
	public static final String CERT_TYPE_JKS = "JKS";
	/**
	 *  判断文件是否完整
	 * @param localFile
	 * @return
	 */
	public static boolean isFileNotBad(File localFile) {
		boolean isFileNotBad = localFile.exists() && localFile.length() > 1024;
		return isFileNotBad;
	}
	
	public static KeyManager getKeyManager(String path,String password) throws Exception {
		if(isEmpty(path)) {
			return null;
		}
		String type=CERT_TYPE_P12;
		if(path.toUpperCase().endsWith(CERT_TYPE_JKS)) {
			type=CERT_TYPE_JKS;
		}
		KeyStore keyStore = KeyStore.getInstance(type);
		keyStore.load(loadFile(path), password.toCharArray());
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, password.toCharArray());
		KeyManager[] km = kmf.getKeyManagers();
		return km[0];
	}
	
	public  static TrustManager getTrustManager(String path,String password) throws Exception
	{
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(loadFile(path), password.toCharArray());
		TrustManagerFactory tf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tf.init(keyStore);
		TrustManager[] tm = tf.getTrustManagers();
		return tm[0];
	}
	public static final String classpathFlag="classpath:";
	public static InputStream loadFile(String filePath) throws Exception {
		if(filePath.startsWith(classpathFlag)) {
			return Utils.class.getClassLoader().getResourceAsStream(filePath.replace(classpathFlag, ""));
		}else {
			return new FileInputStream(filePath);
		}
	}
	public static void main(String[] args) throws Exception {
		String path="e://ftp/client.p12";
				//"classpath:client.p12";
		String password="123456";
		System.out.println(getKeyManager(path, password));
		System.out.println(getTrustManager(path, password));
		
	}
}
