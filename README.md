# little-file

#### 项目介绍
小型文件管理http服务,为原有的文件系统提供可靠的http服务目前支持:fatdfs,ftp,sftp,local(本地文件)图片在线预览,支持gzip压缩简单封装了,fastdfs,sftp,ftp基本操作(上传,下载)
简单安全控制 hmac
可在线预览pdf,图片等文件,后续可支持文件缩放,剪裁等功能
#### 软件架构
客户端文件请求(携带参数:文件服务器路径,token,访问类型(查看或者下载))->->web服务,拉取远端文件到Web服务器所在本地磁盘->返回文件流信息(可选gzip压缩)

#### 配置详解
```
#文件服务器类型 FTP,FDFS,SFTP,LOCAL(本地模式);
littlefile.server_type=FDFS


#文件缓存时间:s秒，min分钟,h小时,d天  0 不清理 ,可选
littlefile.file_cache_time=30min
#文件清理线程数,可选
littlefile.file_clean_thread_num=3
#是否对文件进行gzip压缩
littlefile.file.gzip=false

#webapp:xx abs:xx webapp表示为web环境下目录 abs表示绝对路径
#如webapp:file或 abs:l:/tmp
littlefile.file_cache_dir=webapp:file

#安全控制,可选配置  支持 md4,sha1,sha256
littlefile.token.hmac=md5
#hmac 秘钥
littlefile.token.password=md5

#fdfs配置地址 classpath:xxx或者绝对路径
littlefile.fdfs.fdfs_conf=classpath:fdfs.properties

#sftp配置 
littlefile.sftp.host=192.168.91.201
littlefile.sftp.port=22
littlefile.sftp.username=root
littlefile.sftp.password=123
#工作目录
littlefile.sftp.workdir=/opt/sftp/
#非账户连接配置,可选
# classpath:xxx或者绝对路径
littlefile.sftp.private_key=
littlefile.sftp.private_key_password=



#ftp配置
littlefile.ftp.host=127.0.0.1
littlefile.ftp.port=21
littlefile.ftp.username=ftp
littlefile.ftp.password=ftp
#工作目录
littlefile.ftp.workdir=/
#ftp ssl配置,可选
# classpath:xxx或者绝对路径
littlefile.ftp.key_manager_path=
littlefile.ftp.key_manager_password=
# classpath:xxx或者绝对路径
littlefile.ftp.trust_manager_path=
littlefile.ftp.trust_manager_password=


#本地模式 英文,分割,支持多个本地文件目录
littlefile.local.dirs=L:/tmp2,L:/tmp3
```
### 传统项目使用:在web.xml中配置默认的servlet
```
    <!--   配置servlet,处理文件请求 需要参数:
    f 文件路径
    t 0 下载  1查看
    s token 可选 对f的hmac
             具体实现 参见com.taoyuanx.littlefile.web.FileHandler
   	 如: 下载链接:http://localhost:8080/littlefile-web/down?f=group1/M00/00/01/oYYBAFwjqB6AEatWAABkNxa-me0434.png&t=0&s=DRfo7B8fxDxrnQBR9IfPrA
   		 查看链接:http://localhost:8080/littlefile-web/down?f=group1/M00/00/01/oYYBAFwjqB6AEatWAABkNxa-me0434.png&t=1&s=DRfo7B8fxDxrnQBR9IfPrA
    -->
  <servlet>
  <servlet-name>downLoadServlet</servlet-name>
  <servlet-class>com.taoyuanx.littlefile.web.DownLoadServlet</servlet-class>
   <init-param>
      <param-name>liitle_conf</param-name>
      <param-value>classpath:littlefile.properties</param-value>
    </init-param>
  </servlet>
  <servlet-mapping>
  <servlet-name>downLoadServlet</servlet-name>
  <url-pattern>/down</url-pattern>
  </servlet-mapping>
</web-app>


```


### 其他环境
```
实例化 com.taoyuanx.littlefile.web.FileHandler 类,或扩展该类暴露http服务

```

### 安全控制
项目中实现了简单的安全控制,其他外部系统可引入littlefile-core包构造访问路径即可
安全控制实现参见:com.taoyuanx.littlefile.web.security.HmacTokenManager,用户亦可自行实现

### 使用
部分参数解释如上,也可参见示例项目:littlefile-web
1.上传文件<br/>
参见 LittleFile系列实现
2.创建授权url(com.taoyuanx.littlefile.web.FileHandler.signFileUrl),
未开启安全控制时,创建公开url(com.taoyuanx.littlefile.web.FileHandler.createPublicUrl)
参见 FileAuthedUrlCreate<br/>
3.访问

#### 仓库地址

**git地址:**[https://github.com/dushitaoyuan/little-file](https://github.com/dushitaoyuan/little-file)



**码云地址:**[https://gitee.com/taoyuanx/little-file](https://gitee.com/taoyuanx/little-file) 
