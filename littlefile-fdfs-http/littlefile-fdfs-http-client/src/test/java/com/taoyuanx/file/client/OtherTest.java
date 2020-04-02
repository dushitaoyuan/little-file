package com.taoyuanx.file.client;

import org.junit.Test;

import java.io.FileInputStream;

/**
 * @author dushitaoyuan
 * @date 2020/4/2
 */
public class OtherTest {
    @Test
    public void inputTest() throws Exception {
        System.out.println(new FileInputStream("d://test.exe").available());
        String temp,str;
        System.out.println((temp=str="123"));
        System.out.println(temp);
        System.out.println(str);


    }
}
