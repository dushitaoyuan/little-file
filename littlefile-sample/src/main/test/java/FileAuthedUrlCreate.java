import com.taoyuanx.littlefile.config.LittleFilConfig;
import com.taoyuanx.littlefile.web.Constant;
import com.taoyuanx.littlefile.web.FileHandler;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author dushitaoyuan
 * @desc 文件授权访问url构造
 * @date 2019/7/3
 */
public class FileAuthedUrlCreate {
    public FileHandler  getSFileHandler(){
        LittleFilConfig littleFileConfig = new LittleFilConfig("classpath:littlefile.properties");

        boolean gzip = littleFileConfig.getConfig(LittleFilConfig.LITTLEFILE_FILE_GZIP);
        String url_format=littleFileConfig.getConfig(LittleFilConfig.LITTLEFILE_FILEHANDLE_URL_FORMAT);
        Long tokenExpireMin=littleFileConfig.getConfig(LittleFilConfig.LITTLEFILE_TOKEN_EXPIRE_MIN);
        FileHandler fileHandler = new FileHandler(null,
                null,gzip,
                littleFileConfig.getTokenManager()
                ,tokenExpireMin,url_format);
        return  fileHandler;
    }

    public FileHandler  getFileHandler(){
        LittleFilConfig littleFileConfig = new LittleFilConfig("classpath:littlefile.properties");

        boolean gzip = littleFileConfig.getConfig(LittleFilConfig.LITTLEFILE_FILE_GZIP);
        String url_format=littleFileConfig.getConfig(LittleFilConfig.LITTLEFILE_FILEHANDLE_URL_FORMAT);
        FileHandler fileHandler = new FileHandler(null,
                null,gzip,url_format);
        return  fileHandler;
    }
    @Test
    public void createAuthedFileUrl(){
        String fileUrl="t1.png";
        String handleType="1";
        System.out.println(getSFileHandler().signFileUrl(fileUrl,handleType));
    }
    @Test
    public void createExpireFileUrl(){
        String fileUrl="t1.png";
        String handleType="1";
        System.out.println(getSFileHandler().signFileUrl(fileUrl,handleType,1L, TimeUnit.SECONDS));
    }

    @Test
    public void createPublicUrl(){
        String fileUrl="t1.png";
        String handleType="1";
        System.out.println(getFileHandler().createPublicUrl(fileUrl,handleType));
    }
}
