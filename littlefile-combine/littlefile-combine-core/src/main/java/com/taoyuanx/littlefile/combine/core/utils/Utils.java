package com.taoyuanx.littlefile.combine.core.utils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.UUID;

public class Utils {
    public static boolean isEmpty(String str) {
        if (null == str || str.length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(String str) {
        if (null != str && str.trim().length() > 0) {
            return true;
        }
        return false;
    }


    public static final String CERT_TYPE_P12 = "PKCS12";
    public static final String CERT_TYPE_JKS = "JKS";


    public static KeyManager getKeyManager(String path, String password) throws Exception {
        if (isEmpty(path)) {
            return null;
        }
        String type = CERT_TYPE_P12;
        if (path.toUpperCase().endsWith(CERT_TYPE_JKS)) {
            type = CERT_TYPE_JKS;
        }
        KeyStore keyStore = KeyStore.getInstance(type);
        keyStore.load(loadFile(path), password.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password.toCharArray());
        KeyManager[] km = kmf.getKeyManagers();
        return km[0];
    }

    public static TrustManager getTrustManager(String path, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(loadFile(path), password.toCharArray());
        TrustManagerFactory tf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tf.init(keyStore);
        TrustManager[] tm = tf.getTrustManagers();
        return tm[0];
    }

    public static final String classpathFlag = "classpath:";

    public static InputStream loadFile(String filePath) throws Exception {
        if (filePath.startsWith(classpathFlag)) {
            return Utils.class.getClassLoader().getResourceAsStream(filePath.replace(classpathFlag, ""));
        } else {
            return new FileInputStream(filePath);
        }
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
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 按年月组织文件
     * 构造文件路径
     */
    public static String buildFilePath(String fileId) {
        return getYearMonth() + "/" + fileId;
    }

    /**
     * 产生新文件名
     */
    public static String newFileName(String fileName) {
        return UUID.randomUUID() + "_" + fileName;
    }

    private static String getYearMonth() {
        Calendar instance = Calendar.getInstance();
        int year = instance.get(Calendar.YEAR);
        int month = instance.get(Calendar.MONTH) + 1;
        return year + "-" + month;
    }

    /**
     * 获取文件名
     *
     * @param file
     * @return
     */
    public static String getFileName(String file) {
        int index = file.lastIndexOf("/") + 1;
        if (index > -1) {
            return file.substring(index);
        } else {
            return new File(file).getName();
        }
    }

    public static String removeFileStoreProtocol(String fileId) {
        int index = fileId.indexOf("://");
        if (index > -1) {
            return fileId.substring(index + 3);
        }
        return fileId;
    }

    public static String addFileStoreProtocol(String storeProtocol, String fileId) {
        return storeProtocol + fileId;
    }

    public static String getStoreProtocol(String fileId) {
        return fileId.substring(0, fileId.indexOf("://") + 3);
    }
}
