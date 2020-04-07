package com.taoyuanx.file.client;

import com.taoyuanx.littlefile.client.core.FastFileClientFactory;
import com.taoyuanx.littlefile.client.impl.DefaultSingletonFastFileClientFactory;
import com.taoyuanx.littlefile.fdfshttp.core.client.FileClient;
import org.junit.Before;
import org.junit.Test;

/**
 * @author dushitaoyuan
 * @date 2020/4/512:27
 */
public class BaseUrlTest {
    public static FileClient client = null;

    @Before
    public void init() {
        FastFileClientFactory fastFileClientFactory = new DefaultSingletonFastFileClientFactory();
        client = fastFileClientFactory.fileClient();
    }


    /**
     * 测试上传
     */
    @Test
    public void testUpload() {
        String upload = client.upload("g://11.jpg");
        System.out.println(upload);
    }
    /**
     * 测试info
     */
    @Test
    public void testGetFileInfo() {
        for (int i=0;i<100;i++){
            try{
                Thread.sleep(1000L);
                System.out.println(client.getFileInfo("group1/M00/00/01/wKgeyF6JatuARd72AACS8A7FtWk434.jpg"));
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

}
