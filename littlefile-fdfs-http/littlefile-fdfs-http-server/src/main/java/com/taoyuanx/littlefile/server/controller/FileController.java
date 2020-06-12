package com.taoyuanx.littlefile.server.controller;

import com.taoyuanx.littlefile.fdfshttp.core.dto.FileInfo;
import com.taoyuanx.littlefile.fdfshttp.core.dto.MasterAndSlave;
import com.taoyuanx.littlefile.server.anno.NeedToken;
import com.taoyuanx.littlefile.server.config.FileProperties;
import com.taoyuanx.littlefile.server.dto.PreviewType;
import com.taoyuanx.littlefile.server.dto.Result;
import com.taoyuanx.littlefile.server.dto.ResultBuilder;
import com.taoyuanx.littlefile.server.ex.ServiceException;
import com.taoyuanx.littlefile.server.service.FastdfsService;
import com.taoyuanx.littlefile.server.utils.ByteRangeUtil;
import com.taoyuanx.littlefile.server.utils.PdfToImage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;


@RestController
@RequestMapping("/file")
@Api(value = "文件通用服务接口")
@NeedToken
public class FileController {
    Logger LOG = LoggerFactory.getLogger(FileController.class);

    @Autowired
    FastdfsService fastdfsService;
    @Autowired
    FileProperties fileProperties;

    private static final int BUFFER_SIZE = 4 << 20;

    @ApiOperation(value = "上传文件", notes = "上传文件")
    @PostMapping(value = "/upload")
    public Result uploadFile(
            @ApiParam(value = "文件", required = true) @RequestParam("file") MultipartFile file)
            throws ServiceException {
        return ResultBuilder.successResult(fastdfsService.uploadFile(file));
    }

    @ApiOperation(value = "上传从文件", notes = "上传从文件,上传从文件时,多个从文件名称一定要不同,如果不指定,服务端在文件名基础上随机生成")
    @PostMapping(value = "/uploadSlave")
    public Result uploadSlaveFile(
            @ApiParam(value = "从文件流,name=file", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "主文件id", required = false) @RequestParam("masterFileId") String masterFileId,
            @ApiParam(value = "前缀名称", required = false) @RequestParam(value = "prefixName", required = false) String prefixName)
            throws ServiceException {
        if (StringUtils.isEmpty(prefixName)) {
            return ResultBuilder.successResult(fastdfsService.uploadSlaveFile(masterFileId, file));
        }
        return ResultBuilder.successResult(fastdfsService.uploadSlaveFile(masterFileId, prefixName, file));
    }

    @ApiOperation(value = "断点上传文件", notes = "上传文件")
    @PostMapping(value = "/upload/range")
    public Result uploadRangeFile(
            @ApiParam(value = "文件流,name=file", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "文件fileId", required = false) @RequestParam(value = "fileId", required = false) String fileId,
            @ApiParam(value = "文件偏移量", required = false) @RequestParam(value = "offset", required = false) Long offset)
            throws ServiceException {
        if (StringUtils.isEmpty(fileId)) {
            return ResultBuilder.successResult(fastdfsService.uploadAppendFile(file));
        } else if (Objects.nonNull(offset)) {
            fastdfsService.modifyFile(file, offset, fileId);
        } else {
            fastdfsService.appendFile(fileId, file);
        }
        return ResultBuilder.success();
    }

    @ApiOperation(value = "删除文件", notes = "删除文件")
    @DeleteMapping(value = "/removeFile")
    public Result removeFile(@ApiParam(value = "文件fileId", required = true) @RequestParam("fileId") String fileId)
            throws ServiceException {
        fastdfsService.removeFile(fileId);
        return ResultBuilder.success();
    }

    @ApiOperation(value = "上传图片", notes = "上传图片,可自由选择是否生成预览图,如果不传递cutSize,则不生成")
    @PostMapping(value = "/image/upload")
    public Result uploadImage(

            @ApiParam(value = "文件流,name=file", required = true) @RequestParam(value = "file") MultipartFile file,


            @ApiParam(value = "裁剪尺寸,如:20x20,30x30,100x100", required = false) @RequestParam(value = "cutSize", required = false) String cutSize)
            throws ServiceException {
        if (StringUtils.isEmpty(cutSize)) {
            MasterAndSlave result = new MasterAndSlave();
            result.setMaster(fastdfsService.uploadFile(file));
            return ResultBuilder.success(result);
        }
        return ResultBuilder.success(fastdfsService.uploadImageAndThumb(cutSize, file));
    }

    @ApiOperation(value = "文件下载", notes = "文件下载操作")
    @GetMapping(value = "download")
    public void download(
            @ApiParam(value = "下载fileId", required = true) @RequestParam(value = "fileId", required = true) String fileId,
            HttpServletResponse response, HttpServletRequest request) throws Exception {
        File tempFile = new File(fileProperties.getFileCacheDir(), fileId);
        if (false == tempFile.getParentFile().exists()) {
            tempFile.getParentFile().mkdirs();
        }
        if (!tempFile.exists()) {
            fastdfsService.download(fileId, new FileOutputStream(tempFile));
        }
        response.setHeader("Content-type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;fileName=" + tempFile.getName());
        transferToOutStream(tempFile, response.getOutputStream());
    }

    @ApiOperation(value = "文件断点下载", notes = "文件下载操作")
    @GetMapping(value = "download/range")
    public void byteRangedownload(
            @ApiParam(value = "下载fileId", required = true) @RequestParam(value = "fileId", required = true) String fileId,
            @ApiParam(value = "文件大小", required = false) @RequestParam(value = "fileSize", required = false) Long fileSize,
            HttpServletResponse response, HttpServletRequest request) throws Exception {
        String range = request.getHeader("Range");
        if (range.contains(",")) {
            throw new ServiceException("断点下载, header Range: [" + range + "]不支持");
        }
        Long start, end, len;
        response.setHeader("Accept-Ranges", "bytes");
        String tempRange = range.replaceFirst("bytes=", "");
        String[] split = tempRange.split("-");
        if (ByteRangeUtil.RANGE_FMT_START_END.matcher(tempRange).matches()) {
            start = Long.parseLong(split[0]);
            end = Long.parseLong(split[1]);
        } else if (ByteRangeUtil.RANGE_FMT_START_TILL_END.matcher(tempRange).matches()) {
            fileSize = getFileSize(fileSize, fileId);
            start = Long.parseLong(split[0]);
            end = fileSize - 1;
        } else if (ByteRangeUtil.RANGE_FMT_BYTES_END.matcher(tempRange).matches()) {
            fileSize = getFileSize(fileSize, fileId);
            start = fileSize - Long.parseLong(split[0]);
            end = fileSize - 1;
        } else {
            throw new ServiceException("断点下载, header Range: [" + range + "]不支持");
        }
        len = end - start + 1;
        response.setHeader("Content-type", "application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment;fileName=" + URLEncoder.encode(FilenameUtils.getName(fileId), "UTF-8"));
        //格式 bytes %s-%s/%s
        response.addHeader(" Content-Range", String.format(ByteRangeUtil.CONTENTRANGE_FMT, start, end, fileSize));
        response.addHeader("Content-Length", "" + len);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        fastdfsService.download(fileId, start, len, response.getOutputStream());
        IOUtils.closeQuietly(response.getOutputStream());
    }


    @ApiOperation(value = "获取文件信息", notes = "获取文件信息")
    @GetMapping(value = "info")
    public Result getFileInfo(
            @ApiParam(value = "下载fileId", required = true) @RequestParam(value = "fileId", required = true) String fileId)
            throws ServiceException {
        return ResultBuilder.success(fastdfsService.getFileInfo(fileId));
    }

    @ApiOperation(value = "预览文件", notes = "直接向页面输出流,可直接预览图片,pdf等,后期可支持多文件预览,office,pdf-图片,"
            + "压缩包在线预览,当然这不是本项目的重点所在,预览url:http://xx:port/file/preview?fileId=group1/M00/00/01/wKhbyVqo4S2AZ7swAAJWdlvYGRY480.pdf")
    @GetMapping(value = "preview")
    public void preview(
            @ApiParam(value = "预览文件fileId", required = true) @RequestParam(value = "fileId", required = true) String fileId,
            @ApiParam(value = "预览类型,如果不输入类型,则直接返回文件流,经测试pc端,可直接使用iframe预览pdf,img标签预览图片,移动端浏览器不支持,需将pdf转为图片", required = false) @RequestParam(value = "previewType", required = false) Integer previewType,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        File tempFile = new File(fileProperties.getFileCacheDir(), fileId);
        if (false == tempFile.getParentFile().exists()) {
            tempFile.getParentFile().mkdirs();
        }
        if (!tempFile.exists()) {
            LOG.debug("开始下载文件[{}]", fileId);
            fastdfsService.download(fileId, new FileOutputStream(tempFile));
        }
        if (StringUtils.isEmpty(previewType)) {
            // 查看
            response.setContentType(request.getServletContext().getMimeType(tempFile.getName()));
            transferToOutStream(tempFile, response.getOutputStream());
            return;
        }
        /**
         * pdf 渲染图片
         */
        switch (PreviewType.getByCode(previewType)) {
            case PDF_TO_IMG: {
                File imgFile = new File(tempFile.getAbsolutePath().replace(".pdf", ".jpg"));
                if (!imgFile.exists()) {
                    PdfToImage.pdfToOneImage(tempFile, imgFile);
                }
                response.setContentType(request.getServletContext().getMimeType(imgFile.getName()));
                transferToOutStream(tempFile, response.getOutputStream());
            }
            return;
            default:
                break;
        }


    }

    private void transferToOutStream(File tempFile, OutputStream outputStream) throws Exception {
        RandomAccessFile dest = new RandomAccessFile(tempFile, "r");
        FileChannel channel = dest.getChannel();
        try {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            int len = 0;
            while ((len = channel.read(buffer)) > 0) {
                buffer.flip();
                outputStream.write(buffer.array(), 0, len);
                buffer.clear();
            }
        } finally {
            IOUtils.closeQuietly(dest);
            IOUtils.closeQuietly(channel);
        }

    }

    private Long getFileSize(Long fileSize, String fileId) {
        if (Objects.isNull(fileSize)) {
            return fileSize;
        }
        FileInfo fileInfo = fastdfsService.getFileInfo(fileId);
        if (Objects.nonNull(fileInfo)) {
            return fileInfo.getFile_size();
        }
        throw new ServiceException("断点下载, 无法计算文件大小");
    }

    @ApiOperation(value = "", notes = "心跳监测")
    @GetMapping
    public void hello() {
    }


}
