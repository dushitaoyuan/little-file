package com.taoyuanx.file;

import com.taoyuanx.littlefile.combine.core.dto.FileSignDTO;
import com.taoyuanx.littlefile.combine.core.sign.FileTypeEnum;
import com.taoyuanx.littlefile.combine.core.sign.SimpleFileManager;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author dushitaoyuan
 * @desc 文件服务测试
 * @date 2020/2/20
 */
public class FileSignTest {
    private SimpleFileManager simpleFileManager = null;

    @Before
    public void before() {
        simpleFileManager = new SimpleFileManager("123456", "https://localhost:9995/file=%s");
    }

    @Test
    public void fileMockTest() {
        FileSignDTO fileDTO=simpleFileManager.newFileSignDTO("oss://1.png", FileTypeEnum.SYETEM);
        //永久签名url
        String signUrl = simpleFileManager.signFile(fileDTO);
        System.out.println(signUrl);
        System.out.println(simpleFileManager.parseFile(signUrl));
        System.out.println(simpleFileManager.verify(signUrl));


        //时效性签名url
        String signUrlWithExpire = simpleFileManager.signFile(fileDTO,5L, TimeUnit.MINUTES);
        System.out.println(signUrlWithExpire);

        System.out.println(simpleFileManager.parseFile(signUrlWithExpire));
        System.out.println(simpleFileManager.verify(signUrlWithExpire));
        System.out.println(simpleFileManager.getFullHttpUrl(fileDTO));
    }
}
