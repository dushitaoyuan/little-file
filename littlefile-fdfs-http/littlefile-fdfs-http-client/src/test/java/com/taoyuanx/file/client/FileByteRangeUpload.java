package com.taoyuanx.file.client;

import com.taoyuanx.littlefile.client.core.ByteRange;
import com.taoyuanx.littlefile.client.core.FileChunk;
import com.taoyuanx.littlefile.client.ex.FdfsException;
import com.taoyuanx.littlefile.client.utils.OkHttpUtil;
import com.taoyuanx.littlefile.fdfshttp.core.client.FileClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


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
        FileChunk chunk = chunkList.get(0);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OkHttpUtil.transferTo(channel, chunk.getStrat(), chunk.getEnd(), output);
        String fileId = this.client.uploadAppendFile(new ByteArrayInputStream(output.toByteArray()), fileName);
        for (int index = 1, len = chunkList.size(); index < len; index++) {
            output.reset();
            OkHttpUtil.transferTo(channel, chunk.getStrat(), chunk.getEnd(), output);
            this.client.appendFile(new ByteArrayInputStream(output.toByteArray()), fileName, chunk.getStrat(), fileId);
        }
        return fileId;
    }


}
