package com.taoyuanx.littlefile.combine.core.store.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.taoyuanx.littlefile.combine.core.FileStoreTypeEnum;
import com.taoyuanx.littlefile.combine.core.store.AbstractFileStoreService;
import com.taoyuanx.littlefile.combine.core.store.FileStoreService;
import com.taoyuanx.littlefile.combine.core.utils.Utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * sftp 实现
 */
@SuppressWarnings("all")
public class SftpFileService extends AbstractFileStoreService implements FileStoreService {

    private String host, username, password, workDir, privateKey, privateKeyPassword;
    private int port;
    private boolean usePassword = true, isWorkDirNotEmpty = false;
    private JSch jsch;

    /**
     * 私钥认证
     *
     * @param host
     * @param port
     * @param username
     * @param privateKey
     * @param privateKeyPassword
     * @param workDir
     */
    public SftpFileService(String host, int port, String username, String password, String workDir, String privateKey, String privateKeyPassword) {
        super(FileStoreTypeEnum.SFTP.protocol);
        this.host = host;
        this.port = port;
        this.username = username;
        this.workDir = workDir;
        this.password = password;
        this.isWorkDirNotEmpty = Utils.isNotEmpty(workDir);
        if (Utils.isNotEmpty(privateKey)) {
            this.privateKey = privateKey;
            this.privateKeyPassword = privateKeyPassword;
            usePassword = false;
        }
        jsch = new JSch(); // 创建JSch对象
    }

    public SftpFileService(String host, int port, String username, String password, String workDir) {
        this(host, port, username, password, workDir, null, null);
    }

    @Override
    public String store(InputStream inputStream, String fileName) throws Exception {
        ChannelSftp channel = getSftpChannel();
        try {
            String filePath = Utils.buildFilePath(Utils.newFileName(fileName));
            String file = mkdirs(filePath, channel);
            channel.put(inputStream, file);
            return Utils.addFileStoreProtocol(getStoreProtocol(), filePath);
        } finally {
            closeChannel(channel);
        }
    }

    @Override
    public void delete(String fileId) throws Exception {
        ChannelSftp channel = getSftpChannel();
        try {
            fileId = Utils.removeFileStoreProtocol(fileId);
            channel.rm(fileId);
        } finally {
            closeChannel(channel);
        }
    }

    @Override
    public void downLoad(String fileId, OutputStream outputStream) throws Exception {


        ChannelSftp channel = getSftpChannel();
        try {
            fileId = Utils.removeFileStoreProtocol(fileId);
            channel.get(fileId, outputStream);
        } finally {
            closeChannel(channel);
        }
    }


    /**
     * 递归创建目录
     *
     * @param filePath
     * @param channel
     * @return
     * @throws Exception
     */
    private String mkdirs(String filePath, ChannelSftp channel) throws Exception {
        if (filePath.indexOf("/") > -1) {
            String dirs[] = filePath.split("/");
            int i = 0;
            for (i = 0; i < dirs.length - 1; i++) {
                try {
                    channel.cd(dirs[i]);
                } catch (Exception e) {
                    channel.mkdir(dirs[i]);
                    channel.cd(dirs[i]);
                }
            }
            return dirs[i];
        }
        return filePath;

    }

    /**
     * 关闭channel
     *
     * @param channel
     * @throws Exception
     */
    public void closeChannel(ChannelSftp channel) throws Exception {
        if (channel != null) {
            channel.disconnect();
            if (channel.getSession() != null) {
                channel.getSession().disconnect();
            }
        }
    }

    /**
     * 打开stfp 通道
     *
     * @return sftp通道
     * @throws Exception
     */
    private ChannelSftp getSftpChannel() throws Exception {
        Session session = jsch.getSession(username, host, port);
        if (usePassword) {
            session.setPassword(password);
        } else {
            jsch.addIdentity(privateKey, privateKeyPassword);
        }
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setTimeout(3000);
        session.setConfig(config);
        session.connect();
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        if (isWorkDirNotEmpty) {
            channel.cd(workDir);
        }
        return channel;


    }


}
