package com.taoyuanx.file;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author dushitaoyuan
 * @desc 文件读取速度测试
 * @date 2019/7/8
 */
public class FileSpeedTest {

    /**
     * 测试结论 FileChannel 较快,理论上 MappedByteBuffer 较快,不知道为啥我测出相反的结论
     * 另外也对比了MappedByteBuffer的使用坑点(关闭bug,超大文件),所以决定使用filechannel
     *
     */
    @Test
    public void fileChannelTest() throws Exception {
        Long start=System.currentTimeMillis();
        RandomAccessFile src=new RandomAccessFile(new File("d://file/300m.zip"),"r");
        FileChannel in = src.getChannel();
        ByteBuffer buffer=ByteBuffer.allocate(4*1024*1024);
        OutputStream fileOutputStream=new FileOutputStream("d://file/temp_fileChannel.zip");
        int len=0;
       while ((len=in.read(buffer))>0){
           buffer.flip();
           fileOutputStream.write(buffer.array(),0,len);
           buffer.clear();
       }
        Long end=System.currentTimeMillis();
        System.out.println("FileChannel耗时:"+(end-start));
    }

    @Test
    public void  mappedByteBufferTest() throws Exception {
        Long start=System.currentTimeMillis();
        RandomAccessFile src=new RandomAccessFile(new File("d://file/300m.zip"),"r");
        MappedByteBuffer map = src.getChannel().map(FileChannel.MapMode.READ_ONLY,0, src.length());
        OutputStream fileOutputStream=new FileOutputStream("d://file/temp_mapped.zip");
        byte[] buf=null;
        while ((buf=mapRead(map,4*1024*1024))!=null){
            fileOutputStream.write(buf);
        }
        Long end=System.currentTimeMillis();
        System.out.println("MappedByteBuffer耗时:"+(end-start));
    }
    public byte[] mapRead(MappedByteBuffer map,int size){
        int limit = map.limit();
        int position = map.position();
        if (position == limit) {
            return null;
        }
        if (limit - position > size) {
            byte[] array = new byte[size];
            map.get(array);
            return array;
        } else {// 最后一次读取数据
            byte[] array = new byte[limit - position];
            map.get(array);
            return array;
        }
    }
}
