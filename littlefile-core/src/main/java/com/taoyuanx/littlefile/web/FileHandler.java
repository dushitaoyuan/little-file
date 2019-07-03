package com.taoyuanx.littlefile.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taoyuanx.littlefile.web.security.TokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taoyuanx.littlefile.fdfs.FdfsUtil;
import com.taoyuanx.littlefile.support.FileDownStrategy;
import com.taoyuanx.littlefile.util.Utils;
import com.taoyuanx.littlefile.web.security.AbstractSimpleTokenManager;

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
    public static final String DOWN = "0", LOOK = "1";
    public static Logger LOG = LoggerFactory.getLogger(FileHandler.class);
    private String cacheDir;
    private FileDownStrategy fileDownStrategy;
    private boolean isGzip = false;
    private AbstractSimpleTokenManager tokenManager;
    private Long tokenExpire;
    private String urlFmt;
    private  boolean tokenOpen=true;
    public FileHandler(String cacheDir, FileDownStrategy fileDownStrategy,
                       boolean isGzip, AbstractSimpleTokenManager tokenManager, Long tokenExpire, String urlFmt) {
        super();
        this.cacheDir = cacheDir;
        this.fileDownStrategy = fileDownStrategy;
        this.isGzip = isGzip;
        this.tokenManager = tokenManager;
        this.tokenExpire = tokenExpire;
        this.urlFmt = urlFmt+"?s=%s";
    }
    public FileHandler(String cacheDir, FileDownStrategy fileDownStrategy,
                       boolean isGzip,String urlFmt) {
        super();
        this.cacheDir = cacheDir;
        this.fileDownStrategy = fileDownStrategy;
        this.isGzip = isGzip;
        tokenOpen=false;
        this.urlFmt = urlFmt+"?f=%s&t=%s";
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
            if(tokenOpen){
                String token = req.getParameter(Constant.REQUEST_PARAM_TOKEN_KEY);
                if (Utils.isEmpty(token)) {
                    throw new TokenException("operation not allowed");
                }
                Map<String, Object> signData = tokenManager.vafy(token);
                type = (String) signData.get(Constant.REQUEST_PARAM_TYPE_KEY);
                filePath = (String) signData.get(Constant.REQUEST_PARAM_FILE_KEY);
            }else{
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
                ;
                break;
                case DOWN: {// 下载
                    resp.setContentType(req.getServletContext().getMimeType(absoluteFile.getName()));
                    resp.setHeader("Content-type", "application/octet-stream");
                    resp.setHeader("Content-Disposition",
                            "attachment;fileName=" + URLEncoder.encode(FdfsUtil.getFileName(filePath), "UTF-8"));
                    resp.setContentType(req.getServletContext().getMimeType(absoluteFile.getName()));

                }
                ;
                break;
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
     * @param fileUrl 文件路径
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
     * @param fileUrl 文件路径
     * @param handleType 文件处理类型 0下载 1查看
     * @return
     */
    public String signFileUrl(String fileUrl, String handleType,Long expire,TimeUnit timeUnit) {
        Map<String, Object> signMap = new HashMap<>();
        signMap.put(Constant.REQUEST_PARAM_FILE_KEY, fileUrl);
        signMap.put(Constant.REQUEST_PARAM_TYPE_KEY, handleType);
        return String.format(urlFmt, tokenManager.create(signMap, expire,timeUnit));
    }

    /**
     * 文件url构造接口
     *
     * @param fileUrl 文件路径
     * @param handleType 文件处理类型 0下载 1查看
     * @return
     */
    public String createPublicUrl(String fileUrl, String handleType) {
        return String.format(urlFmt, fileUrl,handleType);
    }


    private void handle(OutputStream out, File localFile) throws Exception {
        InputStream input = new FileInputStream(localFile);
        byte[] buf = new byte[1024 * 1024];
        int len = 0;
        while ((len = input.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        input.close();
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

}
