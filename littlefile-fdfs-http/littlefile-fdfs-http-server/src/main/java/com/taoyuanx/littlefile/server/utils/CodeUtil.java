package com.taoyuanx.littlefile.server.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import com.taoyuanx.littlefile.server.ex.ServiceException;
import org.springframework.util.Assert;


/**
 * @author 都市桃源
 * 工具信息
 */
public class CodeUtil {


    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }


}
