package com.taoyuanx.littlefile.client.utils;

import okhttp3.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

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

	public  static void addParams(MultipartBody.Builder builder,
			Map<String, String> params) {
		if (params != null && !params.isEmpty()) {
			for (String key : params.keySet()) {
				builder.addPart(
						Headers.of("Content-Disposition", "form-data; name=\""
								+ key + "\""),
						RequestBody.create(null, params.get(key)));
			}
		}
	}

	/**
	 * 文件流转字节数组
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public  static  byte[] streamToArray(InputStream input) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(input.available());
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = input.read(buf)) != -1) {
			out.write(buf, 0, len);
		}
		input.close();
		return out.toByteArray();
	}

	public  static  ResponseBody doCall(OkHttpClient client,Request request) {
		try {
			Response resp = client.newCall(request).execute();
			if(resp.isSuccessful()){
				return resp.body();
			}
			throw new RuntimeException("服务调用异常"+resp.body().string());
		} catch (Exception e) {
			throw new RuntimeException("文件服务调用异常" + e + "\t url:"
					+ request.url(),e);
		}

	}
	


	public  static Response doCallWithResp(OkHttpClient client,Request request) {
		try {
			
			return client.newCall(request).execute();
		} catch (Exception e) {
			throw new RuntimeException("文件服务调用异常" + e + "\t url:"
					+ request.url(),e);
		}

	}
	

}
