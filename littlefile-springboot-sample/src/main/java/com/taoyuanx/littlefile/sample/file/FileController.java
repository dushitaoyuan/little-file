package com.taoyuanx.littlefile.sample.file;

import com.taoyuanx.littlefile.web.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dushitaoyuan
 * @desc 文件暴露接口
 * @date 2019/7/4
 */
@RequestMapping("file")
@RestController
public class FileController {
    @Autowired
    FileHandler fileHandler;

    @RequestMapping(method = RequestMethod.GET, value = "")
    public void handleFile(HttpServletRequest req, HttpServletResponse resp) {
        fileHandler.handleFile(resp, req);
    }
}
