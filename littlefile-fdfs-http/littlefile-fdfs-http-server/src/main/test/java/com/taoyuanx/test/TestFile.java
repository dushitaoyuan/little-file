package com.taoyuanx.test;

import com.taoyuanx.littlefile.server.dto.ImageWH;
import com.taoyuanx.littlefile.server.utils.FdfsFileUtil;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TestFile {
    @Test
    public void test() {
        String cutSize = "100*100";
        List<ImageWH> collect = Arrays.stream(cutSize.split(",")).map(singleSize -> {
            try {
                String size[] = singleSize.split("x");
                if (size.length != 2) {
                    return null;
                }
                int width = Integer.parseInt(size[0]);
                int height = Integer.parseInt(size[1]);
                return new ImageWH(width, height);
            } catch (Exception e) {
                System.out.println(singleSize);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        System.out.println(collect);
    }

    FdfsFileUtil fdfsFileUtil = new FdfsFileUtil();

    @Test
    public void poolTest() throws InterruptedException {
        ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

        int limit = 10000;
        for (int i = 0; i < limit; i++) {
            final int finalI = i;
            poolExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        String fileId = fdfsFileUtil.upload(null, new ByteArrayInputStream("123456".getBytes()), UUID.randomUUID().toString() + ".txt");
                        System.out.println(String.format("%s %s", finalI, fileId));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        poolExecutor.shutdown();
        while (!poolExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
            Thread.sleep(3000);
        }
        System.out.println("finsh");
        Thread.sleep(5000000);
    }
}
