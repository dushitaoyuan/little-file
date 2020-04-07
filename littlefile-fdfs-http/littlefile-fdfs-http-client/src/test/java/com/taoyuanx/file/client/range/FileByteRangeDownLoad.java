package com.taoyuanx.file.client.range;

import com.taoyuanx.littlefile.client.core.FileChunk;
import com.taoyuanx.littlefile.client.ex.FdfsException;
import com.taoyuanx.littlefile.client.utils.OkHttpUtil;
import com.taoyuanx.littlefile.client.utils.StrUtil;
import com.taoyuanx.littlefile.fdfshttp.core.client.FileClient;
import com.taoyuanx.littlefile.fdfshttp.core.dto.FileInfo;
import org.apache.commons.io.FileUtils;
import sun.nio.ch.FileChannelImpl;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * @author dushitaoyuan
 * @desc 断点下载
 * @date 2019/7/8
 */
public class FileByteRangeDownLoad {
    private String fileId;
    private FileClient client;
    private Long fileSize;

    private OutputStream outputStream;
    private static final Long DEFAULT_CHUNK_SIZE = 4L << 20;
    private static ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(6);

    public FileByteRangeDownLoad(FileClient client, String fileId, OutputStream outputStream) {
        this.client = client;
        this.fileId = fileId;
        this.outputStream = outputStream;
    }

    public FileByteRangeDownLoad(FileClient client, Long fileSize, OutputStream outputStream) {
        this.client = client;
        this.fileSize = fileSize;
        this.outputStream = outputStream;
    }

    public void memoryDownLoad() throws Exception {

        long totalSize = getFileSize();
        Long start = System.currentTimeMillis();
        if (totalSize > 2 * 1024 * 1024) {
            LongAdder count = new LongAdder();
            List<Future<byte[]>> downLoadList = OkHttpUtil.chunkSize(totalSize, DEFAULT_CHUNK_SIZE).stream().map(chunk -> {
                return poolExecutor.submit(new MemoryDownLoad(count, chunk, client, fileId));
            }).collect(Collectors.toList());
            //合并文件
            for (Future<byte[]> bytes : downLoadList) {
                outputStream.write(bytes.get());
            }
            System.out.println("下载完成,文件总大小:" + totalSize + "下载总大小:" + count.longValue());
        } else {
            client.downLoad(fileId, outputStream);
        }
        Long end = System.currentTimeMillis();
        System.out.println("下载耗时:" + (end - start));

    }

    public void diskDownLoad() throws Exception {

        long totalSize = getFileSize();
        Long start = System.currentTimeMillis();
        if (totalSize > 2 * 1024 * 1024) {
            LongAdder count = new LongAdder();
            String chunkDir = "d://chunkDir/" + UUID.randomUUID().toString().replaceAll("-", "");
            File chunkDirFile = new File(chunkDir);
            if (!chunkDirFile.exists()) {
                chunkDirFile.mkdirs();
            }
            List<Future<File>> downLoadList = OkHttpUtil.chunkSize(totalSize, DEFAULT_CHUNK_SIZE).stream().map(chunk -> {
                return poolExecutor.submit(new DiskDownLoad(count, chunk, client, fileId, chunkDir));
            }).collect(Collectors.toList());

            WritableByteChannel mergeFileChannel = Channels.newChannel(outputStream);
            //合并文件
            for (Future<File> diskFile : downLoadList) {
                File file = diskFile.get();
                FileInputStream fileInputStream = new FileInputStream(file);
                FileChannel fileChannel = fileInputStream.getChannel();
                fileChannel.map()
                fileChannel.transferTo(0, fileInputStream.available(), mergeFileChannel);
            }
            Method method = FileChannelImpl.class.getDeclaredMethod("unmap", MappedByteBuffer.class);
            method.setAccessible(true);
            FileUtils.deleteDirectory(chunkDirFile);
            System.out.println("下载完成,文件总大小:" + totalSize + "下载总大小:" + count.longValue());
        } else {
            client.downLoad(fileId, outputStream);
        }
        Long end = System.currentTimeMillis();
        System.out.println("下载耗时:" + (end - start));

    }

    private Long getFileSize() {
        if (Objects.nonNull(fileSize)) {
            return fileSize;
        }
        FileInfo fileInfo = client.getFileInfo(fileId);
        if (Objects.isNull(fileInfo)) {
            throw new FdfsException("文件不存在");
        }
        return fileInfo.getFile_size();
    }

    private static interface ChunkDownLoadMonitor {
        void onSuccess(FileChunk fileChunk);

        void onFailed(FileChunk fileChunk);

    }

    private static abstract class BaseChunkDownLoadTask<T> implements Callable<T>, ChunkDownLoadMonitor {

        protected LongAdder count;
        protected FileChunk chunk;
        protected FileClient fileClient;
        protected String fileId;


        public BaseChunkDownLoadTask(LongAdder count, FileChunk chunk, FileClient fileClient, String fileId) {
            this.count = count;
            this.chunk = chunk;
            this.fileClient = fileClient;
            this.fileId = fileId;
        }

        @Override
        public void onSuccess(FileChunk fileChunk) {
            System.out.println("start:" + fileChunk.getStart() + "\t end:" + fileChunk.getEnd() + "\t" + fileChunk.getLen());
            count.add(fileChunk.getLen());
        }

        @Override
        public T call() throws Exception {
            try {
                T result = this.doCall();
                onSuccess(chunk);
                return result;
            } catch (Exception e) {
                onFailed(chunk);
            }
            return null;

        }

        public abstract T doCall() throws Exception;

        @Override
        public void onFailed(FileChunk fileChunk) {
            throw new FdfsException(StrUtil.log4jFormat("分片[{}]下载失败,开始位置[{}],截止位置[{}]", fileChunk.getChunkIndex(),
                    fileChunk.getStart(),
                    fileChunk.getLen()));
        }

    }

    private static class DiskDownLoad extends BaseChunkDownLoadTask<File> {
        private String chunkDir;
        private File chunkFile;

        public DiskDownLoad(LongAdder count, FileChunk chunk, FileClient fileClient, String fileId, String chunkDir) {
            super(count, chunk, fileClient, fileId);
            this.chunkDir = chunkDir;
            this.chunkFile = new File(chunkDir, chunk.getChunkIndex() + "_chunk");
        }

        @Override
        public File doCall() throws Exception {
            fileClient.downLoadRange(fileId, chunk.getStart(), chunk.getEnd(), new FileOutputStream(chunkFile));
            return chunkFile;
        }

        @Override
        public void onSuccess(FileChunk fileChunk) {
            try {
                super.onSuccess(fileChunk);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onFailed(FileChunk fileChunk) {
            System.out.println("记录日志,后台恢复下载" + StrUtil.log4jFormat("分片[{}]下载失败,开始位置[{}],截止位置[{}]", fileChunk.getChunkIndex(),
                    fileChunk.getStart(),
                    fileChunk.getLen()));
        }
    }

    private static class MemoryDownLoad extends BaseChunkDownLoadTask<byte[]> {
        public MemoryDownLoad(LongAdder count, FileChunk chunk, FileClient fileClient, String fileId) {
            super(count, chunk, fileClient, fileId);
        }

        @Override
        public byte[] doCall() throws Exception {
            return fileClient.downLoadRange(fileId, chunk.getStart(), chunk.getEnd());
        }


    }


}
