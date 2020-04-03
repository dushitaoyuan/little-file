package com.taoyuanx.littlefile.server.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.csource.common.NameValuePair;
import com.taoyuanx.littlefile.server.utils.Utils;


public class FdfsHelperUtil {

    /**
     * 获取文件名
     *
     * @param file
     * @return
     */
    public static String getFileName(String file) {
        return FilenameUtils.getName(file);
    }

    /**
     * 从文件名获取扩展名
     *
     * @param fileName
     * @return
     */
    public static String getExtension(String fileName) {
        if (Utils.isEmpty(fileName)) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 获取文件前缀
     *
     * @param fileName
     * @return
     */
    public static String getPrefix(String fileName) {
        if (Utils.isEmpty(fileName)) {
            return null;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }


    private static final int BUFFER_SIZE = 4 << 20;

    public static byte[] streamToArray(InputStream input) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        transferTo(input, outputStream);
        return outputStream.toByteArray();
    }

    public static void transferTo(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int len = 0;
        while ((len = input.read(buffer)) != -1) {
            output.write(buffer, 0, len);
        }
    }
}
