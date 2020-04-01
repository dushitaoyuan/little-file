package com.taoyuanx.littlefile.web;

import com.taoyuanx.littlefile.fdfs.FdfsUtil;
import com.taoyuanx.littlefile.support.FileDownStrategy;
import com.taoyuanx.littlefile.util.Utils;
import com.taoyuanx.littlefile.web.security.AbstractSimpleTokenManager;
import com.taoyuanx.littlefile.web.security.TokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * @author dushitaoyuan
 * @desc 文件处理类
 * @date 2019/7/3 17:31
 */
public class FileHandler {

    /**
     * 参数解释
     * cacheDir 本地文件缓存目录
     * fileDownStrategy 文件下载策略
     * isGzip 是否开启gzip压缩
     * tokenManager token生成器
     * tokenExpire token过期时间
     * urlFmt 授权url模板 如:http://localhost:8080/down?token=%s
     */
    //文件处理类型: 0下载,1查看 2断点续传
    public static final String DOWN = "0",
            LOOK = "1",
            BYTE_RANGE_DOWN = "2";
    public static Logger LOG = LoggerFactory.getLogger(FileHandler.class);
    private String cacheDir;
    private FileDownStrategy fileDownStrategy;
    private boolean isGzip = false;
    private AbstractSimpleTokenManager tokenManager;
    private Long tokenExpire;
    private String urlFmt;
    private boolean tokenOpen = true;
    private Integer buffSize = 1024 * 1024 * 4;

    public FileHandler(String cacheDir, FileDownStrategy fileDownStrategy,
                       boolean isGzip, AbstractSimpleTokenManager tokenManager, Long tokenExpire, String urlFmt) {
        super();
        this.cacheDir = cacheDir;
        this.fileDownStrategy = fileDownStrategy;
        this.isGzip = isGzip;
        this.tokenManager = tokenManager;
        this.tokenExpire = tokenExpire;
        this.urlFmt = urlFmt + "?s=%s";
    }

    public FileHandler(String cacheDir, FileDownStrategy fileDownStrategy,
                       boolean isGzip, String urlFmt) {
        super();
        this.cacheDir = cacheDir;
        this.fileDownStrategy = fileDownStrategy;
        this.isGzip = isGzip;
        tokenOpen = false;
        this.urlFmt = urlFmt + "?f=%s&t=%s";
    }

    /**
     * 文件处理
     *
     * @param resp
     * @param req
     */
    public void handleFile(HttpServletResponse resp, HttpServletRequest req) {
        String type = null, filePath = null;
        try {
            if (tokenOpen) {
                String token = req.getParameter(Constant.REQUEST_PARAM_TOKEN_KEY);
                if (Utils.isEmpty(token)) {
                    throw new TokenException("operation not allowed");
                }
                Map<String, Object> signData = tokenManager.vafy(token);
                type = (String) signData.get(Constant.REQUEST_PARAM_TYPE_KEY);
                filePath = (String) signData.get(Constant.REQUEST_PARAM_FILE_KEY);
            } else {
                type = req.getParameter(Constant.REQUEST_PARAM_TYPE_KEY);
                filePath = req.getParameter(Constant.REQUEST_PARAM_FILE_KEY);
            }
            File absoluteFile = new File(cacheDir, filePath);
            //文件不存在或损坏,下载
            if (!Utils.isFileNotBad(absoluteFile)) {
                //父级目录不存在,创建
                File parentFile = absoluteFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                String dest = absoluteFile.getAbsolutePath();
                LOG.debug("download file:[{}] local path:[{}]", filePath, dest);
                fileDownStrategy.down(filePath, dest);
            }
            switch (type) {
                case LOOK: {// 查看
                    resp.setContentType(req.getServletContext().getMimeType(absoluteFile.getName()));
                }
                break;
                case DOWN: {// 下载
                    resp.setContentType(req.getServletContext().getMimeType(absoluteFile.getName()));
                    resp.setHeader("Content-type", "application/octet-stream");
                    resp.setHeader("Content-Disposition",
                            "attachment;fileName=" + URLEncoder.encode(FdfsUtil.getFileName(filePath), "UTF-8"));
                    resp.setContentType(req.getServletContext().getMimeType(absoluteFile.getName()));

                }
                break;
                case BYTE_RANGE_DOWN: {
                    handleByteRange(req, resp, absoluteFile, isGzip);
                }
                return;
            }
            //gzip 压缩
            if (isGzip) {
                resp.setHeader("Content-Encoding", "gzip");
                GZIPOutputStream gzip = new GZIPOutputStream(resp.getOutputStream());
                handle(gzip, absoluteFile);
                gzip.close();
            } else {
                ServletOutputStream out = resp.getOutputStream();
                handle(out, absoluteFile);
                out.flush();
            }
        } catch (Exception e) {
            handleError(resp, req, e, filePath);
        }

    }

    /**
     * 文件签名url构造接口
     *
     * @param fileUrl    文件路径
     * @param handleType 文件处理类型 0下载 1查看
     * @return
     */
    public String signFileUrl(String fileUrl, String handleType) {
        Map<String, Object> signMap = new HashMap<>();
        signMap.put(Constant.REQUEST_PARAM_FILE_KEY, fileUrl);
        signMap.put(Constant.REQUEST_PARAM_TYPE_KEY, handleType);
        return String.format(urlFmt, tokenManager.create(signMap, tokenExpire, TimeUnit.MINUTES));
    }

    /**
     * 文件签名url构造接口
     *
     * @param fileUrl    文件路径
     * @param handleType 文件处理类型 0下载 1查看
     * @return
     */
    public String signFileUrl(String fileUrl, String handleType, Long expire, TimeUnit timeUnit) {
        Map<String, Object> signMap = new HashMap<>();
        signMap.put(Constant.REQUEST_PARAM_FILE_KEY, fileUrl);
        signMap.put(Constant.REQUEST_PARAM_TYPE_KEY, handleType);
        return String.format(urlFmt, tokenManager.create(signMap, expire, timeUnit));
    }

    /**
     * 文件url构造接口
     *
     * @param fileUrl    文件路径
     * @param handleType 文件处理类型 0下载 1查看
     * @return
     */
    public String createPublicUrl(String fileUrl, String handleType) {
        return String.format(urlFmt, fileUrl, handleType);
    }


    private void handle(OutputStream out, File localFile) throws Exception {
        FileChannel channel = new FileInputStream(localFile).getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(buffSize);
        int len = 0;
        while ((len = channel.read(buffer)) > 0) {
            buffer.flip();
            out.write(buffer.array(), 0, len);
            buffer.clear();
        }
        channel.close();

    }

    private void handleError(HttpServletResponse resp, HttpServletRequest req, Exception e, String filePath) {
        try {
            LOG.error("处理文件[{}]异常{}", filePath, e);
            if (e instanceof TokenException) {
                resp.getWriter().println("operation not allowed,url Unauthorized or url expired");
                return;
            }
            resp.getWriter().println("file " + filePath + " error" + e.getMessage());
        } catch (IOException e1) {
        }
    }


    public String getCacheDir() {
        return cacheDir;
    }


    private void handleByteRange(HttpServletRequest req, HttpServletResponse resp, File localFile, boolean isGzip) throws Exception {
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
    }

    private Long[] resolveByteRange(String range, Long fileSize) {
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
            int index = range.indexOf("-");
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

    private static Pattern RANGE_FMT_START_END = Pattern.compile("^[0-9]{1,}-[0-9]{1,}$");
    private static Pattern RANGE_FMT_START_TILL_END = Pattern.compile("^[0-9]{1,}-$");
    private static Pattern RANGE_FMT_BYTES_END = Pattern.compile("^-[0-9]{1,}$");
    private static String CONTENTRANGE_FMT = "bytes %s-%s/%s";
}
