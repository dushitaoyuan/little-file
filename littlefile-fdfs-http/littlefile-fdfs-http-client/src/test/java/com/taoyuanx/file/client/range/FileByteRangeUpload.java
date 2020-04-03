package com.taoyuanx.file.client.range;

import com.taoyuanx.littlefile.client.core.FileChunk;
import com.taoyuanx.littlefile.client.ex.FdfsException;
import com.taoyuanx.littlefile.client.utils.OkHttpUtil;
import com.taoyuanx.littlefile.fdfshttp.core.client.FileClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class FileByteRangeUpload {
    private FileClient client;
    private Long fileSize;

    private Long chunkSize;
    private FileChannel channel;

    private String fileName;


    private static final Long DEFAULT_CHUNK_SIZE = 4L << 20;

    public FileByteRangeUpload(FileClient client, FileChannel channel, Long fileSize, String fileName) {
        this(client, channel, DEFAULT_CHUNK_SIZE, fileSize, fileName);
    }

    public FileByteRangeUpload(FileClient client, FileChannel channel, Long chunkSize, Long fileSize, String fileName) {
        this.client = client;
        this.channel = channel;
        this.chunkSize = chunkSize;
        this.fileSize = fileSize;
        this.fileName = fileName;
    }

    public String uploadChunk() throws Exception {
        List<FileChunk> chunkList = OkHttpUtil.chunkSize(fileSize, chunkSize);
        if (Objects.isNull(chunkList) && chunkList.isEmpty()) {
            throw new FdfsException("文件异常");
        }
        Long sum = chunkList.stream().collect(Collectors.summingLong(chunk -> chunk.getLen()));
        System.out.println("总大小:" + sum);
        FileChunk chunk = chunkList.get(0);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OkHttpUtil.transferTo(channel, chunk.getStart(), chunk.getEnd(), output);
        String fileId = this.client.uploadAppendFile(new ByteArrayInputStream(output.toByteArray()), fileName);
        for (int index = 1, len = chunkList.size(); index < len; index++) {
            output = new ByteArrayOutputStream();
            chunk = chunkList.get(index);
            OkHttpUtil.transferTo(channel, chunk.getStart(), chunk.getEnd(), output);
            byte[] bytes = output.toByteArray();
            System.out.println("calc len" + chunk.getLen() + " real " + bytes.length);
            this.client.appendFile(new ByteArrayInputStream(bytes), fileName, fileId);
        }
        return fileId;
    }


}
