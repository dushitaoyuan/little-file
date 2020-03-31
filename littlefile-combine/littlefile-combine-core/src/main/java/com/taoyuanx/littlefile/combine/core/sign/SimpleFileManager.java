package com.taoyuanx.littlefile.combine.core.sign;

import com.taoyuanx.littlefile.combine.core.dto.FileSignDTO;
import com.taoyuanx.littlefile.combine.core.utils.JSONUtil;
import com.taoyuanx.littlefile.combine.core.utils.TokenForamtUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author dushitaoyuan
 * @desc 文件工具(文件路径签名, http访问路径拼接, 签名验签)
 * @date 2020/2/20
 */
public class SimpleFileManager {

    public static HmacAlgorithms hmacAlg = HmacAlgorithms.HMAC_MD5;

    /**
     * 文件签名密码
     */
    private String fileSignHmacKey;
    /**
     * 文件服务暴露端点地址
     */
    private String fileEndpoint;
    /**
     * 签名url 默认过期时间 5分钟
     */
    private Long expire = 5L;
    private TimeUnit timeUnit = TimeUnit.MINUTES;

    public SimpleFileManager(String fileSignHmacKey, String fileEndpoint) {
        this.fileSignHmacKey = fileSignHmacKey;
        this.fileEndpoint = fileEndpoint;
    }


    public SimpleFileManager(String fileSignHmacKey, String fileEndpoint, Long expire, TimeUnit timeUnit) {
        this.fileSignHmacKey = fileSignHmacKey;
        this.fileEndpoint = fileEndpoint;
        this.expire = expire;
        this.timeUnit = timeUnit;
    }

    /**
     * 文件签名
     */
    public String signFile(FileSignDTO FileSignDTO, Long expire, TimeUnit timeUnit) {
        if (expire != null) {
            Long now = System.currentTimeMillis();
            Long end = now + timeUnit.toMillis(expire);
            FileSignDTO.setCreateTime(now);
            FileSignDTO.setEndTime(end);
        }
        byte[] data = JSONUtil.toJsonBytes(FileSignDTO);
        byte[] sign = HmacUtils.getInitializedMac(hmacAlg, fileSignHmacKey.getBytes()).doFinal(data);
        return TokenForamtUtil.format(data, sign);
    }

    public String signFile(FileSignDTO FileSignDTO) {
        if (needExpireTime(FileSignDTO)) {
            return signFile(FileSignDTO, expire, timeUnit);
        } else {
            return signFile(FileSignDTO, null, null);
        }

    }

    private boolean needExpireTime(FileSignDTO FileSignDTO) {
        return !(FileSignDTO.getType().equals(FileTypeEnum.PUBLIC.code));
    }

    /**
     * 签名url 校验
     */
    public FileSignDTO parseFile(String signUrl) {
        if (StringUtils.isEmpty(signUrl)) {
            return null;
        }
        String[] split = TokenForamtUtil.splitToken(signUrl);
        if (split.length != 2) {
            return null;
        }
        byte[] data = Base64.decodeBase64(split[TokenForamtUtil.DATA_INDEX].getBytes());
        FileSignDTO FileSignDTO = JSONUtil.parseObject(data, FileSignDTO.class);
        FileSignDTO.setData(data);
        FileSignDTO.setSign(split[TokenForamtUtil.SING_INDEX]);
        return FileSignDTO;

    }

    public boolean verify(FileSignDTO fileSignDTO, FileTypeEnum type) {
        /**
         * 文件类型不符
         */
        if (type != null && !type.equals(FileTypeEnum.type(fileSignDTO.getType()))) {
            return false;
        }
        /**
         * 检查是否过期
         */
        Long end = fileSignDTO.getEndTime();
        if (end != null && end < System.currentTimeMillis()) {
            return false;
        }
        String calcSign = Base64.encodeBase64URLSafeString(HmacUtils.getInitializedMac(hmacAlg, fileSignHmacKey.getBytes()).doFinal(fileSignDTO.getData()));
        if (!calcSign.equals(fileSignDTO.getSign())) {
            return false;
        }
        return true;
    }

    public boolean verify(FileSignDTO FileSignDTO) {
        return verify(FileSignDTO, null);
    }

    public boolean verify(String signUrl, FileTypeEnum fileType) {
        return verify(parseFile(signUrl), fileType);
    }

    public boolean verify(String signUrl) {
        return verify(parseFile(signUrl), null);
    }


    public String getFullHttpUrl(FileSignDTO FileSignDTO, Long expire, TimeUnit timeUnit) {
        return String.format(fileEndpoint, signFile(FileSignDTO, expire, timeUnit));
    }

    public String getFullHttpUrl(FileSignDTO FileSignDTO) {
        return String.format(fileEndpoint, signFile(FileSignDTO));
    }

    public FileSignDTO newFileSignDTO(String storePath, FileTypeEnum fileType) {
        FileSignDTO FileSignDTO = new FileSignDTO();
        FileSignDTO.setType(fileType.code);
        FileSignDTO.setPath(storePath);
        return FileSignDTO;

    }

    public FileSignDTO newFileSignDTO(Long fileId, String storePath, FileTypeEnum fileType) {
        FileSignDTO FileSignDTO = newFileSignDTO(storePath, fileType);
        FileSignDTO.setId(fileId);
        return FileSignDTO;

    }


}
