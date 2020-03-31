package com.taoyuanx.file.client;


import com.taoyuanx.littlefile.client.ClientConfig;
import com.taoyuanx.littlefile.client.FileClient;
import com.taoyuanx.littlefile.client.impl.DefaultFastFileClientFactory;
import org.junit.Before;
import org.junit.Test;

public class TestFileClient {
	public static FileClient client=null;
	@Before
	public void init(){
		DefaultFastFileClientFactory fastFileClientFactory=new DefaultFastFileClientFactory();
		ClientConfig config=new ClientConfig();
		fastFileClientFactory.setConfig(config);
		fastFileClientFactory.init();
		client=fastFileClientFactory.getFileClient();
	}
	
	/**
	 * 测试上传
	 */
	@Test
	public void testUpload(){
		String upload = client.upload("e://123.pdf");
		System.out.println(upload);
	}
	/**
	 * 测试下载
	 */
	@Test
	public void testDownload(){
		client.downLoad("group1/M00/00/12/wKhbyVrDRDGAcOM1ABNRtsA3DK8877.pdf", "e://1234.pdf");
	}
	
	/**
	 * 测试下载
	 */
	@Test
	public void testDelete(){
		System.out.println(client.delete("group1/M00/00/12/wKhbyVrDRDGAcOM1ABNRtsA3DK8877.pdf"));
	}
	
	
	
	/**
	 * 测试下载
	 */
	@Test
	public void testGetFileInfo(){
		System.out.println(client.getFileInfo("group1/M00/00/12/wKhbyVrDRDGAcOM1ABNRtsA3DK8877.pdf"));
	}
	
}
