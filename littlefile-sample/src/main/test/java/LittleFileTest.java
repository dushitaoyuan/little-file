import com.taoyuanx.littlefile.config.LittleFileConfig;
import com.taoyuanx.littlefile.ftp.LittleFileFtp;
import com.taoyuanx.littlefile.sftp.LittleFileSftp;
import com.taoyuanx.littlefile.support.FileServerEum;
import com.taoyuanx.littlefile.util.Utils;
import com.taoyuanx.littlefile.web.security.MacEum;
import org.junit.Test;

/**
 * @author dushitaoyuan
 * @desc 测试
 * @date 2019/7/4
 */
public class LittleFileTest {
    @Test
    public void testLittleFileConfig() {
        LittleFileConfig config = new LittleFileConfig("classpath:littlefile.properties");
        FileServerEum serverEum = config.getConfig(LittleFileConfig.LITTLEFILE_SERVER_TYPE);
        System.out.println(serverEum);
        System.out.println(config.getConfig(LittleFileConfig.LITTLEFILE_FILE_CACHE_TIME));
        MacEum m = config.getConfig(LittleFileConfig.LITTLEFILE_TOKEN_HMAC);
        System.out.println(m);
    }

    @Test
    public void testLittleFtp() throws Exception {
        LittleFileFtp ftp = new LittleFileFtp("127.0.0.1", 21, "ftp", "ftp", "/", null, null, null, null);
        //new LittleFileFtp("127.0.0.1", 2221, "admin", "admin", "","classpath:ftpserver.jks", "password", "classpath:ftpserver.jks", "password");
        //new LittleFileFtp("127.0.0.1", 2121, "admin", "admin", "");
        //new LittleFileFtp("127.0.0.1", 21, "ftp", "ftp", "/opt/ftp/");
        ftp.upload("L://1.png", "/mk6/1.png");
        ftp.download("/mk6/1.png", "L://123.png");
        ftp.close();
    }

    @Test
    public void testLittleSftp() throws Exception {
        LittleFileSftp sftp = new LittleFileSftp("192.168.91.201", 22, "root", "123", "/opt/sftp/", null, null);
        sftp.download("1.png", "L://123.png");
        sftp.upload("L://123.png", "123/123.png");
        sftp.closeChannel();
    }

    @Test
    public void testKey() throws Exception {
        String path = "e://ftp/client.p12";
        //"classpath:client.p12";
        String password = "123456";
        System.out.println(Utils.getKeyManager(path, password));
        System.out.println(Utils.getTrustManager(path, password));
    }


}
