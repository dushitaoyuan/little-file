package com.taoyuanx.littlefile.server.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author dushitaoyuan
 * @date 2020/7/822:28
 */
public class FileDeteleUtil {
    private static final String LOG_DATE_FORMAT = "yyyymmdd";
    private static final String FILE_NAME_LOG_DATE_FORMAT = "delete_bin%s.log";

    public static String getNewFileDeleteLogName() {
        return String.format(FILE_NAME_LOG_DATE_FORMAT, DateTimeFormatter.ofPattern(LOG_DATE_FORMAT).format(LocalDateTime.now()));
    }

    public static String getLastFileDeleteLogName() {
        return String.format(FILE_NAME_LOG_DATE_FORMAT, DateTimeFormatter.ofPattern(LOG_DATE_FORMAT).format(LocalDateTime.now().minusDays(1)));
    }
}
