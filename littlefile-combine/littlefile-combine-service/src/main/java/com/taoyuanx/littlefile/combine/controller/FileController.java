package com.taoyuanx.littlefile.combine.controller;

import com.taoyuanx.littlefile.combine.config.FileProperties;
import com.taoyuanx.littlefile.combine.core.FileStoreTypeEnum;
import com.taoyuanx.littlefile.combine.core.dto.FileInfoDTO;
import com.taoyuanx.littlefile.combine.core.dto.FileSignDTO;
import com.taoyuanx.littlefile.combine.core.sign.FileTypeEnum;
import com.taoyuanx.littlefile.combine.core.sign.SimpleFileManager;
import com.taoyuanx.littlefile.combine.core.store.FileStoreService;
import com.taoyuanx.littlefile.combine.core.utils.JSONUtil;
import com.taoyuanx.littlefile.combine.core.utils.Utils;
import com.taoyuanx.littlefile.combine.dto.MapResultBuilder;
import com.taoyuanx.littlefile.combine.dto.Result;
import com.taoyuanx.littlefile.combine.dto.ResultBuilder;
import com.taoyuanx.littlefile.combine.entity.FileEntity;
import com.taoyuanx.littlefile.combine.ex.ServiceException;
import com.taoyuanx.littlefile.combine.service.FileService;
import com.taoyuanx.littlefile.combine.utils.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

/**
 * @author dushitaoyuan
 * @date 2020/02/19
 */
@Controller
@RequestMapping("file")
@Slf4j
public class FileController {
    @Autowired
    FileService fileService;
    @Autowired
    FileStoreService fileStoreService;


    @Autowired
    SimpleFileManager simpleFileManager;

    @Autowired
    FileProperties fileProperties;


    /**
     * 外部系统文件上传
     * 也可白名单增强权限控制
     */

    @RequestMapping(method = RequestMethod.POST, value = "")
    @ResponseBody
    public Result outFile(@RequestParam("file") MultipartFile file,
                          Integer fileType,
                          @RequestParam("fileInfo") String fileJsonInfo,
                          HttpServletRequest request) throws Exception {
        if (Objects.isNull(fileType)) {
            throw new ServiceException("文件类型不可为空");
        }
        if (Objects.isNull(file)) {
            throw new ServiceException("文件不可为空");
        }

        if (Objects.nonNull(fileProperties.getTypeSet())) {
            String extension = Utils.getExtension(file.getOriginalFilename());
            if (!fileProperties.getTypeSet().contains(extension)) {
                throw new ServiceException(401, "文件类型[" + extension + "]禁止上传");
            }
        }
        String token = RequestUtil.getRequestValue(request, "token");
        if (StringUtils.isEmpty(token) || !token.equals(fileProperties.getToken())) {
            throw new ServiceException(401, "请求非法");
        }
        String fileId = fileStoreService.store(file.getInputStream(), file.getOriginalFilename());
        FileStoreTypeEnum protocol = FileStoreTypeEnum.protocol(fileStoreService.getStoreProtocol());
        FileEntity fileEntity = new FileEntity();
        fileEntity.setCreateTime(new Date());
        fileEntity.setFileType(fileType);
        fileEntity.setPath(fileId);
        fileEntity.setStoreType(protocol.code);
        fileEntity.setFileInfo(fileInfo(file, fileJsonInfo));
        fileService.save(fileEntity);
        FileSignDTO fileSignDTO = new FileSignDTO();
        fileSignDTO.setId(fileEntity.getId());
        fileSignDTO.setPath(fileId);
        fileSignDTO.setType(fileType);
        /**
         * 返回签名后的文件
         */
        String signFileId = simpleFileManager.signFile(fileSignDTO);
        return ResultBuilder.successResult(signFileId);
    }

    /**
     * 内部系统文件上传
     * 也可白名单增强权限控制
     */
    @PostMapping("inner")
    @ResponseBody
    public Result innerFile(@RequestParam("file") MultipartFile file, Integer fileType) throws Exception {
        if (Objects.isNull(fileType)) {
            throw new ServiceException("文件类型不可为空");
        }
        if (Objects.isNull(file)) {
            throw new ServiceException("文件不可为空");
        }
        if (Objects.nonNull(fileProperties.getTypeSet())) {
            String extension = Utils.getExtension(file.getOriginalFilename());
            if (!fileProperties.getTypeSet().contains(extension)) {
                throw new ServiceException(401, "文件类型[" + extension + "]禁止上传");
            }
        }
        //内部系统访问
        String fileId = fileStoreService.store(file.getInputStream(), file.getOriginalFilename());
        String endpointPath = simpleFileManager.getFullHttpUrl(simpleFileManager.newFileSignDTO(fileId, FileTypeEnum.type(fileType)));
        Map<String, Object> innerFileResult = MapResultBuilder.newBuilder(2).put("fileId", fileId)
                .put("endpointPath", endpointPath).build();
        return ResultBuilder.success(innerFileResult);
    }

    /**
     * 文件删除(内部)
     */
    @DeleteMapping("")
    @ResponseBody
    public Result deleteFile(String fileId, Long id) throws Exception {
        if (Objects.isNull(fileId) || Objects.isNull(id)) {
            throw new ServiceException("参数不可为空");
        }
        FileEntity fileEntity = null;
        if (Objects.nonNull(id)) {
            fileEntity = fileService.getById(id);

        }
        if (Objects.isNull(fileEntity) && Objects.nonNull(fileId)) {
            fileEntity = fileService.getByPath(fileId);
        }
        if (Objects.nonNull(fileEntity)) {
            fileService.deleteById(fileEntity.getId());
            fileStoreService.delete(fileEntity.getPath());
        }
        return ResultBuilder.success();
    }

    /**
     * type 1 查看 2 下载
     * 文件处理(下载或在线预览(直接返回流信息,亦可转换后返回流))
     */
    @GetMapping("")
    public void file(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //取出签名参数
        String signUrl = RequestUtil.getRequestValue(request, "file");
        if (StringUtils.isEmpty(signUrl)) {
            throw new ServiceException("未授权url");
        }
        FileSignDTO fileSignDTO = simpleFileManager.parseFile(signUrl);
        if (!simpleFileManager.verify(fileSignDTO)) {
            throw new ServiceException("未授权url");
        }
        String type = RequestUtil.getRequestValue(request, "type"), filePath = null;
        try {
            type = type == null ? "2" : type;
            filePath = fileSignDTO.getPath();
            switch (type) {
                case "1":
                    response.setContentType(request.getServletContext().getMimeType(filePath));
                    break;
                case "2": {
                    response.setHeader("Content-type", "application/octet-stream");
                    response.setHeader("Content-Disposition",
                            "attachment;fileName=" + URLEncoder.encode(Utils.getFileName(filePath), "UTF-8"));
                }
                break;
            }
            //gzip 压缩
            if (fileProperties.isGzip()) {
                response.setHeader("Content-Encoding", "gzip");
                GZIPOutputStream gzip = new GZIPOutputStream(response.getOutputStream());
                handle(gzip, filePath);
                gzip.close();
            } else {
                ServletOutputStream out = response.getOutputStream();
                handle(out, filePath);
                out.flush();
            }
        } catch (Exception e) {
            handleError(response, request, e, filePath);
        }
    }


    private void handle(OutputStream out, String fileId) throws Exception {
        fileStoreService.downLoad(fileId, out);
    }

    private void handleError(HttpServletResponse resp, HttpServletRequest req, Exception e, String filePath) {
        try {
            log.error("处理文件[{}]异常{}", filePath, e);
            resp.getWriter().println("file " + filePath + " error" + e.getMessage());
        } catch (IOException e1) {
        }
    }

    private String fileInfo(MultipartFile file, String fileInfo) {
        if (StringUtils.hasText(fileInfo)) {
            return fileInfo;
        } else {
            FileInfoDTO defaultFileInfo = new FileInfoDTO();
            defaultFileInfo.setByteSize(file.getSize());
            defaultFileInfo.setFileName(file.getOriginalFilename());
            return JSONUtil.toJsonString(defaultFileInfo);
        }


    }


}
