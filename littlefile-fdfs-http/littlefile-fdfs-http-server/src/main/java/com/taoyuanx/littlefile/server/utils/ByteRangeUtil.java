package com.taoyuanx.littlefile.server.utils;

import com.taoyuanx.littlefile.server.fdfs.FdfsUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * @author dushitaoyuan
 * @desc 断点下载工具
 * @date 2020/4/1
 */
public class ByteRangeUtil {

    /*public void handleByteRange(HttpServletRequest req, HttpServletResponse resp, File localFile, boolean isGzip) throws Exception {
        String range = req.getHeader("Range");
        String name = localFile.getName();
        long fileSize = localFile.length();
        resp.addHeader("ETag", String.valueOf(localFile.lastModified()));
        // resp.addHeader("Last-Modified",new Date(localFile.lastModified()).toGMTString());
        if (Utils.isEmpty(range)) {
            resp.setContentType(req.getServletContext().getMimeType(name));
            resp.setHeader("Content-Length", String.valueOf(fileSize));
            //断点下载支持
            resp.setHeader("Accept-Ranges", "bytes");
            return;
        }
        resp.setHeader("Accept-Ranges", "bytes");
        Long[] byteRange = resolveByteRange(range, fileSize);
        Long start = byteRange[0], endSize = byteRange[1], count = endSize - start + 1;
        //range格式非法
        if (start > fileSize || endSize > fileSize || start > endSize) {
            resp.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
        }
        resp.setHeader("Content-type", "application/octet-stream");
        resp.setHeader("Content-Disposition",
                "attachment;fileName=" + URLEncoder.encode(FdfsUtil.getFileName(name), "UTF-8"));
        //格式 bytes %s-%s/%s
        resp.addHeader(" Content-Range", String.format(CONTENTRANGE_FMT, start, endSize, fileSize));
        RandomAccessFile randomAccessFile = new RandomAccessFile(localFile, "r");
        FileChannel channel = randomAccessFile.getChannel();
        OutputStream outputStream = resp.getOutputStream();
        if (isGzip) {
            resp.setHeader("Content-Encoding", "gzip");
            outputStream = new GZIPOutputStream(resp.getOutputStream());
        }
        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        channel.transferTo(start, count, Channels.newChannel(outputStream));
        outputStream.close();
        channel.close();
    }*/

    public static Long[] resolveByteRange(String range, Long fileSize) {
        /**
         * Range header 格式:bytes=
         * 1. 500-1000：指定开始和结束的范围，一般用于多线程下载。
         * 2. 500- ：指定开始区间，一直传递到结束。这个就比较适用于断点续传、或者在线播放等等。
         * 3. -500：无开始区间，只意思是需要最后 500 bytes 的内容实体。
         * 4. 100-300,1000-3000：指定多个范围，这种方式使用的场景很少，了解一下就好了
         */
        Long[] result = new Long[2];
        range = range.replaceFirst("bytes=", "");
        if (range.contains(",")) {
            //暂不支持
        } else {
            String[] split = range.split("-");
            if (RANGE_FMT_START_END.matcher(range).matches()) {
                result[0] = Long.parseLong(split[0]);
                result[1] = Long.parseLong(split[1]);
                return result;
            }
            if (RANGE_FMT_START_TILL_END.matcher(range).matches()) {
                result[0] = Long.parseLong(split[0]);
                result[1] = fileSize;
                return result;
            }
            if (RANGE_FMT_BYTES_END.matcher(range).matches()) {
                result[0] = fileSize - Long.parseLong(split[0]);
                result[1] = fileSize - 1;
                return result;
            }

        }
        return null;
    }

    public static Pattern RANGE_FMT_START_END = Pattern.compile("^[0-9]{1,}-[0-9]{1,}$");
    public static Pattern RANGE_FMT_START_TILL_END = Pattern.compile("^[0-9]{1,}-$");
    public static Pattern RANGE_FMT_BYTES_END = Pattern.compile("^-[0-9]{1,}$");
    public static String CONTENTRANGE_FMT = "bytes %s-%s/%s";
}
