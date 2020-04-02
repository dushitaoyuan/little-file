package com.taoyuanx.littlefile.server.utils;

import com.taoyuanx.littlefile.fdfshttp.core.dto.MasterAndSlave;
import com.taoyuanx.littlefile.server.ex.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.List;

/**
 * @author ncs-spf
 * 可以下载对应的 fdfs包,可以兼容所有的fdfs
 */
@Component
@Slf4j
public class FdfsFileUtil {
    static {
        try {
            ClientGlobal.initByProperties("fdfs.properties");
            log.info("fdfs config:{}", ClientGlobal.configInfo());
        } catch (Exception e) {
            log.error("fastdfs 初始化配置失敗，请检查fdfs.properties 配置");
            throw new RuntimeException(e);
        }

    }

    static TrackerClient tracker = new TrackerClient();

    public StorageClient1 getClient() throws Exception {
        return new StorageClient1(tracker.getConnection(), null);
    }

    /**
     * 上传文件
     *
     * @param data
     * @param fileName
     * @return
     * @throws Exception
     */
    public String upload(byte[] data, String fileName) throws Exception {
        String fileExtName = FilenameUtils.getExtension(fileName);
        return getClient().upload_file1(data, fileExtName, null);
    }

    /**
     * 上传文件
     *
     * @param data
     * @param fileName
     * @return
     * @throws Exception
     */
    public String upload(String group, byte[] data, String fileName) throws Exception {
        String fileExtName = FilenameUtils.getExtension(fileName);
        return getClient().upload_file1(group, data, fileExtName, null);
    }

    /**
     * 上传文件
     *
     * @param input
     * @param fileName
     * @return
     * @throws Exception
     */
    public String upload(InputStream input, String fileName) throws Exception {
        String fileExtName = FilenameUtils.getExtension(fileName);
        return getClient().upload_file1(FdfsHelperUtil.streamToArray(input), fileExtName, null);
    }

    /**
     * 断点续传文件
     *
     * @param input
     * @return
     * @throws Exception
     */
    public String uploadAppendFile(InputStream input, String fileName) throws Exception {
        String fileExtName = FilenameUtils.getExtension(fileName);
        return getClient().upload_appender_file1(FdfsHelperUtil.streamToArray(input), fileExtName, null);
    }

    /**
     * 断点续传文件
     *
     * @param input
     * @return
     * @throws Exception
     */
    public void append(InputStream input, String fileId) throws Exception {
        getClient().append_file1(fileId, FdfsHelperUtil.streamToArray(input));
    }

    /**
     * 断点续传文件
     *
     * @param data
     * @return
     * @throws Exception
     */
    public void append(byte[] data, String fileId) throws Exception {
        getClient().append_file1(fileId, data);
    }

    public void modifyFile(InputStream inputStream, Long offset, String fileId) throws Exception {
        getClient().modify_file1(fileId, offset, FdfsHelperUtil.streamToArray(inputStream));

    }

    /**
     * 上传文件
     *
     * @param input
     * @param fileName
     * @return
     * @throws Exception
     */
    public String upload(String group, InputStream input, String fileName) throws Exception {
        String fileExtName = FilenameUtils.getExtension(fileName);
        return getClient().upload_file1(group, FdfsHelperUtil.streamToArray(input), fileExtName, null);
    }

    /**
     * 上传从文件
     */
    public String uploadSlave(String masterFileId, InputStream input, String fileName) throws Exception {
        String fileExtName = FilenameUtils.getExtension(fileName);
        String filePrefixName = FilenameUtils.getPrefix(fileName);
        return getClient().upload_file1(masterFileId, filePrefixName, FdfsHelperUtil.streamToArray(input), fileExtName, null);
    }

    /**
     * 上传从文件
     */
    public String uploadSlave(String masterFileId, InputStream input, String filePrefixName, String fileName)
            throws Exception {
        String fileExtName = FilenameUtils.getExtension(fileName);
        return getClient().upload_file1(masterFileId, filePrefixName, FdfsHelperUtil.streamToArray(input), fileExtName, null);
    }

    /**
     * 上传从文件
     */
    public String uploadSlave(String masterFileId, byte[] input, String filePrefixName, String fileName)
            throws Exception {
        String fileExtName = FilenameUtils.getExtension(fileName);
        return getClient().upload_file1(masterFileId, filePrefixName, input, fileExtName, null);
    }

    /**
     * 上传主从文件
     */
    public MasterAndSlave uploadMasterAndSlave(String localMaster, String... localSlave) throws Exception {
        StorageClient1 client = getClient();
        File localMasterFile = new File(localMaster);
        File localSlaveFile = null;
        String fileName = localMasterFile.getName();
        String fileExtName = FilenameUtils.getExtension(fileName);
        MasterAndSlave ms = new MasterAndSlave(localSlave.length);
        // 上传主
        String master = client.upload_file1(fileName, fileExtName, null);
        ms.setMaster(master);
        // 上传从
        for (String s : localSlave) {
            localSlaveFile = new File(s);
            fileName = localSlaveFile.getName();
            fileExtName = FilenameUtils.getExtension(fileName);
            String filePrefixName = FilenameUtils.getExtension(fileName);
            try {
                ms.addSlave(client.upload_file1(master, filePrefixName, s, fileExtName, null));
            } catch (Exception e) {
                log.error("从文件上传失败", e);
            }
        }

        return ms;
    }

    /**
     * 上传主从文件
     */
    public MasterAndSlave uploadMasterAndSlave(String group, InputStream masterInput, String masterName,
                                               List<String> slaveNames, InputStream... slaveInputs) throws Exception {
        StorageClient1 client = getClient();
        String fileName = masterName;
        String fileExtName = FilenameUtils.getExtension(fileName);

        // 上传主
        String master = null;
        if (StringUtils.isEmpty(group)) {
            master = client.upload_file1(FdfsHelperUtil.streamToArray(masterInput), fileExtName, null);
        } else {
            master = client.upload_file1(group, FdfsHelperUtil.streamToArray(masterInput), fileExtName, null);
        }
        MasterAndSlave ms = new MasterAndSlave(slaveNames.size());
        ms.setMaster(master);
        // 上传从
        for (int i = 0, len = slaveNames.size(); i < len; i++) {
            fileName = slaveNames.get(i);
            fileExtName = FilenameUtils.getExtension(fileName);
            String filePrefixName = FilenameUtils.getPrefix(fileName);
            try {
                ms.addSlave(client.upload_file1(master, filePrefixName, FdfsHelperUtil.streamToArray(slaveInputs[i]), fileExtName,
                        null));
            } catch (Exception e) {
                log.warn(fileName + "从文件上传失败", e);
            }
        }
        return ms;
    }

    /**
     * 上传本地文件
     */
    public String upload(String file, String fileName) throws Exception {
        return getClient().upload_file1(file, FdfsHelperUtil.getExtension(fileName), null);
    }

    /**
     * 上传本地文件
     */
    public String upload(String file) throws Exception {
        return getClient().upload_file1(file, null, null);
    }

    /**
     * 下载文件
     */
    public void download(String fileId, OutputStream out) throws Exception {
        getClient().download_file1(fileId, new DownloadStream(out));

    }

    /**
     * 断点下载
     */
    public void download(String fileId, Long start, Long len, OutputStream out) throws Exception {
        getClient().download_file1(fileId, start, len, new DownloadStream(out));
    }

    /**
     * 下载文件
     */
    public int download(String fileId, String destFile) throws Exception {// 0成功
        int res = getClient().download_file1(fileId, destFile);
        if (0 == res) {
            throw new ServiceException("文件下载失败");
        }
        return res;
    }

    /**
     * 下载文件
     */
    public int download(String fileId, File destFile) throws Exception {// 0成功
        int res = getClient().download_file1(fileId, destFile.getAbsolutePath());
        if (0 == res) {
            throw new ServiceException("文件下载失败");
        }
        return res;
    }

    /**
     * 删除
     */
    public int delete(String fileId) throws Exception {// 0成功
        return getClient().delete_file1(fileId);
    }

    private static final int BUFFER_SIZE = 4 << 20;

    /**
     * 文件流转字节数组
     *
     * @param input
     * @return
     * @throws IOException
     */


    /**
     * 获取文件信息
     *
     * @param fileId
     * @return
     * @throws Exception
     */
    public FileInfo getFileInfo(String fileId) throws Exception {
        return getClient().get_file_info1(fileId);
    }


}
