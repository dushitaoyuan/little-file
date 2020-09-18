package com.taoyuanx.littlefile.server.config;

import com.taoyuanx.littlefile.server.utils.FileDeteleUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @author dushitaoyuan
 * @date 2020/7/821:29
 * @desc: 定时任务
 */
@Configuration
@Slf4j
public class TaskConfig {
    @Autowired
    FileProperties fileProperties;

    /***
     * 定时删除本地缓存的临时文件
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void deleteLocalTempFileTask() {
        try {
            /**
             * 1.生成log文件地址
             * 2. 逐行读取待删除的binlog文件,执行删除
             */
            fileProperties.setDeleteBinLogFile(new File(fileProperties.getDeleteLogDir(), FileDeteleUtil.getNewFileDeleteLogName()).getAbsolutePath());
            File deleteBinLog = new File(fileProperties.getDeleteLogDir(), FileDeteleUtil.getLastFileDeleteLogName());
            BufferedReader bufferedReader = new BufferedReader(new FileReader(deleteBinLog));
            String deleteLine = "";
            while ((deleteLine = bufferedReader.readLine()) != null) {
                if (StringUtils.hasText(deleteLine)) {
                    FileUtils.deleteQuietly(new File(deleteLine));
                }
            }
            bufferedReader.close();
            FileUtils.deleteQuietly(deleteBinLog);
        } catch (Exception e) {
            log.warn("文件删除失败");
        }
    }
}
