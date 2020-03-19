package com.taoyuanx.littlefile.sample;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.RealBufferedSink;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author dushitaoyuan
 * @desc 断点下载
 * @date 2019/7/8
 */
public class FileByteRangeDownLoad {
    OkHttpClient client = null;
    FileAuthedUrlCreate fileAuthedUrlCreate=null;
    @Before
    public void before() {
         fileAuthedUrlCreate=new FileAuthedUrlCreate();

        client = new OkHttpClient().newBuilder().connectTimeout(50, TimeUnit.SECONDS).readTimeout(200, TimeUnit.SECONDS).build();
    }

    @Test
    public void byteRangeDownLoadTest() throws Exception {
        String fileUrl = "test.exe";
        String handleType = "2";
        String signUrl = fileAuthedUrlCreate.getSFileHandler().signFileUrl(fileUrl, handleType);
        new ThreadDownFile(signUrl, client, "d:/"+fileUrl).threadDownLoad();
    }


    public class ThreadDownFile {
        private String url;
        private OkHttpClient okHttpClient;
        private String file;

        public ThreadDownFile(String url, OkHttpClient okHttpClient, String file) {
            this.url = url;
            this.okHttpClient = okHttpClient;
            this.file = file;
        }

        public void threadDownLoad() {
            Response execute = null;
            try {
                Request request = new Request.Builder().url(url).get().build();
                execute = okHttpClient.newCall(request).execute();
                final Long totalSize = Long.parseLong(execute.header("Content-Length"));
                final File destFile = new File(file);
                if (totalSize > 4 * 1024 * 1024) {
                    final int threadNums = 3;
                    ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNums);
                    final Long blockSize = totalSize / threadNums;
                    List<Future<RangeResponse>> list = Lists.newArrayList();
                    for (int i = 0; i < threadNums; i++) {
                        final int tempI = i;
                        Future<RangeResponse> submit = poolExecutor.submit(new Callable<RangeResponse>() {

                            @Override
                            public RangeResponse call() throws Exception {
                                long startIndex = tempI * blockSize; // 线程开始下载的位置
                                long endIndex = (tempI + 1) * blockSize - 1; // 线程结束下载的位置
                                if (tempI == (threadNums - 1)) { // 如果是最后一个线程,将剩下的文件全部交给这个线程完成
                                    endIndex = totalSize - 1;
                                }
                                Response response = downLoad(startIndex, endIndex, destFile);
                                return new RangeResponse(startIndex, response,(endIndex-startIndex)+1);

                            }
                        });
                        list.add(submit);
                    }
                    for (Future<RangeResponse> t : list) {
                        RangeResponse rangeResponse = t.get();
                        System.out.println("起始位置:" + rangeResponse.getStart());
                        write(rangeResponse.getResponse().body().byteStream(), destFile, rangeResponse.getStart(),rangeResponse.getCount());
                        rangeResponse.getResponse().close();
                    }

                    poolExecutor.shutdownNow();
                    System.out.println("下载完成,文件大小:" + countFileSize);
                } else {
                    Long start = System.currentTimeMillis();
                    downLoad(0L, totalSize, destFile);
                    Long end = System.currentTimeMillis();
                    System.out.println("下载耗时:" + (end - start) + "起始位置:" + 0);
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
            } finally {
                if (execute != null) {
                    execute.close();
                }
            }
        }

        private Response downLoad(Long start, Long end, File file) {
            Response execute = null;
            try {
                Request request = new Request.Builder().header("Range", "bytes=" + start + "-" + end)
                        .url(url)
                        .build();
                execute = okHttpClient.newCall(request).execute();
                if(execute.isSuccessful()){
                    return execute;
                }
                throw new RuntimeException("下载异常, response-->"+execute.body().string());
            } catch (Exception e) {
                throw new  RuntimeException(e);
            }
        }

        private AtomicLong countFileSize = new AtomicLong(0);

        private void write(InputStream inputStream, File file, Long start,Long count) throws Exception {

            RandomAccessFile tmpAccessFile = new RandomAccessFile(file, "rw");// 获取前面已创建的文件.
            tmpAccessFile.seek(start);// 文件写入的开始位置.
            byte[] buf = new byte[4 * 1024 * 1024];
            int rLen = 0;
            while ((rLen = inputStream.read(buf)) > 0) {
                tmpAccessFile.write(buf, 0, rLen);
                countFileSize.addAndGet(rLen);
            }
            countFileSize.addAndGet(count);
            inputStream.close();
            tmpAccessFile.close();

        }

    }

}
