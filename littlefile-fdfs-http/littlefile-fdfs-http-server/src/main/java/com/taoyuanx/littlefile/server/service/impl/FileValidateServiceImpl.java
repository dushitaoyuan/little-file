package com.taoyuanx.littlefile.server.service.impl;

import com.taoyuanx.littlefile.server.ex.ServiceException;
import org.springframework.web.multipart.MultipartFile;
import com.taoyuanx.littlefile.server.service.FileValidateService;
import com.taoyuanx.littlefile.server.utils.FilenameUtils;

import java.util.Set;


/**
 * @author dushitaoyuan
 * 类型校验
 */
public class FileValidateServiceImpl implements FileValidateService {
    private Set<String> exts;
    
    public  FileValidateServiceImpl (Set<String> exts){
    	this.exts=exts;
    }
    @Override
    public void validateFile(MultipartFile file) throws ServiceException {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        if(!exts.contains(ext)){
            throw new ServiceException("file type error.");
        }
    }

	
}
