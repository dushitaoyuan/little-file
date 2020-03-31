package com.taoyuanx.littlefile.server.controller;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taoyuanx.littlefile.server.config.FileProperties;
import com.taoyuanx.littlefile.server.dto.MasterAndSlave;
import com.taoyuanx.littlefile.server.dto.PreviewType;
import com.taoyuanx.littlefile.server.dto.FileInfo;
import com.taoyuanx.littlefile.server.ex.ServiceException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import com.taoyuanx.littlefile.server.service.FastdfsService;
import com.taoyuanx.littlefile.server.utils.CodeUtil;
import com.taoyuanx.littlefile.server.utils.PdfToImage;

/**
 * create by lorne on 2017/9/26
 */
@RestController
@RequestMapping("/file")
@Api(value = "文件通用服务接口")
public class FileController {

    @Autowired
    FastdfsService fastdfsService;
    @Autowired
    FileProperties fileProperties;

    @ApiOperation(value = "上传文件", notes = "上传文件")
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadFile(

            @ApiParam(value = "文件流,name=file", required = true) @RequestParam("file") MultipartFile file)
            throws ServiceException {
        return fastdfsService.uploadFile(file);
    }

    @ApiOperation(value = "上传从文件", notes = "上传从文件,上传从文件时,多个从文件名称一定要不同,如果不指定,服务端在文件名基础上随机生成")
    @RequestMapping(value = "/uploadSlave", method = RequestMethod.POST)
    public String uploadSlaveFile(
            @ApiParam(value = "从文件流,name=file", required = true) @RequestParam("file") MultipartFile file,

            @ApiParam(value = "主文件名称", required = false) @RequestParam("masterFileId") String masterFileId,
            @ApiParam(value = "前缀名称", required = false) @RequestParam(value = "prefixName", required = false) String prefixName)
            throws ServiceException {
        if (StringUtils.isEmpty(prefixName)) {
            return fastdfsService.uploadSlaveFile(masterFileId, file);
        }
        return fastdfsService.uploadSlaveFile(masterFileId, prefixName, file);
    }

    @ApiOperation(value = "删除文件", notes = "删除文件")
    @RequestMapping(value = "/removeFile", method = RequestMethod.DELETE)
    public boolean removeFile(@ApiParam(value = "文件名称", required = true) @RequestParam("fileId") String fileId)
            throws ServiceException {
        return fastdfsService.removeFile(fileId);
    }

    @ApiOperation(value = "上传图片", notes = "上传图片,可自由选择是否生成预览图,如果不传递cutSize,则不生成")
    @RequestMapping(value = "/image/upload", method = RequestMethod.POST)
    public MasterAndSlave uploadImage(

            @ApiParam(value = "文件流,name=file", required = true) @RequestParam(value = "file") MultipartFile file,


            @ApiParam(value = "裁剪尺寸（数组类型）如:20x20,30x30,100x100", required = false) @RequestParam(value = "cutSize", required = false) String cutSize)
            throws ServiceException {
        if (StringUtils.isEmpty(cutSize)) {
            MasterAndSlave result = new MasterAndSlave();
            result.setMaster(fastdfsService.uploadFile(file));
            return result;
        }
        return fastdfsService.uploadImageAndThumb(cutSize, file);
    }

    @ApiOperation(value = "文件下载", notes = "文件下载操作")
    @RequestMapping(value = "download", method = RequestMethod.GET)
    public void download(
            @ApiParam(value = "下载fileId", required = true) @RequestParam(value = "fileId", required = true) String fileId,
            HttpServletResponse resp, HttpServletRequest req) throws ServiceException {
        File tempFile = new File(fileProperties.getFileCacheDir(), fileId);
        if (false == tempFile.getParentFile().exists()) {
            tempFile.getParentFile().mkdirs();
        }
        if (!tempFile.exists()) {
            fastdfsService.download(fileId, tempFile.getAbsolutePath());
        }
        try {
            resp.setContentType(req.getServletContext().getMimeType(tempFile.getName()));
            resp.setHeader("Content-type", "application/octet-stream");
            resp.setHeader("Content-Disposition", "attachment;fileName=" + CodeUtil.getUUID() + tempFile.getName());
            resp.getOutputStream().write(FileUtils.readFileToByteArray(tempFile));
        } catch (Exception e) {
            throw new ServiceException("文件下载失败", e);
        }
    }

    @ApiOperation(value = "获取文件信息", notes = "获取文件信息")
    @RequestMapping(value = "getFileInfo", method = RequestMethod.GET)
    public FileInfo getFileInfo(
            @ApiParam(value = "下载fileId", required = true) @RequestParam(value = "fileId", required = true) String fileId)
            throws ServiceException {
        return fastdfsService.getFileInfo(fileId);
    }

    @ApiOperation(value = "预览文件", notes = "直接向页面输出流,可直接预览图片,pdf等,后期可支持多文件预览,office,pdf-图片,"
            + "压缩包在线预览,当然这不是本项目的重点所在,预览url:http://xx:port/file/preview?fileId=group1/M00/00/01/wKhbyVqo4S2AZ7swAAJWdlvYGRY480.pdf")
    @RequestMapping(value = "preview", method = RequestMethod.GET)
    public void preview(
            @ApiParam(value = "预览文件fileId", required = true) @RequestParam(value = "fileId", required = true) String fileId,
            @ApiParam(value = "预览类型,如果不输入类型,则直接返回文件流,经测试pc端,可直接使用iframe预览pdf,img标签预览图片,移动端浏览器不支持,需将pdf转为图片", required = false) @RequestParam(value = "previewType", required = false) Integer previewType,
            HttpServletRequest req, HttpServletResponse resp) throws ServiceException {
        File tempFile = new File(fileProperties.getFileCacheDir(), fileId);

        if (false == tempFile.getParentFile().exists()) {
            tempFile.getParentFile().mkdirs();
        }
        if (!tempFile.exists()) {
            fastdfsService.download(fileId, tempFile.getAbsolutePath());
        }
        try {
            if (StringUtils.isEmpty(previewType)) {
                // 查看
                resp.setContentType(req.getServletContext().getMimeType(tempFile.getName()));
                resp.getOutputStream().write(FileUtils.readFileToByteArray(tempFile));
                return;
            }
            switch (PreviewType.getByCode(previewType)) {
                case PDF_TO_IMG: {
                    File imgFile = new File(tempFile.getAbsolutePath().replace(".pdf", ".jpg"));
                    if (!imgFile.exists()) {
                        PdfToImage.pdfToOneImage(tempFile, imgFile);
                    }
                    resp.setContentType(req.getServletContext().getMimeType(imgFile.getName()));
                    resp.getOutputStream().write(FileUtils.readFileToByteArray(imgFile));
                }
                return;
                default:
                    break;
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }

    }

}
