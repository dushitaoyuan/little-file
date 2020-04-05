package com.taoyuanx.littlefile.client.impl.loadbalance;

/**
 * api 概览
 */
public enum FdfsApi {
    UPLOAD("upload", "文件上传"),
    UPLOAD_SLAVE("uploadSlave", "从文件上传"),
    UPLOAD_IMG("image/upload", "图片上传可生成缩略图"),
    UPLOAD_RANGE("upload/range", "文件断点上传"),
    DOWNLOAD("download", "文件下载"),
    DOWNLOAD_RANGE("download/range", "文件断点下载"),
    REMOVE("removeFile", "文件删除"),
    FILE_INFO("info", "文件信息获取"),
    PREVIEW("preview", "文件预览"),
    HELLO("", "心跳监测"),
    ;
    public String path;
    public String desc;

    FdfsApi(String path, String desc) {
        this.path = path;
        this.desc = desc;
    }
}