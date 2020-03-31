package com.taoyuanx.littlefile.combine.core.store.ftp;

import com.taoyuanx.littlefile.combine.core.FileStoreTypeEnum;
import com.taoyuanx.littlefile.combine.core.store.AbstractFileStoreService;
import com.taoyuanx.littlefile.combine.core.store.FileStoreService;
import com.taoyuanx.littlefile.combine.core.utils.Utils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import java.io.InputStream;
import java.io.OutputStream;

public class FtpFileService extends AbstractFileStoreService implements FileStoreService {
    private static final Logger LOG = LoggerFactory.getLogger(FtpFileService.class);
    private String host, username, password, workDir;
    private int port = 21;
    private KeyManager keyManager;
    private TrustManager trustManager;
    private boolean isWorkDirNotEmpty = false, isSSL = false;
    private int default_timeout = 3000;

    public FtpFileService(String host, int port, String username, String password, String workDir, String keyManagerPath, String keyManagerPassword,
                          String trustManagerPath, String trustManagerPassword) {
        super(FileStoreTypeEnum.FTP.protocol);
        this.host = host;
        this.username = username;
        this.password = password;
        this.workDir = workDir;
        this.isWorkDirNotEmpty = Utils.isNotEmpty(workDir) ? "/".equals(workDir) : false;
        try {
            if (Utils.isNotEmpty(keyManagerPath)) {
                keyManager = Utils.getKeyManager(keyManagerPath, keyManagerPassword);
                trustManager = Utils.getTrustManager(trustManagerPath, trustManagerPassword);
                isSSL = true;
            }
        } catch (Exception e) {
            throw new RuntimeException("load keyManager,trustManager failed,异常:{}", e);
        }

        this.port = port;
    }

    public FtpFileService(String host, int port, String username, String password, String workDir) {
        this(host, port, username, password, workDir, null, null, null, null);
    }

    public FTPClient getFtpClient() throws Exception {
        try {
            FTPClient ftpClient = null;
            if (isSSL) {
                FTPSClient ftpsClient = new FTPSClient(true);
                ftpsClient.setKeyManager(keyManager);
                ftpsClient.setTrustManager(trustManager);
                //ftpsClient.execPBSZ(0);
                //ftpsClient.execPROT("P");
                ftpClient = ftpsClient;
            } else {
                ftpClient = new FTPClient();
            }
            ftpClient.setDefaultTimeout(default_timeout);
            ftpClient.connect(host, port);
            ftpClient.login(username, password);
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                LOG.info("{} ftp server 拒绝连接", host);
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            if (isWorkDirNotEmpty) {
                ftpClient.changeWorkingDirectory(workDir);
            }
            ftpClient.setControlEncoding("UTF-8");
            return ftpClient;

        } catch (Exception e) {
            LOG.error("{} ftp server connect failed,异常", e);
            throw e;
        }
    }

    @Override
    public String store(InputStream inputStream, String fileName) throws Exception {
        FTPClient ftpClient = getFtpClient();
        try {
            FTPClient client = getFtpClient();
            String filePath = Utils.buildFilePath(Utils.newFileName(fileName));
            mkdirs(filePath);
            client.storeFile(filePath, inputStream);
            return Utils.addFileStoreProtocol(getStoreProtocol(), filePath);
        } finally {
            close(ftpClient);
        }
    }

    @Override
    public void delete(String fileId) throws Exception {
        FTPClient ftpClient = getFtpClient();
        try {
            fileId = Utils.removeFileStoreProtocol(fileId);
            ftpClient.deleteFile(fileId);
        } finally {
            close(ftpClient);
        }

    }

    @Override
    public void downLoad(String fileId, OutputStream outputStream) throws Exception {
        FTPClient ftpClient = getFtpClient();
        try {
            fileId = Utils.removeFileStoreProtocol(fileId);
            ftpClient.retrieveFile(fileId, outputStream);
        } finally {
            close(ftpClient);
        }
    }

    public void mkdirs(String path) throws Exception {
        try {
            FTPClient client = getFtpClient();
            int index = path.lastIndexOf("/");
            if (index > 0) {
                client.makeDirectory(path.substring(0, index));
            } else if (path.endsWith("/")) {
                client.makeDirectory(path);
            }
        } catch (Exception e) {
            LOG.error("mkdirs {}  failed,异常", path, e);
            throw e;
        }
    }

    public void close(FTPClient ftpClient) {
        try {
            if (ftpClient != null) {
                ftpClient.disconnect();
            }
        } catch (Exception e) {
            LOG.error("close error", e);
        }
    }


}
