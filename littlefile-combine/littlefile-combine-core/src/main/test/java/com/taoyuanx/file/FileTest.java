package com.taoyuanx.file;


import com.taoyuanx.littlefile.combine.core.sign.FileTypeEnum;
import com.taoyuanx.littlefile.combine.core.sign.SimpleFileManager;
import com.taoyuanx.littlefile.combine.core.store.FileStoreService;
import com.taoyuanx.littlefile.combine.core.store.ftp.FtpFileService;
import com.taoyuanx.littlefile.combine.core.store.sftp.SftpFileService;
import com.taoyuanx.littlefile.combine.core.utils.Utils;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;

/**
 * @author dushitaoyuan
 * @desc 文件测试
 * @date 2020/2/20
 */
public class FileTest {
    FileStoreService sftpStoreService;
    FileStoreService ftpStoreService;
    SimpleFileManager simpleFileManager = null;

    @Before
    public void before() {
        sftpStoreService = new SftpFileService("192.168.3.100", 22, "root", "root", "/home");
        ftpStoreService = new FtpFileService("192.168.3.100", 21, "ftp", "ftp", "");

        simpleFileManager = new SimpleFileManager("123456", "https://localhost:9995/file");

    }

    @Test
    public void sftpFileTest() throws Exception {
        String store = sftpStoreService.store(new ByteArrayInputStream("1234".getBytes()), "test.txt");
        System.out.println(store);
        System.out.println(simpleFileManager.signFile(simpleFileManager.newFileSignDTO(store, FileTypeEnum.SYETEM)));
        sftpStoreService.downLoad(store, new FileOutputStream("d://test.txt"));
        sftpStoreService.delete(store);

    }

    @Test
    public void ftpFileTest() throws Exception {
        String store = ftpStoreService.store(new ByteArrayInputStream("1234".getBytes()), "test.txt");
        System.out.println(store);
        System.out.println(simpleFileManager.signFile(simpleFileManager.newFileSignDTO(store, FileTypeEnum.SYETEM)));
        ftpStoreService.downLoad(store, new FileOutputStream("d://test.txt"));
        //ftpStoreService.delete(store);

    }

    @Test
    public void fileProtocolTest() throws Exception {
        System.out.println(Utils.getStoreProtocol("sftp://11.txt"));


    }
}
