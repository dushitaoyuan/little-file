package com.taoyuanx.littlefile.client.impl;

import com.taoyuanx.littlefile.client.core.FdfsFileClientConstant;
import com.taoyuanx.littlefile.client.core.ParamBuilder;
import com.taoyuanx.littlefile.client.ex.FdfsException;
import com.taoyuanx.littlefile.client.utils.OkHttpUtil;
import com.taoyuanx.littlefile.client.utils.StrUtil;
import com.taoyuanx.littlefile.fdfshttp.core.client.FileClient;
import com.taoyuanx.littlefile.fdfshttp.core.dto.FileInfo;
import com.taoyuanx.littlefile.fdfshttp.core.dto.MasterAndSlave;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class FileClientImpl implements FileClient {
    private OkHttpClient client;
    private Map<FdfsFileClientConstant.FdfsApi, String> apiMap;

    public FileClientImpl(OkHttpClient client, Map<FdfsFileClientConstant.FdfsApi, String> apiMap) {
        this.client = client;
        this.apiMap = apiMap;
    }

    @Override
    public String upload(String localFile) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        File file = new File(localFile);
        Map<String, Object> paramMap = ParamBuilder.newBuilder().addParam(FdfsFileClientConstant.FILE_KEY, file).build();
        OkHttpUtil.addParams(builder, paramMap);
        return OkHttpUtil.request(client,
                new Request.Builder().url(apiMap.get(FdfsFileClientConstant.FdfsApi.UPLOAD))
                        .post(builder.build()).tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), String.class);

    }

    @Override
    public String upload(byte[] fileBytes, String fileName) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        Map<String, Object> paramMap = ParamBuilder.newBuilder().addParam(FdfsFileClientConstant.FILE_KEY, fileBytes)
                .addParam(FdfsFileClientConstant.FILE_NAME_KEY, fileName).build();
        OkHttpUtil.addParams(builder, paramMap);
        return OkHttpUtil.request(client,
                new Request.Builder().url(apiMap.get(FdfsFileClientConstant.FdfsApi.UPLOAD))
                        .post(builder.build()).tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), String.class);

    }

    @Override
    public String upload(InputStream fileInput, String fileName) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        Map<String, Object> paramMap = ParamBuilder.newBuilder().addParam(FdfsFileClientConstant.FILE_KEY, fileInput)
                .addParam(FdfsFileClientConstant.FILE_NAME_KEY, fileName).build();
        OkHttpUtil.addParams(builder, paramMap);
        return OkHttpUtil.request(client,
                new Request.Builder().url(apiMap.get(FdfsFileClientConstant.FdfsApi.UPLOAD))
                        .post(builder.build()).tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), String.class);

    }

    @Override
    public void appendFile(InputStream fileInput, String fileName, String fileId) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        Map<String, Object> paramMap = ParamBuilder.newBuilder().addParam(FdfsFileClientConstant.FILE_KEY, fileInput)
                .addParam("fileId", fileId)
                .addParam(FdfsFileClientConstant.FILE_NAME_KEY, fileName)
                .build();
        OkHttpUtil.addParams(builder, paramMap);
        OkHttpUtil.request(client,
                new Request.Builder().url(apiMap.get(FdfsFileClientConstant.FdfsApi.UPLOAD_RANGE))
                        .post(builder.build()).tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), null);
    }
    @Override
    public void coverAppendFile(InputStream fileInput, String fileName, Long start, String fileId) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        Map<String, Object> paramMap = ParamBuilder.newBuilder().addParam(FdfsFileClientConstant.FILE_KEY, fileInput)
                .addParam("fileId", fileId)
                .addParam("offset", start)
                .addParam(FdfsFileClientConstant.FILE_NAME_KEY, fileName)
                .build();
        OkHttpUtil.addParams(builder, paramMap);
        OkHttpUtil.request(client,
                new Request.Builder().url(apiMap.get(FdfsFileClientConstant.FdfsApi.UPLOAD_RANGE))
                        .post(builder.build()).tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), null);
    }

    @Override
    public String uploadAppendFile(InputStream fileInput, String fileName) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        Map<String, Object> paramMap = ParamBuilder.newBuilder().addParam(FdfsFileClientConstant.FILE_KEY, fileInput)
                .addParam(FdfsFileClientConstant.FILE_NAME_KEY, fileName)
                .build();
        OkHttpUtil.addParams(builder, paramMap);
        return OkHttpUtil.request(client,
                new Request.Builder().url(apiMap.get(FdfsFileClientConstant.FdfsApi.UPLOAD_RANGE))
                        .post(builder.build()).tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), String.class);

    }


    @Override
    public String uploadSlave(String localFile, String masterFileId) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        File file = new File(localFile);
        if (StrUtil.isEmpty(masterFileId)) {
            throw new FdfsException("主文件id为空");
        }
        Map<String, Object> paramMap = ParamBuilder.newBuilder().addParam(FdfsFileClientConstant.FILE_KEY, file)
                .addParam("masterFileId", masterFileId).build();
        OkHttpUtil.addParams(builder, paramMap);
        return OkHttpUtil.request(client,
                new Request.Builder().url(apiMap.get(FdfsFileClientConstant.FdfsApi.UPLOAD_SLAVE))
                        .post(builder.build()).tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), String.class);

    }

    @Override
    public String uploadSlave(byte[] fileBytes, String fileName, String masterFileId) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        if (StrUtil.isEmpty(masterFileId)) {
            throw new FdfsException("主文件id为空");
        }
        Map<String, Object> paramMap = ParamBuilder.newBuilder().addParam(FdfsFileClientConstant.FILE_KEY, fileBytes)
                .addParam("masterFileId", masterFileId).build();
        OkHttpUtil.addParams(builder, paramMap);
        return OkHttpUtil.request(client,
                new Request.Builder().url(apiMap.get(FdfsFileClientConstant.FdfsApi.UPLOAD_SLAVE))
                        .post(builder.build()).tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), String.class);

    }

    @Override
    public String uploadSlave(InputStream fileInput, String fileName,
                              String masterFileId) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        if (StrUtil.isEmpty(masterFileId)) {
            throw new FdfsException("主文件id为空");
        }
        Map<String, Object> paramMap = ParamBuilder.newBuilder().addParam(FdfsFileClientConstant.FILE_KEY, fileInput)
                .addParam("masterFileId", masterFileId).build();
        OkHttpUtil.addParams(builder, paramMap);
        return OkHttpUtil.request(client,
                new Request.Builder().url(apiMap.get(FdfsFileClientConstant.FdfsApi.UPLOAD_SLAVE))
                        .post(builder.build()).tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), String.class);

    }

    @Override
    public void delete(String fileId) {
        String deleteUrl = apiMap.get(FdfsFileClientConstant.FdfsApi.REMOVE) + "?fileId=" + fileId;
        OkHttpUtil.request(client,
                new Request.Builder().url(deleteUrl).delete().tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG)
                        .build(), null);
    }

    @Override
    public void downLoad(String fileId, String destFile) {
        String downloadUrl = apiMap.get(FdfsFileClientConstant.FdfsApi.DOWNLOAD) + "?fileId=" + fileId;
        Response response = OkHttpUtil.request(client, new Request.Builder()
                .url(downloadUrl).get().tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), Response.class);
        OkHttpUtil.transferTo(response.body().byteStream(), new File(destFile));
        response.close();
    }

    @Override
    public void downLoad(String fileId, OutputStream output) {
        String downloadUrl = apiMap.get(FdfsFileClientConstant.FdfsApi.DOWNLOAD) + "?fileId=" + fileId;
        Response response = OkHttpUtil.request(client, new Request.Builder()
                .url(downloadUrl).get().tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), Response.class);
        OkHttpUtil.transferTo(response.body().byteStream(), output);
        response.close();
    }


    @Override
    public byte[] downLoad(String fileId) {
        try {
            String downloadUrl = apiMap.get(FdfsFileClientConstant.FdfsApi.DOWNLOAD) + "?fileId=" + fileId;
            Response response = OkHttpUtil.request(client, new Request.Builder()
                    .url(downloadUrl).get().tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), Response.class);
            byte[] bytes = response.body().bytes();
            response.close();
            return bytes;
        } catch (Exception e) {
            throw new FdfsException("下载异常", e);
        }
    }

    @Override
    public void downLoadRange(String fileId, Long start, Long end, OutputStream output) {
        String downloadUrl = apiMap.get(FdfsFileClientConstant.FdfsApi.DOWNLOAD_RANGE) + "?fileId=" + fileId;
        Response response = OkHttpUtil.request(client, new Request.Builder()
                .url(downloadUrl).get().addHeader(FdfsFileClientConstant.RANGE_HEADER, OkHttpUtil.rangeHeader(start, end)).tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), Response.class);
        OkHttpUtil.transferTo(response.body().byteStream(), output);
        response.close();

    }


    @Override
    public byte[] downLoadRange(String fileId, Long start, Long end) {
        try {
            String downloadUrl = apiMap.get(FdfsFileClientConstant.FdfsApi.DOWNLOAD_RANGE) + "?fileId=" + fileId;
            Response response = OkHttpUtil.request(client, new Request.Builder()
                    .url(downloadUrl).get().addHeader(FdfsFileClientConstant.RANGE_HEADER, OkHttpUtil.rangeHeader(start, end)).tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), Response.class);
            byte[] bytes = response.body().bytes();
            response.close();
            return bytes;
        } catch (Exception e) {
            throw new FdfsException("下载异常", e);
        }
    }

    @Override
    public MasterAndSlave uploadImage(String localFile, String cutSize) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        File local = new File(localFile);
        ParamBuilder paramBuilder = ParamBuilder.newBuilder().addParam(FdfsFileClientConstant.FILE_KEY, local);
        if (StrUtil.isNotEmpty(cutSize)) {
            paramBuilder.addParam("cutSize", cutSize);
        }
        OkHttpUtil.addParams(builder, paramBuilder.build());
        return OkHttpUtil.request(client,
                new Request.Builder().url(apiMap.get(FdfsFileClientConstant.FdfsApi.UPLOAD_IMG))
                        .post(builder.build()).tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), MasterAndSlave.class);

    }

    @Override
    public MasterAndSlave uploadImage(byte[] fileBytes, String fileName, String cutSize) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        ParamBuilder paramBuilder = ParamBuilder.newBuilder()
                .addParam(FdfsFileClientConstant.FILE_KEY, fileBytes)
                .addParam(FdfsFileClientConstant.FILE_NAME_KEY, fileName);
        if (StrUtil.isNotEmpty(cutSize)) {
            paramBuilder.addParam("cutSize", cutSize);
        }
        OkHttpUtil.addParams(builder, paramBuilder.build());
        return OkHttpUtil.request(client,
                new Request.Builder().url(apiMap.get(FdfsFileClientConstant.FdfsApi.UPLOAD_IMG))
                        .post(builder.build()).tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), MasterAndSlave.class);

    }

    @Override
    public MasterAndSlave uploadImage(InputStream fileInput, String fileName,
                                      String cutSize) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        ParamBuilder paramBuilder = ParamBuilder.newBuilder()
                .addParam(FdfsFileClientConstant.FILE_KEY, fileInput)
                .addParam(FdfsFileClientConstant.FILE_NAME_KEY, fileName);
        if (StrUtil.isNotEmpty(cutSize)) {
            paramBuilder.addParam("cutSize", cutSize);
        }
        OkHttpUtil.addParams(builder, paramBuilder.build());
        return OkHttpUtil.request(client,
                new Request.Builder().url(apiMap.get(FdfsFileClientConstant.FdfsApi.UPLOAD_IMG))
                        .post(builder.build()).tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), MasterAndSlave.class);

    }

    @Override
    public FileInfo getFileInfo(String fileId) {
        String fileInfoUrl = apiMap.get(FdfsFileClientConstant.FdfsApi.FILE_INFO) + "?fileId=" + fileId;
        return OkHttpUtil.request(client, new Request.Builder()
                .url(fileInfoUrl).get().tag(FdfsFileClientConstant.REQUEST_TOKEN_TAG).build(), FileInfo.class);

    }
}
