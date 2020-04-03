package com.taoyuanx.file.client;


import com.taoyuanx.littlefile.client.core.ClientConfig;
import com.taoyuanx.littlefile.client.core.FastFileClientFactory;
import com.taoyuanx.littlefile.client.impl.DefaultSingletonFastFileClientFactory;
import com.taoyuanx.littlefile.fdfshttp.core.client.FileClient;
import com.taoyuanx.littlefile.fdfshttp.core.dto.FileInfo;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class TestFileClient {
    public static FileClient client = null;

    @Before
    public void init() {
        FastFileClientFactory fastFileClientFactory = new DefaultSingletonFastFileClientFactory();
        client = fastFileClientFactory.getFileClient();
    }

    /**
     * 测试上传
     */
    @Test
    public void testUpload() {
        String upload = client.upload("g://111.png");
        System.out.println(upload);
    }

    /**
     * 测试下载
     */
    @Test
    public void testDownload() {
        client.downLoad("group1/M00/00/00/wKgD0l6E2GeAFrLRAAB5ok9XEwo822.png", "g://download.png");
    }

    /**
     * 测试下载
     */
    @Test
    public void testDelete() {

        client.delete("group1/M00/00/12/wKhbyVrDRDGAcOM1ABNRtsA3DK8877.pdf");
    }


    /**
     * 测试info
     */
    @Test
    public void testGetFileInfo() {
        System.out.println(client.getFileInfo("group1/M00/00/00/wKgD0l6E2GeAFrLRAAB5ok9XEwo822.png"));
    }

    /**
     * 测试info
     */
    @Test
    public void testDownLoadRannge() throws Exception {
        String uploadFile = "d://test.exe";
        String upload = client.upload(uploadFile);
        String fileId = upload;
        System.out.println(fileId);
        // String fileId="group1/M00/00/00/wKgeyF6EoVGAHKgoAMabiRD7QQE4425.7z";
        FileByteRangeDownLoad fileByteRangeDownLoad = new FileByteRangeDownLoad(client, fileId, new FileOutputStream("d://down.exe"));
        fileByteRangeDownLoad.downLoad();
    }

    @Test
    public void testUploadRannge() throws Exception {
        String uploadFile = "d://test.exe";
        RandomAccessFile randomAccessFile=new RandomAccessFile(uploadFile,"r");
        FileChannel channel = randomAccessFile.getChannel();
        FileByteRangeUpload fileByteRangeUpload = new FileByteRangeUpload(client, channel,        channel.size()
                , "test.exe");
        System.out.println(fileByteRangeUpload.uploadChunk());
    }
}