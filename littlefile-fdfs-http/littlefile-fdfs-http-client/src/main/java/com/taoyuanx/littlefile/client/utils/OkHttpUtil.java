package com.taoyuanx.littlefile.client.utils;

import com.alibaba.fastjson.JSON;
import com.taoyuanx.littlefile.client.core.FdfsFileClientConstant;
import com.taoyuanx.littlefile.client.ex.FdfsException;
import com.taoyuanx.littlefile.client.core.Result;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class OkHttpUtil {
    public static String guessMimeType(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = null;
        try {
            contentTypeFor = fileNameMap.getContentTypeFor(URLEncoder.encode(
                    fileName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    public static void addParams(MultipartBody.Builder builder,
                                 Map<String, Object> paramsMap) {
        try {
            if (paramsMap != null && !paramsMap.isEmpty()) {
                for (String key : paramsMap.keySet()) {
                    Object value = paramsMap.get(key);
                    if (paramsMap.containsKey(FdfsFileClientConstant.FILE_NAME_KEY)) {
                        return;
                    }
                    /**
                     * 文件参数
                     */
                    if (value instanceof File) {
                        File file = (File) value;
                        String fileName = file.getName();
                        if (paramsMap.containsKey(FdfsFileClientConstant.FILE_NAME_KEY)) {
                            fileName = paramsMap.get(FdfsFileClientConstant.FILE_NAME_KEY).toString();
                        }
                        builder.addFormDataPart(key, fileName, RequestBody.create(null, file));
                    } else if (value instanceof byte[]) {
                        byte[] fileBytes = (byte[]) value;
                        String fileName = paramsMap.get(FdfsFileClientConstant.FILE_NAME_KEY).toString();
                        RequestBody fileBody = RequestBody.create(MediaType.parse(OkHttpUtil.guessMimeType(fileName)), fileBytes);
                        builder.addFormDataPart(key, fileName, fileBody);
                    } else if (value instanceof InputStream) {
                        byte[] fileBytes = streamToArray((InputStream) value);
                        String fileName = paramsMap.get(FdfsFileClientConstant.FILE_NAME_KEY).toString();
                        RequestBody fileBody = RequestBody.create(MediaType.parse(OkHttpUtil.guessMimeType(fileName)), fileBytes);
                        builder.addFormDataPart(key, fileName, fileBody);
                    } else {
                        /**
                         * 普通参数
                         */
                        builder.addFormDataPart(key, value.toString());
                    }

                }

            }
        } catch (Exception e) {
            throw new FdfsException("参数转换异常");
        }
    }

    /**
     * 文件流转字节数组
     *
     * @param input
     * @return
     * @throws IOException
     */
    public static byte[] streamToArray(InputStream input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(input.available());
        byte[] buf = new byte[FdfsFileClientConstant.BUFFER_SIZE];
        int len = 0;
        while ((len = input.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        input.close();
        return out.toByteArray();
    }

    public static <T> T request(OkHttpClient client, Request request, Class<T> type) throws FdfsException {

        Response response = null, temp;
        try {
            response = client.newCall(request).execute();
            temp = response;
            if (response.isSuccessful()) {
                if (type == null) {
                    return null;
                }
                if (response.getClass().equals(type)) {
                    response = null;
                    return (T) temp;
                } else {
                    Result result = JSON.parseObject(response.body().string(), Result.class);
                    if (type.equals(String.class)) {
                        return (T) result.getData();
                    }
                    return JSON.parseObject(result.getData(), type);
                }
            }
            throw new FdfsException(StrUtil.log4jFormat("文件服务调用异常,异常结果:{}", response.body().string()));
        } catch (FdfsException e) {
            throw e;
        } catch (Exception e) {
            throw new FdfsException(StrUtil.log4jFormat("文件服务调用异常,请求地址:{}", request.url()), e);
        } finally {
            if (Objects.nonNull(response)) {
                response.close();
            }
        }

    }

    public static void transferTo(InputStream input, OutputStream output) {
        try {
            byte[] buffer = new byte[FdfsFileClientConstant.BUFFER_SIZE];
            int len = 0;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
                output.flush();
            }
            output.close();
        } catch (Exception e) {
            throw new FdfsException("文件服务异常");
        }
    }

    public static void transferTo(InputStream input, File file) {
        try {
            transferTo(input, new FileOutputStream(file));
        } catch (FdfsException e) {
            throw e;
        } catch (Exception e) {
            throw new FdfsException("文件服务异常");
        }
    }

}
