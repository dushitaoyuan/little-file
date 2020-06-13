package com.taoyuanx.littlefile.server.utils;

import com.taoyuanx.littlefile.fdfshttp.core.dto.MasterAndSlave;
import com.taoyuanx.littlefile.server.ex.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.csource.fastdfs.*;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
     * @param input
     * @param fileName
     */
    public String upload(String group, InputStream input, String fileName) throws Exception {
        String fileExtName = FilenameUtils.getExtension(fileName);
        return getClient().upload_file1(group, input.available(), new UploadStream(input, input.available()), fileExtName, null);
    }



    /**
     * 断点续传文件api( upload,append,modify)
     */
    public String uploadAppendFile(String group, InputStream input, String fileName) throws Exception {
        String fileExtName = FilenameUtils.getExtension(fileName);
        return getClient().upload_appender_file1(group, input.available(), new UploadStream(input, input.available()), fileExtName, null);

    }

    public void append(InputStream input, String fileId) throws Exception {
        getClient().append_file1(fileId, input.available(), new UploadStream(input, input.available()));
    }

    public void modifyFile(InputStream input, Long offset, String fileId) throws Exception {
        getClient().modify_file1(fileId, offset, input.available(), new UploadStream(input, input.available()));

    }

    /**
     * 断点下载
     */
    public void download(String fileId, Long start, Long len, OutputStream out) throws Exception {
        try {
            getClient().download_file1(fileId, start, len, new DownloadStream(out));
        } finally {
            out.close();
        }
    }

    /**
     * 从文件上传
     */

    public String uploadSlave(String masterFileId, InputStream input, String filePrefixName, String fileName)
            throws Exception {
        String fileExtName = FilenameUtils.getExtension(fileName);
        return getClient().upload_file1(masterFileId, filePrefixName, input.available(), new UploadStream(input, input.available()), fileExtName, null);
    }


    /**
     * 主从文件上传
     */
    public MasterAndSlave uploadMasterAndSlave(String group, InputStream masterInput, String masterName,
                                               List<String> slaveNames, List<InputStream> slaveInputList) throws Exception {
        StorageClient1 client = getClient();
        String fileName = masterName;
        String fileExtName = FilenameUtils.getExtension(fileName);

        // 上传主
        String master = client.upload_file1(group, masterInput.available(), new UploadStream(masterInput, masterInput.available()), fileExtName, null);
        MasterAndSlave ms = new MasterAndSlave(slaveNames.size());
        ms.setMaster(master);
        // 上传从
        for (int i = 0, len = slaveNames.size(); i < len; i++) {
            InputStream slaveInput = slaveInputList.get(i);
            fileName = slaveNames.get(i);
            fileExtName = FilenameUtils.getExtension(fileName);
            String filePrefixName = FilenameUtils.getPrefix(fileName);
            try {
                ms.addSlave(client.upload_file1(masterName, filePrefixName, slaveInput.available(), new UploadStream(slaveInput, slaveInput.available()), fileExtName, null));
            } catch (Exception e) {
                log.warn(fileName + "从文件上传失败", e);
            }
        }
        return ms;
    }

    public MasterAndSlave uploadMasterAndSlave(String group, String localMaster, String... localSlave) throws Exception {

        List<String> slaveNameList = Arrays.stream(localSlave).map(FdfsHelperUtil::getFileName).collect(Collectors.toList());
        List<InputStream> slaveInputList = Arrays.stream(localSlave).map(name -> {
            try {
                return new FileInputStream(name);
            } catch (Exception e) {
                throw new ServiceException("文件上传异常");
            }
        }).collect(Collectors.toList());
        return uploadMasterAndSlave(group, new FileInputStream(localMaster), FdfsHelperUtil.getFileName(localMaster),
                slaveNameList,
                slaveInputList
        );
    }

    /**
     * 下载文件
     */
    public void download(String fileId, OutputStream out) throws Exception {
        try {
            getClient().download_file1(fileId, new DownloadStream(out));
        } finally {
            out.close();
        }
    }


    /**
     * 删除
     */
    public int delete(String fileId) throws Exception {// 0成功
        return getClient().delete_file1(fileId);
    }


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
