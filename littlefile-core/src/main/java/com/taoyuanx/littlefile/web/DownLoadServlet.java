package com.taoyuanx.littlefile.web;

import com.taoyuanx.littlefile.clean.FileClean;
import com.taoyuanx.littlefile.config.LittleFileConfig;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @author 都市桃源 2018年12月28日 Servlet 模式
 */
public class DownLoadServlet extends HttpServlet {
    private static final long serialVersionUID = -7181085958885587018L;
    private FileClean fileClean;
    private FileHandler fileHandler = null;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        fileHandler.handleFile(resp, req);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        LittleFileConfig littleFileConfig = new LittleFileConfig(config.getInitParameter(Constant.LITTLEFILE_CONF));
        // 配置缓存目录地址
        String cacheDir = littleFileConfig.getConfig(LittleFileConfig.LITTLEFILE_FILE_CACHE_DIR);
        File cacheDirFile = null;
        if (cacheDir.startsWith(Constant.LITTLEFILE_CACHEDIR_ABS_PREFIX)) {
            cacheDirFile = new File(cacheDir.replaceFirst(Constant.LITTLEFILE_CACHEDIR_ABS_PREFIX, ""));
        } else if (cacheDir.startsWith(Constant.LITTLEFILE_CACHEDIR_WEBAPP_PREFIX)) {
            cacheDirFile = new File(config.getServletContext().getRealPath("/"),
                    cacheDir.replaceFirst(Constant.LITTLEFILE_CACHEDIR_WEBAPP_PREFIX, ""));
        }
        if (!cacheDirFile.exists()) {
            cacheDirFile.mkdirs();
        }
        String absloulteCacheFileDIR = cacheDirFile.getAbsolutePath();
        boolean gzip = littleFileConfig.getConfig(LittleFileConfig.LITTLEFILE_FILE_GZIP);
        String url_format = littleFileConfig.getConfig(LittleFileConfig.LITTLEFILE_FILEHANDLE_URL_FORMAT);
        if (littleFileConfig.getTokenManager() != null) {
            Long tokenExpireMin = littleFileConfig.getConfig(LittleFileConfig.LITTLEFILE_TOKEN_EXPIRE_MIN);
            fileHandler = new FileHandler(absloulteCacheFileDIR,
                    littleFileConfig.getFileDownStrategy(absloulteCacheFileDIR), gzip,
                    littleFileConfig.getTokenManager()
                    , tokenExpireMin, url_format);

        } else {
            fileHandler = new FileHandler(absloulteCacheFileDIR, littleFileConfig.getFileDownStrategy(absloulteCacheFileDIR), gzip, url_format);
        }
        fileClean = littleFileConfig.getFileClean(absloulteCacheFileDIR);
        if (fileClean != null) {
            fileClean.start();
        }
    }

    @Override
    public void destroy() {
        if (fileClean != null) {
            fileClean.stop();
        }
    }

}
