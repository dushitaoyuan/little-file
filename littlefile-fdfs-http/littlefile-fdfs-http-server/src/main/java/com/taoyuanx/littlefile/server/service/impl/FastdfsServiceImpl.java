package com.taoyuanx.littlefile.server.service.impl;

import com.taoyuanx.littlefile.fdfshttp.core.dto.FileInfo;
import com.taoyuanx.littlefile.fdfshttp.core.dto.MasterAndSlave;
import com.taoyuanx.littlefile.server.config.FileProperties;
import com.taoyuanx.littlefile.server.dto.ImageWH;
import com.taoyuanx.littlefile.server.ex.ServiceException;
import com.taoyuanx.littlefile.server.service.FastdfsService;
import com.taoyuanx.littlefile.server.service.FileValidateService;
import com.taoyuanx.littlefile.server.utils.CodeUtil;
import com.taoyuanx.littlefile.server.utils.FdfsFileUtil;
import com.taoyuanx.littlefile.server.utils.FdfsHelperUtil;
import com.taoyuanx.littlefile.server.utils.FilenameUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FastdfsServiceImpl implements FastdfsService {
    @Autowired
    private FileValidateService fileValidateService;
    @Autowired
    FdfsFileUtil fdfsFileUtil;

    @Autowired
    FileProperties fileProperties;


    @Override
    public String uploadFile(MultipartFile file) throws ServiceException {
        try {
            fileValidateService.validateFile(file);
            long fileSize = file.getSize();
            if (fileSize <= 0) {
                throw new ServiceException("file is null.");
            }
            String path = fdfsFileUtil.upload(null, file.getInputStream(), file.getOriginalFilename());
            if (path == null) {
                throw new ServiceException("upload error.");
            }
            return path;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传异常-->", e);
            throw new ServiceException("文件上传异常");
        }
    }

    @Override
    public String uploadAppendFile(MultipartFile file) throws ServiceException {
        try {
            long fileSize = file.getSize();
            if (fileSize <= 0) {
                throw new ServiceException("file is null.");
            }
            return fdfsFileUtil.uploadAppendFile(null, file.getInputStream(), file.getOriginalFilename());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传异常-->", e);
            throw new ServiceException("文件上传异常");
        }
    }

    @Override
    public void appendFile(String fileId, MultipartFile file) throws ServiceException {
        try {
            long fileSize = file.getSize();
            if (fileSize <= 0) {
                throw new ServiceException("file is null.");
            }
            if (StringUtils.isEmpty(fileId)) {
                throw new ServiceException("fileId is null.");
            }
            fdfsFileUtil.append(file.getInputStream(), fileId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传异常-->", e);
            throw new ServiceException("文件上传异常");
        }
    }

    @Override
    public void modifyFile(MultipartFile file, Long offset, String fileId) {
        try {
            long fileSize = file.getSize();
            if (fileSize <= 0) {
                throw new ServiceException("file is null.");
            }
            if (Objects.isNull(offset)) {
                throw new ServiceException("fileId is null.");
            }
            fdfsFileUtil.modifyFile(file.getInputStream(), offset, fileId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传异常-->", e);
            throw new ServiceException("文件上传异常");
        }
    }

    @Override
    public String uploadSlaveFile(String masterFileId, MultipartFile file) throws ServiceException {
        try {
            fileValidateService.validateFile(file);
            long fileSize = file.getSize();
            if (fileSize <= 0) {
                throw new ServiceException("file is null.");
            }
            String fileName = file.getOriginalFilename();
            //随机生成从文件前缀,防止重名异常
            String filePrefixName = FilenameUtils.getPrefixRandom(fileName);
            String path = fdfsFileUtil.uploadSlave(masterFileId, file.getInputStream(), filePrefixName, fileName);
            if (path == null) {
                throw new ServiceException("slave upload error.");
            }
            return path;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件从上传异常-->", e);
            throw new ServiceException("文件上传异常");
        }
    }

    @Override
    public String uploadSlaveFile(String masterFilename, String prefixName, MultipartFile file) throws ServiceException {
        try {
            fileValidateService.validateFile(file);
            long fileSize = file.getSize();
            if (fileSize <= 0) {
                throw new ServiceException("file is null.");
            }
            prefixName = prefixName + "_" + FilenameUtils.generateShortUuid();
            String path = fdfsFileUtil.uploadSlave(masterFilename, file.getInputStream(), prefixName, file.getOriginalFilename());
            if (path == null) {
                throw new ServiceException("slave upload error.");
            }
            return path;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件从上传异常-->", e);
            throw new ServiceException("文件上传异常");
        }
    }


    @Override
    public MasterAndSlave uploadImageAndThumb(String cutSize, MultipartFile file) throws ServiceException {
        try {
            fileValidateService.validateFile(file);
            long fileSize = file.getSize();
            if (fileSize <= 0) {
                throw new ServiceException("file is null.");
            }
            if (StringUtils.isEmpty(cutSize)) {
                throw new ServiceException("cutSize is null.");
            }
            String ext = FilenameUtils.getExtension(file.getOriginalFilename());
            File sourceFile = new File(fileProperties.getFileCacheDir(), CodeUtil.getUUID());
            file.transferTo(sourceFile);
            /**
             * 生成缩略图
             */
            List<ImageWH> smallImageList = loadCutSize(cutSize);
            List<String> slaveNameList = new ArrayList<>();
            List<File> slaveFileList = new ArrayList<>();
            List<InputStream> slaveInputList = new ArrayList<>();
            smallImageList.stream().forEach(imageWH -> {
                try {
                    String smallImageName = String.format("%dx%d", imageWH.getW(), imageWH.getH()) + "." + ext;
                    slaveNameList.add(smallImageName);
                    File slaveFile = new File(FilenameUtils.getFileNameRandom(smallImageName));
                    Thumbnails.of(sourceFile).size(imageWH.getW(), imageWH.getH()).toOutputStream(new FileOutputStream(slaveFile));
                    slaveFileList.add(slaveFile);
                    slaveInputList.add(new FileInputStream(slaveFile));
                } catch (Exception e) {
                    log.warn("从文件上传异常", e);
                    throw new ServiceException("图片缩略生成异常");
                }
            });
            MasterAndSlave uploadMasterAndSlave = fdfsFileUtil.uploadMasterAndSlave(null, new FileInputStream(sourceFile),
                    file.getOriginalFilename(),
                    slaveNameList, slaveInputList);
            //删除临时
            FileUtils.deleteQuietly(sourceFile);
            slaveFileList.forEach(FileUtils::deleteQuietly);
            return uploadMasterAndSlave;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("上传图片异常-->", e);
            throw new ServiceException("文件上传异常");
        }
    }

    @Override
    public boolean removeFile(String fileId) throws ServiceException {
        try {
            if (StringUtils.isEmpty(fileId)) {
                throw new ServiceException("fileId is null");
            }
            fdfsFileUtil.delete(fileId);
            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除异常-->", e);
            throw new ServiceException("删除异常");
        }
    }

    @Override
    public void download(String fileId, OutputStream outputStream) throws ServiceException {
        try {
            if (StringUtils.isEmpty(fileId)) {
                throw new ServiceException("fileId is null");
            }
            fdfsFileUtil.download(fileId, outputStream);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("下载文件异常-->", e);
            throw new ServiceException("下载文件异常");
        }
    }

    @Override
    public void download(String fileId, Long start, Long len, OutputStream outputStream) throws ServiceException {
        try {
            if (StringUtils.isEmpty(fileId)) {
                throw new ServiceException("fileId is null");
            }
            fdfsFileUtil.download(fileId, start, len, outputStream);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("下载文件异常-->", e);
            throw new ServiceException("下载文件异常");
        }
    }

    @Override
    public FileInfo getFileInfo(String fileId) throws ServiceException {

        try {
            if (StringUtils.isEmpty(fileId)) {
                throw new ServiceException("fileId is null");
            }
            org.csource.fastdfs.FileInfo fileInfo = fdfsFileUtil.getFileInfo(fileId);
            if (null == fileInfo) {
                throw new ServiceException("文件不存在");
            }
            FileInfo info = new FileInfo();
            info.setCrc32(fileInfo.getCrc32());
            info.setCreate_timestamp(fileInfo.getCreateTimestamp().getTime());
            info.setFile_size(fileInfo.getFileSize());
            return info;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件信息-->", e);
            throw new ServiceException("文件信息获取异常");
        }
    }

    private List<ImageWH> loadCutSize(String cutSize) throws ServiceException {
        if (StringUtils.hasText(cutSize)) {
            return Arrays.stream(cutSize.split(",")).map(singleSize -> {
                try {
                    String size[] = singleSize.split("x");
                    int width = Integer.parseInt(size[0]);
                    int height = Integer.parseInt(size[1]);
                    return new ImageWH(width, height);
                } catch (Exception e) {
                    log.warn("{} cutSize is error ", singleSize);
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;

    }



}
