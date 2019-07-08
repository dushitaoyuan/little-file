package com.taoyuanx.littlefile.sample;

import lombok.AllArgsConstructor;
import lombok.Data;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author dushitaoyuan
 * @desc 断点下载
 * @date 2019/7/8
 */
public class FileByteRangeDownLoad {
    OkHttpClient client = null;
    @Before
    public  void before(){
        client=new OkHttpClient().newBuilder().connectTimeout(50, TimeUnit.SECONDS).readTimeout(200,TimeUnit.SECONDS).build();
    }
    @Test
    public  void byteRangeDownLoadTest() throws Exception {
        String url="http://localhost:8082/file?s=eyJ0IjoiMiIsImYiOiJ0ZW1wLnppcCIsImVuZCI6MTU2MjU4NjA3OTE0NX0.aBMUCKuvD6uzB6E6n1gvtQ";
        new ThreadDownFile(url,client,"d://temp.zip").threadDownLoad();
    }



    public class  ThreadDownFile {
        private  String url;
        private  OkHttpClient okHttpClient;
        private  String file;

        public ThreadDownFile(String url, OkHttpClient okHttpClient, String file) {
            this.url = url;
            this.okHttpClient = okHttpClient;
            this.file = file;
        }

        public  void threadDownLoad(){
            Response execute=null;
            try{
                final Long firstSize=4*1024*1024L;
                Request request = new Request.Builder().header("Range", "bytes=" + 0 + "-" + firstSize)
                        .url(url)
                        .build();

                execute = okHttpClient.newCall(request).execute();
                String header = execute.header("Content-Length");
                final long fileSize = Long.parseLong(header);
                final File destFile=new File(file);;
                if(fileSize<firstSize){
                    write(execute.body().byteStream(),destFile,0L,fileSize);
                    return;
                }else{
                    final int threadNums=3;
                    ThreadPoolExecutor poolExecutor= (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNums);
                    for(int i = 0; i<threadNums; i++){
                        final Long blockSize=fileSize-firstSize / threadNums;
                        final int finalI = i;
                        poolExecutor.submit(new Runnable() {
                            @Override
                            public void run() {
                                Long start=System.currentTimeMillis();
                                long startIndex = firstSize+finalI * blockSize; // 线程开始下载的位置
                                long endIndex = (finalI + 1) * blockSize - 1+firstSize; // 线程结束下载的位置
                                if (finalI == (threadNums - 1)) { // 如果是最后一个线程,将剩下的文件全部交给这个线程完成
                                    endIndex = fileSize - 1;
                                }
                                downLoad(startIndex,endIndex,destFile);
                                Long end=System.currentTimeMillis();
                                System.out.println("下载耗时:"+(end-start)+"起始位置:"+startIndex);
                            }
                        });
                    }
                    while (poolExecutor.getActiveCount()>0){
                        Thread.sleep(1000L);
                    }
                    poolExecutor.shutdownNow();
                    System.out.println("下载完成,文件大小:"+countFileSize);
                }
            }catch (Exception e){
                e.printStackTrace();
                System.out.println(e);
            }finally {
                if(execute!=null){
                    execute.close();
                }
            }
        }

        private   void downLoad(Long start,Long end,File file)  {
            Response execute=null;
            try{
                Request request = new Request.Builder().header("Range", "bytes=" + start + "-" + end)
                        .url(url)
                        .build();
                execute = okHttpClient.newCall(request).execute();
                ResponseBody body = execute.body();

                InputStream inputStream = body.byteStream();
                write(inputStream,file,start, (end-start+1));
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(execute!=null){
                    execute.close();
                }
            }
        }
        private  Long countFileSize=0L;
        private  void write(InputStream inputStream,File file,Long start,Long len) throws Exception {
            System.out.println("len "+len);
            RandomAccessFile tmpAccessFile = new RandomAccessFile(file, "rw");// 获取前面已创建的文件.
            tmpAccessFile.seek(start);// 文件写入的开始位置.
            tmpAccessFile.getChannel().transferFrom(Channels.newChannel(inputStream),0,len);
            inputStream.close();
            tmpAccessFile.close();
            countFileSize+=len;
        }

    }
}
