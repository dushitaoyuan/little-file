# fastdfs-http

## 项目简介

1. littlefile-fdfs-http-server   服务端
2. littlefile-fdfs-http-core 核心依赖
3. littlefile-fdfs-http-client client SDK  
支持 心跳监测 client端负载均衡


## http api

### 全局约定

1. 基础路径  /file

2. 全局结果包装

   | 名称    | 类型   | 描述         | 备注       |
   | ------- | ------ | ------------ | ---------- |
   | success | number | 请求是否成功 | 1成功0失败 |
   | code    | number | 消息码       | 可能为空   |
   | msg     | string | 消息描述     | 可能为空   |
   | data    | all    | 返回结果     | 可能为空   |

3. 错误码

      | code错误码 | 描述       | http状态码 |
      | ---------- | ---------- | ---------- |
      | 401        | 未授权访问 | 401        |
      | 500        | 系统异常   | 500        |

   4. 安全配置

      静态token 参见配置: littlefile.fdfs.server.token

###  api列表

####  api概览

| path                 | http method | 描述           |
| -------------------- | ----------- | -------------- |
| /file/upload         | POST        | 上传           |
| /file/uploadSlave    | POST        | 从文件上传     |
| /file/upload/range   | POST        | 断点上传       |
| /file/removeFile     | DELETE      | 文件删除       |
| /file/image/upload   | POST        | 图片上传       |
| /file/download       | GET         | 下载           |
| /file/download/range | GET         | 断点下载       |
| /file/info           | GET         | 信息获取       |
| /file/preview        | GET         | 文件下载或预览 |
| /file/hello        | GET         | 心跳监测 |

####  /file/upload

- POST 

- 参数

| 参数名称 | 类型 | 描述 | 必选 |
| -------- | ---- | ---- | ---- |
| file     | file | 文件 | 是   |

- 返回结果

```json
  {
  	"success": 1,
  	"data": "group1/M00/00/00/wKgD0l6E2GeAFrLRAAB5ok9XEwo822.png"
  }
```

####  /file/uploadSlave

- POST 

- 参数

| 参数名称 | 类型 | 描述 | 必选 |
| -------- | ---- | ---- | ---- |
| file     | file | 从文件 | 是   |
| masterFileId     | string | 主文件id | 是   |
| prefixName     | string | 从文件前缀名称 | 否 |

- 返回结果

```json
  {
	"success": 1,
	"data": "group1/M00/00/00/wKgD0l6E2GeAFrLRAAB5ok9XEwo822.png"
}
```

####  /file/upload/range 

- POST 

- 参数

| 参数名称 | 类型 | 描述 | 必选 |
| -------- | ---- | ---- | ---- |
| file     | file | 文件块 | 是   |
| fileId     | string | 文件id | 非首块否,首块是   |
| offset     | number | 文件块偏移量 | 否 |

- 返回结果

```json
# 1.首次断点上传
  {
	"success": 1,
	"data": "group1/M00/00/00/wKgD0l6E2GeAFrLRAAB5ok9XEwo822.png"
}
# 后续文件块上传

 {
	"success": 1
}
```

####  /file/removeFile

- DELETE 

- 参数

| 参数名称 | 类型 | 描述 | 必选 |
| -------- | ---- | ---- | ---- |
| fileId     | string | 文件id | 是|


- 返回结果

```json
 {
	"success": 1
}
```

####  /file/image/upload

- POST 

- 参数

| 参数名称 | 类型 | 描述 | 必选 |
| -------- | ---- | ---- | ---- |
| file     | file | 文件 | 是   |
| cutSize     | string | 图片裁剪尺寸缩略图,如:20x20,30x30,100x100 | 否|

- 返回结果

```json
{
	"success": 1,
	"data": {
		"master": "group1/M00/00/00/wKgD0l6E2GeAFrLRAAB5ok9XEwo822.png",
		"slaves": [
			"group1/M00/00/00/wKgD0l6E2GeAFrLRAAB5ok9XEwo822_20x20.png",
			"group1/M00/00/00/wKgD0l6E2GeAFrLRAAB5ok9XEwo822_100x100.png"
		]
	}
}
```

####  /file/download

- GET 

- 参数

| 参数名称 | 类型 | 描述 | 必选 |
| -------- | ---- | ---- | ---- |
| fileId     |string | 文件id | 是   |


- 返回结果

文件流

####  /file/download/range

- GET 

- 参数

| 参数名称 | 类型 | 描述 | 必选 |
| -------- | ---- | ---- | ---- |
| fileId     |string | 文件id | 是   |
| fileSize     |strinnumberg | 文件大小 | 否   |

- http header

| header 名称 | 示例 | 描述 |
| -------- | ---- | ---- |
| Range     |bytes=500-1000 | 起止位置 |
| Range     |bytes=500- | 起始位置,适合流媒体播放 |

- 返回结果
文件流


####  /file/info

- GET 

- 参数

| 参数名称 | 类型 | 描述 | 必选 |
| -------- | ---- | ---- | ---- |
| fileId     |string | 文件id | 是   |

- 返回结果
```
{
	"success": 1,
	"data": {
		"file_size": 1000,
		"create_timestamp": 11111111,
		"crc32": 123131
	}
}
```
-  结果描述  

| 名称 | 类型 | 描述 | 
| -------- | ---- | ---- | 
| file_size     |number | 文件字节大小 |
| create_timestamp     |number |文件创建时间戳  |
| crc32     |number | 文件id |文件crc32 值|


####  /file/preview

- GET 

- 参数

| 参数名称 | 类型 | 描述 | 必选 |
| -------- | ---- | ---- | ---- |
| fileId     |string | 文件id | 是   |
| previewType     |number | 预览类型 1 pdf转换图片,默认直接返回文件流 | 否    |

返回结果  
 文件流(或转换后的文件数据流)
 ####  /file/hello
 - GET
 - 参数  
 无
 - 返回结果  
  http status 200