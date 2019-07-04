package com.taoyuanx.littlefile.sample.config;

import com.taoyuanx.littlefile.clean.FileClean;
import com.taoyuanx.littlefile.config.LittleFileConfig;
import com.taoyuanx.littlefile.web.Constant;
import com.taoyuanx.littlefile.web.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * @author dushitaoyuan
 * @desc 系统配置
 * @date 2019/7/4
 */
@Configuration
public class LittleFileBootConfig {
    @Bean
    public LittleFileConfig littleFileConfig() {
        LittleFileConfig littleFileConfig = new LittleFileConfig("classpath:littlefile.properties");
        return littleFileConfig;
    }

    @Bean
    @Autowired
    public FileHandler fileHandler(LittleFileConfig littleFileConfig) {
        // 配置缓存目录地址
        String cacheDir = littleFileConfig.getConfig(LittleFileConfig.LITTLEFILE_FILE_CACHE_DIR);
        File cacheDirFile = null;
        if (cacheDir.startsWith(Constant.LITTLEFILE_CACHEDIR_ABS_PREFIX)) {
            cacheDirFile = new File(cacheDir.replaceFirst(Constant.LITTLEFILE_CACHEDIR_ABS_PREFIX, ""));
        }
        if (!cacheDirFile.exists()) {
            cacheDirFile.mkdirs();
        }
        String absloulteCacheFileDIR = cacheDirFile.getAbsolutePath();
        boolean gzip = littleFileConfig.getConfig(LittleFileConfig.LITTLEFILE_FILE_GZIP);
        String url_format = littleFileConfig.getConfig(LittleFileConfig.LITTLEFILE_FILEHANDLE_URL_FORMAT);
        if (littleFileConfig.getTokenManager() != null) {
            Long tokenExpireMin = littleFileConfig.getConfig(LittleFileConfig.LITTLEFILE_TOKEN_EXPIRE_MIN);
            return new FileHandler(absloulteCacheFileDIR,
                    littleFileConfig.getFileDownStrategy(absloulteCacheFileDIR), gzip,
                    littleFileConfig.getTokenManager()
                    , tokenExpireMin, url_format);

        } else {
            return new FileHandler(absloulteCacheFileDIR, littleFileConfig.getFileDownStrategy(absloulteCacheFileDIR), gzip, url_format);
        }
    }

    @Bean(destroyMethod = "stop")
    @Autowired
    public FileClean fileClean(LittleFileConfig littleFileConfig, FileHandler fileHandler) {
        FileClean fileClean = littleFileConfig.getFileClean(fileHandler.getCacheDir());
        if (fileClean != null) {
            fileClean.start();
        }
        return fileClean;
    }
}
