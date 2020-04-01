package com.taoyuanx.littlefile.client.core;

/**
 * @author dushitaoyuan
 * @desc 文件client常量
 * @date 2020/4/1
 */
public class FdfsFileClientConstant {
    public static final String FILE_NAME_KEY = "fileName";
    public static final String FILE_KEY = "file";
    public static final int BUFFER_SIZE = 2 << 20;

    public static final String REQUEST_TOKEN_TAG = "token";
    public static final String REQUEST_TOKEN_KEY = "token";

    public static final String RANGE_HEADER = "Range";

    public static enum FdfsApi {
        UPLOAD("upload", "文件上传"),
        UPLOAD_SLAVE("uploadSlave", "从文件上传"),
        UPLOAD_IMG("image/upload", "图片上传可生成缩略图"),
        DOWNLOAD("download", "文件下载"),
        DOWNLOAD_RANGE("download/range", "文件断点下载"),
        REMOVE("removeFile", "文件删除"),
        FILE_INFO("info", "文件信息获取"),
        PREVIEW("preview", "文件预览"),
        ;
        public String path;
        public String desc;

        FdfsApi(String path, String desc) {
            this.path = path;
            this.desc = desc;
        }
    }
}
