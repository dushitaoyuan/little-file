package com.taoyuanx.littlefile.client.impl;

import com.taoyuanx.littlefile.client.FileClient;
import com.taoyuanx.littlefile.client.utils.OkHttpUtil;
import com.taoyuanx.littlefile.client.utils.StrUtil;
import okhttp3.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FileClientImpl implements FileClient {
    public static OkHttpClient client;
    public static Map<String, String> baseUrls;

    @Override
    public String upload(String localFile) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            File file = new File(localFile);
            RequestBody fileBody = RequestBody.create(
                    MediaType.parse(OkHttpUtil.guessMimeType(file.getName())), file);
            builder.addFormDataPart("file", file.getName(), fileBody);
            return OkHttpUtil.doCall(client,
                    new Request.Builder().url(baseUrls.get("upload"))
                            .post(builder.build()).build()).string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String upload(byte[] fileBytes, String fileName) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            RequestBody fileBody = RequestBody.create(
                    MediaType.parse(OkHttpUtil.guessMimeType(fileName)), fileBytes);
            builder.addFormDataPart("file", fileName, fileBody);
            return OkHttpUtil.doCall(client,
                    new Request.Builder().url(baseUrls.get("upload"))
                            .post(builder.build()).build()).string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String upload(InputStream fileInput, String fileName) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            RequestBody fileBody = RequestBody.create(
                    MediaType.parse(OkHttpUtil.guessMimeType(fileName)), OkHttpUtil.streamToArray(fileInput));
            builder.addFormDataPart("file", fileName, fileBody);
            return OkHttpUtil.doCall(client,
                    new Request.Builder().url(baseUrls.get("upload"))
                            .post(builder.build()).build()).string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String uploadSlave(String localFile, String masterFileId) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            File file = new File(localFile);
            RequestBody fileBody = RequestBody.create(
                    MediaType.parse(OkHttpUtil.guessMimeType(file.getName())), file);
            builder.addFormDataPart("file", file.getName(), fileBody);
            if (StrUtil.isEmpty(masterFileId)) {
                throw new RuntimeException("主文件id为空");
            }
            Map<String, String> params = new HashMap<String, String>(1);
            params.put("masterFileId", masterFileId);
            OkHttpUtil.addParams(builder, params);
            return OkHttpUtil.doCall(client,
                    new Request.Builder().url(baseUrls.get("uploadSlave"))
                            .post(builder.build()).build()).string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String uploadSlave(byte[] fileBytes, String fileName, String masterFileId) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            RequestBody fileBody = RequestBody.create(
                    MediaType.parse(OkHttpUtil.guessMimeType(fileName)), fileBytes);
            builder.addFormDataPart("file", fileName, fileBody);
            if (StrUtil.isEmpty(masterFileId)) {
                throw new RuntimeException("主文件id为空");
            }
            Map<String, String> params = new HashMap<String, String>(1);
            params.put("masterFileId", masterFileId);
            OkHttpUtil.addParams(builder, params);
            return OkHttpUtil.doCall(client,
                    new Request.Builder().url(baseUrls.get("uploadSlave"))
                            .post(builder.build()).build()).string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String uploadSlave(InputStream fileInput, String fileName,
                              String masterFileId) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            RequestBody fileBody = RequestBody.create(
                    MediaType.parse(OkHttpUtil.guessMimeType(fileName)), OkHttpUtil.streamToArray(fileInput));
            builder.addFormDataPart("file", fileName, fileBody);
            if (StrUtil.isEmpty(masterFileId)) {
                throw new RuntimeException("主文件id为空");
            }
            Map<String, String> params = new HashMap<String, String>(1);
            params.put("masterFileId", masterFileId);
            OkHttpUtil.addParams(builder, params);
            return OkHttpUtil.doCall(client,
                    new Request.Builder().url(baseUrls.get("uploadSlave"))
                            .post(builder.build()).build()).string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(String fileId) {
        try {
            StringBuilder delteUrl = new StringBuilder(baseUrls.get("delete"));
            delteUrl.append("?").append("fileId").append("=").append(fileId);
            String res = OkHttpUtil.doCall(client,
                    new Request.Builder().url(delteUrl.toString()).delete()
                            .build()).string();
            if ("true".equals(res)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void downLoad(String fileId, String destFile) {
        try {
            StringBuilder delteUrl = new StringBuilder(baseUrls.get("download"));
            delteUrl.append("?").append("fileId").append("=").append(fileId);
            ResponseBody body = OkHttpUtil.doCall(client, new Request.Builder()
                    .url(delteUrl.toString()).get().build());

            FileOutputStream dest = new FileOutputStream(destFile);
            InputStream input = body.byteStream();
            byte[] b = new byte[1024];
            int len = 0;
            while ((len = input.read(b)) != -1) {
                dest.write(b, 0, len);
                dest.flush();
            }
            dest.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] downLoad(String fileId) {
        try {
            StringBuilder delteUrl = new StringBuilder(baseUrls.get("download"));
            delteUrl.append("?").append("fileId").append("=").append(fileId);
            ResponseBody body = OkHttpUtil.doCall(client, new Request.Builder()
                    .url(delteUrl.toString()).get().build());
            return body.bytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String uploadImage(String localFile, String cutSize) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            File local = new File(localFile);
            RequestBody fileBody = RequestBody.create(
                    MediaType.parse(OkHttpUtil.guessMimeType(local.getName())), localFile);
            builder.addFormDataPart("file", local.getName(), fileBody);
            if (!StrUtil.isEmpty(cutSize)) {
                Map<String, String> params = new HashMap<String, String>(1);
                params.put("cutSize", cutSize);
                OkHttpUtil.addParams(builder, params);
            }
            return OkHttpUtil.doCall(client,
                    new Request.Builder().url(baseUrls.get("uploadImage"))
                            .post(builder.build()).build()).string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String uploadImage(byte[] fileBytes, String fileName, String cutSize) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            RequestBody fileBody = RequestBody.create(
                    MediaType.parse(OkHttpUtil.guessMimeType(fileName)), fileBytes);
            builder.addFormDataPart("file", fileName, fileBody);
            if (!StrUtil.isEmpty(cutSize)) {
                Map<String, String> params = new HashMap<String, String>(1);
                params.put("cutSize", cutSize);
                OkHttpUtil.addParams(builder, params);
            }
            return OkHttpUtil.doCall(client,
                    new Request.Builder().url(baseUrls.get("uploadImage"))
                            .post(builder.build()).build()).string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String uploadImage(InputStream fileInput, String fileName,
                              String cutSize) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            RequestBody fileBody = RequestBody.create(
                    MediaType.parse(OkHttpUtil.guessMimeType(fileName)), OkHttpUtil.streamToArray(fileInput));
            builder.addFormDataPart("file", fileName, fileBody);
            if (!StrUtil.isEmpty(cutSize)) {
                Map<String, String> params = new HashMap<String, String>(1);
                params.put("cutSize", cutSize);
                OkHttpUtil.addParams(builder, params);
            }
            return OkHttpUtil.doCall(client,
                    new Request.Builder().url(baseUrls.get("uploadImage"))
                            .post(builder.build()).build()).string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFileInfo(String fileId) {
        try {
            StringBuilder delteUrl = new StringBuilder(baseUrls.get("getFileInfo"));
            delteUrl.append("?").append("fileId").append("=").append(fileId);
            ResponseBody body = OkHttpUtil.doCall(client, new Request.Builder()
                    .url(delteUrl.toString()).get().build());
            return body.string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
