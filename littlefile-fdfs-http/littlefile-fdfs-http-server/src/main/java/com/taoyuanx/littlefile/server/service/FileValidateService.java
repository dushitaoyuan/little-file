package com.taoyuanx.littlefile.server.service;

import com.taoyuanx.littlefile.server.ex.ServiceException;
import org.springframework.web.multipart.MultipartFile;



/**
 * @author 都市桃源
 * 2019年3月5日 下午5:34:36
 * @description  文件校验
 *
*/
public interface FileValidateService {



    void validateFile(MultipartFile file) throws ServiceException;
    
}
