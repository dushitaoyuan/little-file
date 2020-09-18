package com.taoyuanx.littlefile.config;

import com.taoyuanx.littlefile.alioss.AliyunOssFileService;
import com.taoyuanx.littlefile.clean.FileClean;
import com.taoyuanx.littlefile.clean.FileCleanTask.BadFileAndPeriodDelte;
import com.taoyuanx.littlefile.clean.FileCleanTask.CheckDelete;
import com.taoyuanx.littlefile.fdfs.FdfsFileService;
import com.taoyuanx.littlefile.ftp.LittleFileFtp;
import com.taoyuanx.littlefile.minio.MinioConfig;
import com.taoyuanx.littlefile.minio.MinioFileService;
import com.taoyuanx.littlefile.sftp.LittleFileSftp;
import com.taoyuanx.littlefile.support.FileDownStrategy;
import com.taoyuanx.littlefile.support.FileServerEum;
import com.taoyuanx.littlefile.support.impl.*;
import com.taoyuanx.littlefile.util.Utils;
import com.taoyuanx.littlefile.web.security.AbstractSimpleTokenManager;
import com.taoyuanx.littlefile.web.security.HmacTokenManager;
import com.taoyuanx.littlefile.web.security.MacEum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 * @author 都市桃源
 * 2018年12月28日
 * 全局配置类
 */
public class LittleFileConfig {
    public static final Logger LOG = LoggerFactory.getLogger(LittleFileConfig.class);
    public static final String LITTLE_FILE_CONFIG_PREIFIX = "littlefile.";

    public static class FdfsConfig {
        public static final String LITTLEFILE_FDFS_FDFS_CONF = LITTLE_FILE_CONFIG_PREIFIX + "fdfs.fdfs_conf";
    }

    public static class SftpConfig {
        public static final String LITTLEFILE_SFTP_HOST = LITTLE_FILE_CONFIG_PREIFIX + "sftp.host";
        public static final String LITTLEFILE_SFTP_PORT = LITTLE_FILE_CONFIG_PREIFIX + "sftp.port";
        public static final String LITTLEFILE_SFTP_USERNAME = LITTLE_FILE_CONFIG_PREIFIX + "sftp.username";
        public static final String LITTLEFILE_SFTP_PASSWORD = LITTLE_FILE_CONFIG_PREIFIX + "sftp.password";
        public static final String LITTLEFILE_SFTP_WORKDIR = LITTLE_FILE_CONFIG_PREIFIX + "sftp.workdir";
        public static final String LITTLEFILE_SFTP_PRIVATE_KEY = LITTLE_FILE_CONFIG_PREIFIX + "sftp.private_key";
        public static final String LITTLEFILE_SFTP_PRIVATE_KEY_PASSWORD = LITTLE_FILE_CONFIG_PREIFIX + "sftp.private_key_password";
    }

    public static class FtpConfig {
        public static final String LITTLEFILE_FTP_HOST = LITTLE_FILE_CONFIG_PREIFIX + "ftp.host";
        public static final String LITTLEFILE_FTP_PORT = LITTLE_FILE_CONFIG_PREIFIX + "ftp.port";
        public static final String LITTLEFILE_FTP_USERNAME = LITTLE_FILE_CONFIG_PREIFIX + "ftp.username";
        public static final String LITTLEFILE_FTP_PASSWORD = LITTLE_FILE_CONFIG_PREIFIX + "ftp.password";
        public static final String LITTLEFILE_FTP_WORKDIR = LITTLE_FILE_CONFIG_PREIFIX + "ftp.workdir";
        public static final String LITTLEFILE_FTP_KEY_MANAGER_PATH = LITTLE_FILE_CONFIG_PREIFIX + "ftp.key_manager_path";
        public static final String LITTLEFILE_FTP_KEY_MANAGER_PASSWORD = LITTLE_FILE_CONFIG_PREIFIX + "ftp.key_manager_password";
        public static final String LITTLEFILE_FTP_TRUST_MANAGER_PATH = LITTLE_FILE_CONFIG_PREIFIX + "ftp.trust_manager_path";
        public static final String LITTLEFILE_FTP_TRUST_MANAGER_PASSWORD = LITTLE_FILE_CONFIG_PREIFIX + "ftp.trust_manager_password";
    }

    public static class LocalConfig {
        public static final String LITTLEFILE_LOCAL_DIRS = LITTLE_FILE_CONFIG_PREIFIX + "local.dirs";
    }

    public static class AliOssConfig {
        /**
         * oss 相关常量配置名称
         * ACCESSURL_EXPIRE_TIME 访问url过期时间配置名称
         * OSS_FILE_PATH_PREFIX  oss 文件存储前缀 @see
         */
        public static final String BUCKET_NAME = LITTLE_FILE_CONFIG_PREIFIX + "alioss.bucketName";
        public static final String ACCESSKEY_ID = LITTLE_FILE_CONFIG_PREIFIX + "alioss.AccessKeyID";
        public static final String ACCESSKEY_SECRET = LITTLE_FILE_CONFIG_PREIFIX + "alioss.AccessKeySecret";
        public static final String ENDPOINT = LITTLE_FILE_CONFIG_PREIFIX + "alioss.endpoint";

    }

    public static class MinioConfig {
        /**
         * minio 相关常量配置名称
         */
        public static final String BUCKET_NAME = LITTLE_FILE_CONFIG_PREIFIX + "minio.bucketName";
        public static final String ACCESS_KEY = LITTLE_FILE_CONFIG_PREIFIX + "minio.accessKey";
        public static final String SECRET_KEY = LITTLE_FILE_CONFIG_PREIFIX + "minio.secretKey";
        public static final String ENDPOINT = LITTLE_FILE_CONFIG_PREIFIX + "minio.endpoint";
        public static final String REGION = LITTLE_FILE_CONFIG_PREIFIX + "minio.region";


        public static final String CONNECT_TIMEOUT = LITTLE_FILE_CONFIG_PREIFIX + "minio.connectTimeout";
        public static final String MAXIDLE_CONNECTIONS = LITTLE_FILE_CONFIG_PREIFIX + "minio.maxIdleConnections";
        public static final String KEEPALIVE_DURATION = LITTLE_FILE_CONFIG_PREIFIX + "minio.keepAliveDuration";

    }

    public static final String LITTLEFILE_SERVER_TYPE = LITTLE_FILE_CONFIG_PREIFIX + "server_type";
    public static final String LITTLEFILE_FILE_GZIP = LITTLE_FILE_CONFIG_PREIFIX + "file.gzip";
    public static final String LITTLEFILE_FILE_CACHE_TIME = LITTLE_FILE_CONFIG_PREIFIX + "file_cache_time";
    public static final Long LITTLEFILE_FILE_CACHE_TIME_DEFAULT = TimeUnit.MINUTES.toMillis(30);

    public static final String LITTLEFILE_FILE_CLEAN_THREAD_NUM = LITTLE_FILE_CONFIG_PREIFIX + "file_clean_thread_num";
    public static final String LITTLEFILE_FILE_CACHE_DIR = LITTLE_FILE_CONFIG_PREIFIX + "file_cache_dir";
    public static final Integer LITTLEFILE_FILE_CLEAN_THREAD_NUM_DEFAULT = 3;


    //安全控制
    public static final String LITTLEFILE_TOKEN_HMAC = LITTLE_FILE_CONFIG_PREIFIX + "token.hmac";
    public static final String LITTLEFILE_TOKEN_PASSWORD = LITTLE_FILE_CONFIG_PREIFIX + "token.password";
    //token过期时间
    public static final String LITTLEFILE_TOKEN_EXPIRE_MIN = LITTLE_FILE_CONFIG_PREIFIX + "token.expireMin";
    //文件访问url模板
    public static final String LITTLEFILE_FILEHANDLE_URL_FORMAT = LITTLE_FILE_CONFIG_PREIFIX + "fileHandle.url_format";

    private static final Map<String, Object> CONFIGHOLDER = new HashMap<>();

    public LittleFileConfig(String littleConfig) {
        try {
            String property = null;
            Properties pro = new Properties();
            pro.load(Utils.loadFile(littleConfig));
            property = pro.getProperty(LITTLEFILE_SERVER_TYPE, "FDFS");
            FileServerEum serverEum = FileServerEum.valueOf(property);
            CONFIGHOLDER.put(LITTLEFILE_SERVER_TYPE, serverEum);
            property = pro.getProperty(LITTLEFILE_FILE_CACHE_TIME);
            Long cacheTime = Utils.calcTimes(property);

            if (null == cacheTime) {
                cacheTime = LITTLEFILE_FILE_CACHE_TIME_DEFAULT;
            }
            CONFIGHOLDER.put(LITTLEFILE_FILE_CACHE_TIME, cacheTime);
            property = pro.getProperty(LITTLEFILE_FILE_CLEAN_THREAD_NUM);
            Integer num = LITTLEFILE_FILE_CLEAN_THREAD_NUM_DEFAULT;
            if (Utils.isNotEmpty(property)) {
                num = Integer.parseInt(property);
            }
            CONFIGHOLDER.put(LITTLEFILE_FILE_CLEAN_THREAD_NUM, num);
            CONFIGHOLDER.put(LITTLEFILE_FILE_CACHE_DIR, pro.getProperty(LITTLEFILE_FILE_CACHE_DIR));
            String gzip = pro.getProperty(LITTLEFILE_FILE_GZIP);
            if (Utils.isEmpty(gzip) || !Boolean.parseBoolean(gzip)) {
                CONFIGHOLDER.put(LITTLEFILE_FILE_GZIP, false);
            } else {
                CONFIGHOLDER.put(LITTLEFILE_FILE_GZIP, true);
            }

            String hmac = pro.getProperty(LITTLEFILE_TOKEN_HMAC);
            if (Utils.isNotEmpty(hmac)) {
                CONFIGHOLDER.put(LITTLEFILE_TOKEN_HMAC, MacEum.valueOf(hmac.toUpperCase()));
                CONFIGHOLDER.put(LITTLEFILE_TOKEN_PASSWORD, pro.getProperty(LITTLEFILE_TOKEN_PASSWORD));
                String tokenExpireMin = pro.getProperty(LITTLEFILE_TOKEN_EXPIRE_MIN);
                if (Utils.isNotEmpty(tokenExpireMin)) {
                    CONFIGHOLDER.put(LITTLEFILE_TOKEN_EXPIRE_MIN, Long.parseLong(tokenExpireMin));
                } else {
                    //默认30分钟
                    CONFIGHOLDER.put(LITTLEFILE_TOKEN_EXPIRE_MIN, 30L);
                }

            }
            String fileUrlFormat = pro.getProperty(LITTLEFILE_FILEHANDLE_URL_FORMAT);
            if (Utils.isEmpty(fileUrlFormat)) {
                throw new RuntimeException("config [" + LITTLEFILE_FILEHANDLE_URL_FORMAT + "] can not be null");
            }
            CONFIGHOLDER.put(LITTLEFILE_FILEHANDLE_URL_FORMAT, fileUrlFormat);
            switch (serverEum) {
                case FDFS: {
                    CONFIGHOLDER.put(FdfsConfig.LITTLEFILE_FDFS_FDFS_CONF, pro.get(FdfsConfig.LITTLEFILE_FDFS_FDFS_CONF));
                }
                break;
                case FTP: {
                    CONFIGHOLDER.put(FtpConfig.LITTLEFILE_FTP_HOST, pro.getProperty(FtpConfig.LITTLEFILE_FTP_HOST));
                    CONFIGHOLDER.put(FtpConfig.LITTLEFILE_FTP_PORT, Integer.parseInt(pro.getProperty(FtpConfig.LITTLEFILE_FTP_PORT)));
                    CONFIGHOLDER.put(FtpConfig.LITTLEFILE_FTP_USERNAME, pro.getProperty(FtpConfig.LITTLEFILE_FTP_USERNAME));
                    CONFIGHOLDER.put(FtpConfig.LITTLEFILE_FTP_PASSWORD, pro.getProperty(FtpConfig.LITTLEFILE_FTP_PASSWORD));
                    CONFIGHOLDER.put(FtpConfig.LITTLEFILE_FTP_WORKDIR, pro.getProperty(FtpConfig.LITTLEFILE_FTP_WORKDIR));
                    CONFIGHOLDER.put(FtpConfig.LITTLEFILE_FTP_KEY_MANAGER_PATH, pro.getProperty(FtpConfig.LITTLEFILE_FTP_KEY_MANAGER_PATH));
                    CONFIGHOLDER.put(FtpConfig.LITTLEFILE_FTP_KEY_MANAGER_PASSWORD, pro.getProperty(FtpConfig.LITTLEFILE_FTP_KEY_MANAGER_PASSWORD));
                    CONFIGHOLDER.put(FtpConfig.LITTLEFILE_FTP_TRUST_MANAGER_PATH, pro.getProperty(FtpConfig.LITTLEFILE_FTP_TRUST_MANAGER_PATH));
                    CONFIGHOLDER.put(FtpConfig.LITTLEFILE_FTP_TRUST_MANAGER_PASSWORD, pro.getProperty(FtpConfig.LITTLEFILE_FTP_TRUST_MANAGER_PASSWORD));
                }
                break;
                case SFTP: {
                    CONFIGHOLDER.put(SftpConfig.LITTLEFILE_SFTP_HOST, pro.getProperty(SftpConfig.LITTLEFILE_SFTP_HOST));
                    CONFIGHOLDER.put(SftpConfig.LITTLEFILE_SFTP_PORT, Integer.parseInt(pro.getProperty(SftpConfig.LITTLEFILE_SFTP_PORT)));
                    CONFIGHOLDER.put(SftpConfig.LITTLEFILE_SFTP_USERNAME, pro.getProperty(SftpConfig.LITTLEFILE_SFTP_USERNAME));
                    CONFIGHOLDER.put(SftpConfig.LITTLEFILE_SFTP_PASSWORD, pro.getProperty(SftpConfig.LITTLEFILE_SFTP_PASSWORD));
                    CONFIGHOLDER.put(SftpConfig.LITTLEFILE_SFTP_WORKDIR, pro.getProperty(SftpConfig.LITTLEFILE_SFTP_WORKDIR));
                    CONFIGHOLDER.put(SftpConfig.LITTLEFILE_SFTP_PRIVATE_KEY, pro.getProperty(SftpConfig.LITTLEFILE_SFTP_PRIVATE_KEY));
                    CONFIGHOLDER.put(SftpConfig.LITTLEFILE_SFTP_PRIVATE_KEY_PASSWORD, pro.getProperty(SftpConfig.LITTLEFILE_SFTP_PRIVATE_KEY_PASSWORD));
                }
                break;

                case OSS: {
                    CONFIGHOLDER.put(AliOssConfig.BUCKET_NAME, pro.getProperty(AliOssConfig.BUCKET_NAME));
                    CONFIGHOLDER.put(AliOssConfig.ACCESSKEY_ID, pro.getProperty(AliOssConfig.ACCESSKEY_ID));
                    CONFIGHOLDER.put(AliOssConfig.ACCESSKEY_SECRET, pro.getProperty(AliOssConfig.ACCESSKEY_SECRET));
                    CONFIGHOLDER.put(AliOssConfig.ENDPOINT, pro.getProperty(AliOssConfig.ENDPOINT));
                }
                break;
                case LOCAL: {
                    String localDirs = pro.getProperty(LocalConfig.LITTLEFILE_LOCAL_DIRS);
                    if (Utils.isEmpty(localDirs)) {
                        throw new RuntimeException("local mode " + LocalConfig.LITTLEFILE_LOCAL_DIRS + " can't empty");
                    }
                    CONFIGHOLDER.put(LocalConfig.LITTLEFILE_LOCAL_DIRS, localDirs.split(","));
                }
                break;
            }
        } catch (Exception e) {
            LOG.error("初始化littlefile {}配置失败", littleConfig, e);
        }
    }

    public <T> T getConfig(String configKey) {
        return (T) CONFIGHOLDER.get(configKey);
    }


    static FileClean fileClean = null;

    public FileClean getFileClean(String cacheDir) {
        if (null == fileClean) {
            Long cacheTime = getConfig(LITTLEFILE_FILE_CACHE_TIME);
            if (cacheTime == 0) {
                return null;
            }
            CheckDelete checkDelete = new BadFileAndPeriodDelte(cacheTime);
            Integer num = getConfig(LITTLEFILE_FILE_CLEAN_THREAD_NUM);
            fileClean = new FileClean(num, new File(cacheDir), checkDelete, cacheTime);
        }
        return fileClean;
    }

    public FileDownStrategy getFileDownStrategy(String cacheDir) {
        FileServerEum serverEum = getConfig(LITTLEFILE_SERVER_TYPE);
        FileDownStrategy strategy = null;
        switch (serverEum) {
            case FDFS: {
                String configPath = getConfig(FdfsConfig.LITTLEFILE_FDFS_FDFS_CONF);
                strategy = new FdfsStrategy(new FdfsFileService(configPath));
            }
            break;
            case FTP: {
                String host = getConfig(FtpConfig.LITTLEFILE_FTP_HOST);
                Integer port = getConfig(FtpConfig.LITTLEFILE_FTP_PORT);
                String username = getConfig(FtpConfig.LITTLEFILE_FTP_USERNAME);
                String password = getConfig(FtpConfig.LITTLEFILE_FTP_PASSWORD);
                String workDir = getConfig(FtpConfig.LITTLEFILE_FTP_WORKDIR);
                String keyManagerPath = getConfig(FtpConfig.LITTLEFILE_FTP_KEY_MANAGER_PATH);
                String keyManagerPassword = getConfig(FtpConfig.LITTLEFILE_FTP_KEY_MANAGER_PASSWORD);
                String trustManagerPath = getConfig(FtpConfig.LITTLEFILE_FTP_TRUST_MANAGER_PATH);
                String trustManagerPassword = getConfig(FtpConfig.LITTLEFILE_FTP_TRUST_MANAGER_PASSWORD);
                LittleFileFtp littleFileFtp = new LittleFileFtp(host, port, username, password, workDir, keyManagerPath, keyManagerPassword, trustManagerPath, trustManagerPassword);
                strategy = new FtpStrategy(littleFileFtp);
            }
            break;
            case SFTP: {
                String host = getConfig(SftpConfig.LITTLEFILE_SFTP_HOST);
                Integer port = getConfig(SftpConfig.LITTLEFILE_SFTP_PORT);
                String username = getConfig(SftpConfig.LITTLEFILE_SFTP_USERNAME);
                String password = getConfig(SftpConfig.LITTLEFILE_SFTP_PASSWORD);
                String workDir = getConfig(SftpConfig.LITTLEFILE_SFTP_WORKDIR);
                String privateKey = getConfig(SftpConfig.LITTLEFILE_SFTP_PRIVATE_KEY);
                String privateKeyPassword = getConfig(SftpConfig.LITTLEFILE_SFTP_PASSWORD);
                LittleFileSftp littleFileSftp = new LittleFileSftp(host, port, username, password, workDir, privateKey, privateKeyPassword);
                strategy = new SftpStrategy(littleFileSftp);
            }
            break;

            case OSS: {
                String msg = "alioss配置丢失,请检查配置";
                String accessKeyID = getConfig(AliOssConfig.ACCESSKEY_ID);
                String accessKeySecret = getConfig(AliOssConfig.ACCESSKEY_SECRET);
                String bucketName = getConfig(AliOssConfig.BUCKET_NAME);
                String endpoint = getConfig(AliOssConfig.ENDPOINT);
                Utils.notNull(accessKeyID, msg);
                Utils.notNull(accessKeySecret, msg);
                Utils.notNull(bucketName, msg);
                Utils.notNull(endpoint, msg);
                AliyunOssFileService aliyunOssFileService = new AliyunOssFileService(endpoint, accessKeyID, accessKeySecret, bucketName);
                strategy = new AliyunOssStrategy(aliyunOssFileService);
            }
            case MINIO: {
                String msg = "minio配置丢失,请检查配置";
                String accessKey = getConfig(MinioConfig.ACCESS_KEY);
                String secretKey = getConfig(MinioConfig.SECRET_KEY);
                String bucketName = getConfig(MinioConfig.BUCKET_NAME);
                String endpoint = getConfig(MinioConfig.ENDPOINT);
                String region = getConfig(MinioConfig.REGION);
                Utils.notNull(accessKey, msg);
                Utils.notNull(secretKey, msg);
                Utils.notNull(bucketName, msg);
                Utils.notNull(endpoint, msg);
                com.taoyuanx.littlefile.minio.MinioConfig minioConfig = new com.taoyuanx.littlefile.minio.MinioConfig();
                minioConfig.setAccessKey(accessKey);
                minioConfig.setSecretKey(secretKey);
                minioConfig.setBucketName(bucketName);
                minioConfig.setEndpoint(endpoint);
                minioConfig.setRegion(region);

                String connectTimeout = getConfig(MinioConfig.CONNECT_TIMEOUT);
                String maxIdleConnections = getConfig(MinioConfig.KEEPALIVE_DURATION);
                String keepAliveDuration = getConfig(MinioConfig.MAXIDLE_CONNECTIONS);
                if (Utils.isNotEmpty(connectTimeout)) {
                    minioConfig.setConnectTimeout(Integer.getInteger(connectTimeout));
                }
                if (Utils.isNotEmpty(maxIdleConnections)) {
                    minioConfig.setMaxIdleConnections(Integer.getInteger(maxIdleConnections));
                }
                if (Utils.isNotEmpty(keepAliveDuration)) {
                    minioConfig.setKeepAliveDuration(Integer.getInteger(keepAliveDuration));
                }
                MinioFileService minioFileService = new MinioFileService(minioConfig);
                strategy = new MinioStrategy(minioFileService);
            }
            break;
            case LOCAL: {
                String localDirs[] = getConfig(LocalConfig.LITTLEFILE_LOCAL_DIRS);
                strategy = new LocalStrategy(localDirs);
            }
            break;

        }
        return strategy;
    }

    AbstractSimpleTokenManager tokenManager = null;

    public AbstractSimpleTokenManager getTokenManager() {
        if (tokenManager != null) {
            return tokenManager;
        }
        MacEum mac = getConfig(LITTLEFILE_TOKEN_HMAC);
        if (mac == null) {
            return null;
        }
        String password = getConfig(LITTLEFILE_TOKEN_PASSWORD);
        tokenManager = new HmacTokenManager(mac, password);
        return tokenManager;
    }


}
