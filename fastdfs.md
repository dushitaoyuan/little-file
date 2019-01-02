#fastdfs全面详解

#### fastdfs架构简介
首先简单了解一下基础概念，FastDFS是一个开源的轻量级分布式文件系统，由跟踪服务器（tracker server）、存储服务器（storage server）和客户端（client）三个部分组成，主要解决了海量数据存储问题，特别适合以中小文件（建议范围：4KB < file_size <500MB）为载体的在线服务，如果需要对外提供http服务需要在storage server上安装nginx+fastdfs_module服务。FastDFS的系统结构图如下：

![avatar](https://github.com/dushitaoyuan/little-file/blob/master/jieshao.png)
<br/>
如上图,FastDFS的两个核心概念分别是：<br/>
1.Tracker（跟踪器）<br/>
2.Storage（存储节点）<br/>

Tracker主要做调度工作，相当于mvc中的controller的角色，在访问上起负载均衡的作用。跟踪器和存储节点都可以由一台或多台服务器构成，跟踪器和存储节点中的服务器均可以随时增加或下线而不会影响线上服务，其中跟踪器中的所有服务器都是对等的，可以根据服务器的压力情况随时增加或减少。Tracker负责管理所有的Storage和group，每个storage在启动后会连接Tracker，告知自己所属的group等信息，并保持周期性的心跳，tracker根据storage的心跳信息，建立group==>[storage server list]的映射表，Tracker需要管理的元信息很少，会全部存储在内存中；另外tracker上的元信息都是由storage汇报的信息生成的，本身不需要持久化任何数据，这样使得tracker非常容易扩展，直接增加tracker机器即可扩展为tracker cluster来服务，cluster里每个tracker之间是完全对等的，所有的tracker都接受stroage的心跳信息，生成元数据信息来提供读写服务。
Storage采用了分卷[Volume]（或分组[group]）的组织方式，存储系统由一个或多个组组成，组与组之间的文件是相互独立的，所有组的文件容量累加就是整个存储系统中的文件容量。一个卷[Volume]（组[group]）可以由一台或多台存储服务器组成，一个组中的存储服务器中的文件都是相同的，组中的多台存储服务器起到了冗余备份和负载均衡的作用，数据互为备份，存储空间以group内容量最小的storage为准，所以建议group内的多个storage尽量配置相同，以免造成存储空间的浪费。
工作流程:<br/>
![avatar](https://github.com/dushitaoyuan/little-file/blob/master/liucheng.png)
<br/>
1.client连接tracker server，tracker server查询storage server 列表，根据配置的负载均衡算法（随机，第一 storage server，第一storage server 优先级排序（最小值）），返回一个storage ip <br/>
2. client 根据返回的ip连接 storage server 进行上传 <br/>
3. storage server 根据配置的存储路径，根据指定的负载算法（随机，最大剩余空间）选择一个路径存储到磁盘上，并返回结果 <br/>
备注：<br/>
Fastdfs 只提供文件的基本操作，没有对文件重复上传做处理，如果业务上需要实现文件重复上传可使用fastdfs配合fastdht处理，也可以利用业务系统自身实现（推荐),前者无疑增加了系统复杂度，fastdht是一种分布式的kv存储数据库，以Berkeley DB作为数据存储。

#### 安装和使用
安装教程:[安装地址](https://github.com/happyfish100/fastdfs/wiki) <br/>
客户端地址:[客户端地址](https://github.com/happyfish100/fastdfs-client-java) <br/>
服务端源码地址:[服务端](客户端地址:[客户端地址](https://github.com/happyfish100/fastdfs-client-java) <br/>
#####使用
```
fdfs命令详解：
启动：
tracker 启动: fdfs_trackerd /etc/fdfs/tracker.conf start
storage 启动: fdfs_storaged /etc/fdfs/storage.conf start
关闭  
#查询fdfs进程id
ps -aux|grep fdfs
Kill -15 进程id
重启：
fdfs_trackerd  tracker.conf restart
fdfs_storaged  tracker.conf restart

查询集群信息：
fdfs_monitor  /etc/fdfs/storage.conf
命令格式：

Usage: fdfs_monitor <config_file> [-h <tracker_server>] [list|delete|set_trunk_server <group_name> [storage_id]]

Nginx 命令：
#启动nginx
/usr/local/nginx/sbin/nginx 
#热加载配置
/usr/local/nginx/sbin/nginx -s reload 
#停止nginx
/usr/local/nginx/sbin/nginx -s stop 

设置开机自启动：
#自启动storage服务
chkconfig fdfs_storaged /xxx/fdfs/storage.conf  on 

```


###fastdfs配置详解


####tracker.conf 配置详解
```
#是否启用本配置问价true|false
disabled=false
#绑定本机ip address  为空绑定所有本机所有ip
bind_addr=10.120.21.13
#端口
port=22122
#连接超时时间 （s）
connect_timeout=30
network_timeout=60
#存储tracker server 数据和日志的基础路径
base_path=/fastdfs/tracker/
#tracker server 最大连接数
max_connections=256
#处理连接事件线程数
accept_threads=1
#工作线程数
work_threads=8
#最大最小缓冲空间大小
min_buff_size = 8KB
max_buff_size = 256KB

#上传时 存储group负载均衡算法：
#0 随机
#1 指定
#2  选择最大剩余空间的group
store_lookup=2
# 指定存储组，当 store_lookup=1 时生效
store_group=group2
# 组内storage server 负载算法
#0 随机
#1 组内第一个有限
#2 组内第一个权重排序
# 备注: 如果 use_trunk_file=true, store_server 必须为 1或2
store_server=0

# 一个服务器有多个存储路径（多磁盘挂载点）的负载算法
# 0: 随机上传
# 2:  选择最大剩余空间的挂载点 上传 
store_path=0
#下载文件负载均衡算法
# 0: 随机
# 1: 源文件服务器
download_server=0
#保留系统或其他应用程序的存储空间


#如果任何group的某个storage server的空闲（可用）空间
#<＝reserved_storage_space;那么没有文件可以上传到这个组。
#字节单位可以是如下之一：
#千兆字节 G
#兆字节   M 
#千字节   K
#字节单位 B
#或者 xx.xx% 如保留值为10.01%
reserved_storage_space = 10%
#日志级别
### emerg for emergency
### alert
### crit for critical
### error
### warn for warning
### notice
### info
### debug
log_level=info
# 运行当前程序的 unix group 为空 则为当前user 所在的group
run_by_group=
#运行当前程序的 unix  user 为空 则为当前 user
run_by_user=

# allow_hosts 属性可出现一次或多次, value 可以是 ip 或 hostname,
# "*" (only one asterisk) means match all ip addresses
#  如： CIDR ips 192.168.5.64/26
# ip 范围 10.0.1.[0-254] and host[01-08,20-25].domain.com
# 如:
# allow_hosts=10.0.1.[1-15,20]
# allow_hosts=host[01-08,20-25].domain.com
# allow_hosts=192.168.5.64/26
allow_hosts=*
#从缓冲区刷新日志到磁盘的时间间隔（s）
sync_log_buff_interval = 10
# 检查 storage server 存储的时间间隔（s）
check_active_interval = 120
#（线程 stack 大小）
thread_stack_size = 64KB
#当存储服务器的IP地址改变时自动调整
storage_ip_changed_auto_adjust = true
#storage server 同步文件 最大延时 秒数 默认 86400 一天
storage_sync_file_max_delay = 86400
#同步单个文件耗时最大秒数 默认300s
storage_sync_file_max_time = 300
#是否开启小文件合并存储 trunk存储
use_trunk_file = false 
#trunk文件最小分配单元
slot_min_size = 256
#trunk内部存储的最大文件，超过该值会被独立存储
slot_max_size = 16MB
#trunk文件大小
trunk_file_size = 64MB
#是否预先创建trunk文件
trunk_create_file_advance = false
#预先创建trunk文件的基准时间
trunk_create_file_time_base = 02:00
#预先创建trunk文件的时间间隔
trunk_create_file_interval = 86400
#trunk创建文件的最大空闲空间
trunk_create_file_space_threshold = 20G
#启动时是否检查每个空闲空间列表项已经被使用
trunk_init_check_occupying = false
#是否纯粹从trunk-binlog重建空闲空间列表
trunk_init_reload_from_binlog = false
#对trunk-binlog进行压缩的时间间隔
trunk_compress_binlog_min_interval = 0
#是否使用 storage_ids配置
use_storage_id = false
#存储storage_ids配置的文件名称
storage_ids_filename = storage_ids.conf
#文件名中id类型 id|ip 
# ip storage server ip
# id storage server id
id_type_in_filename = ip
#如果存储从文件使用符号链接
store_slave_file_use_link = false
#是否 每天 rotate error log 
rotate_error_log = false
#每天 rotate 时间
error_log_rotate_time=00:00
# rorate log 代销
rotate_error_log_size = 0
#日志保存天数
log_file_keep_days = 0
#是否使用连接池
use_connection_pool = false
#连接池连接空闲时间
connection_pool_max_idle_time = 3600


#tracker server HTTP 端口 5.x版本取消内置http
http.server_port=8080
# 检查 storage HTTP server 存活 间隔 seconds
http.check_alive_interval=30
http.check_alive_type=tcp
http.check_alive_uri=/status.html

```
####tracker.conf 配置详解
```
#是否启用这个配置
disabled=false

# 当前 storage server 所有的group
#
# 注释或删除 group_name,将从tracker server 获取 ,但必须保证tracker.conf 中 use_storage_id=true
#并且 storage_ids.conf 必须正确配置好
group_name=group1

#绑定本机ip address  为空绑定所有本机所有ip
bind_addr=

#  如果绑定当前主机的某个ip地址是时，当本 storage server 连接 其他 servers(当前storage server 作为一客户端)
# true 就使用 bind_addr的值
# false 使用 当前storage server 的所有ip
client_bind=true

#端口
port=23000

#连接超时时间
connect_timeout=30
network_timeout=60

# 心跳间隔
heart_beat_interval=30

# 磁盘使用情况报告间隔 秒数 
stat_report_interval=60


# storage server 存储数据和日志的基础路径
base_path=/opt/fdfs_storage

#最大连接数
max_connections=256


#缓冲区大小 （接收和发送）
buff_size = 256KB


#接收线程数
accept_threads=1

#工作线程数
work_threads=8

#磁盘是否读写分离
disk_rw_separated = true

#磁盘 读线程数
disk_reader_threads = 1

#磁盘写线程数
disk_writer_threads = 1

#当没有进入同步时，尝试在sync_wait_msec毫秒之后再次读取BILCONG
#必须大于0，默认值为200毫秒
sync_wait_msec=50

# after sync a file, usleep milliseconds
# 0 for sync successively (never call usleep)

sync_interval=0

# 每天同步开始时间 时间格式： Hour:Minute
# Hour from 0 to 23, Minute from 0 to 59
sync_start_time=00:00

# 每天同步结束时间  时间格式： Hour:Minute
# Hour from 0 to 23, Minute from 0 to 59
sync_end_time=23:59

#在同步N文件之后写入标记文件
#默认值为500
write_mark_file_freq=500

# 存储 path(磁盘或挂载点) 个数1
store_path_count=1

# store_path#, 从0开始, 如果store_path0未设置，store_path0 值为base_path的值  
#如：
store_path0=/fastdfs/store/data


# 第二个存储路径（或挂载点）
#store_path1=/home/yuqing/fastdfs2

# 存储路径 store_path下的子目录数 （subdir_count  * subdir_count）二级
#取值范围 1-256 默认 256
subdir_count_per_path=256

# tracker_server 属性可以出现多次 格式 host:port host 可以是hostname

tracker_server=10.120.21.13:22122

#standard log level as syslog, case insensitive, value list:
### emerg for emergency
### alert
### crit for critical
### error
### warn for warning
### notice
### info
### debug
log_level=info

# 运行当前程序的 unix group 为空 则为当前user 所在的group
run_by_group=

#运行当前程序的 unix  user 为空 则为当前 user
run_by_user=

# allow_hosts 属性可出现一次或多次, value 可以是 ip 或 hostname,
# "*" (only one asterisk) means match all ip addresses
#  如： CIDR ips 192.168.5.64/26
# ip 范围 10.0.1.[0-254] and host[01-08,20-25].domain.com
# 如:
# allow_hosts=10.0.1.[1-15,20]
# allow_hosts=host[01-08,20-25].domain.com
# allow_hosts=192.168.5.64/26
allow_hosts=*



# the mode of the files distributed to the data path
# 0: round robin(default)
# 1: random, distributted by hash code
#文件分发到数据路径的模式
#0：循环（默认）
#1：随机，用hash分配
file_distribute_path_mode=0


#当文件分配到file_distribute_path_mode=0 （循环）时，
#当写入文件计数达到这个数时，然后旋转到下一个路径，默认值为100
file_distribute_rotate_count=100

#当写入大文件到磁盘时
# 0: never call fsync
# other: call fsync when written bytes >= this bytes
# default value is 0 (never call fsync)
fsync_after_written_bytes=0


# 每隔间隔秒同步日志到磁盘 必须大于0，默认值为10秒
sync_log_buff_interval=10

# sync binlog buff / cache to disk every interval seconds
# default value is 60 seconds
#从 buff / cache 同步 binlog 日志到磁盘的时间间隔，默认值为60秒
sync_binlog_buff_interval=10


# 同步 storage 监控信息到磁盘时间间隔 默认 300s
sync_stat_file_interval=300


#线程stack 大小 默认 512kb
thread_stack_size=512KB
#作为上传文件的源服务器的优先级
# 值越低，优先级越高，默认10
upload_priority=10

#网卡别名前缀
# the NIC alias prefix, such as eth in Linux, you can see it by ifconfig -a
# multi aliases split by comma. empty value means auto set by OS type
# default values is empty
if_alias_prefix=

# 检查文件是否重复，如果检查文件重复，当设置为true时，使用FASDHT存储文件索引
# 1 or yes: 检查
# 0 or no: 不检查
# default value is 0
check_file_duplicate=0


# 文件签名算法 用于检查文件重复 hash
## hash: four 32 bits hash code
## md5: MD5 signature
# default value is hash
# since V4.01
file_signature_method=hash


# 存储 文件索引的命名空间 (key-value pairs)
# 当前  check_file_duplicate 为true时，key_namespace 必须有值
key_namespace=FastDFS

# 1 表示将和FastDHT servers 建立持久连接，0 表示短连接
keep_alive=0

# you can use "#include filename" (not include double quotes) directive to 
# load FastDHT server list, when the filename is a relative path such as 
# pure filename, the base path is the base path of current/this config file.
# must set FastDHT server list when check_file_duplicate is true / on
# please see INSTALL of FastDHT for detail
##include /home/yuqing/fastdht/conf/fdht_servers.conf

# if log to access log
# default value is false
# since V4.00
# 是否开启访问日志
use_access_log = false


#是否 每天 rotate access log
rotate_access_log = false

# 每天 rotate access log时间 时间格式  Hour:Minute
# Hour from 0 to 23, Minute from 0 to 59
access_log_rotate_time=00:00

# if rotate the error log every day
# default value is false
# since V4.02
#是否 每天 rotate error日志
rotate_error_log = false

# 每天 rotate error  log时间 时间格式  Hour:Minute
# Hour from 0 to 23, Minute from 0 to 59
error_log_rotate_time=00:00


#当 日志大小超过 rotate_access_log_size大小 rotate access 
rotate_access_log_size = 0
#当 日志大小超过 rotate_error_log_size大小 rotate error 
rotate_error_log_size = 0

# 日志保留天数 0 表示 从不删除历史日志
log_file_keep_days = 0


# 文件同步 跳过无效记录
file_sync_skip_invalid_record=false
# 是否使用 连接池
use_connection_pool = false

# 连接池空闲时间
connection_pool_max_idle_time = 3600

#如果domain_name名称为空，则使用此存储服务器的IP地址；
#否则这个域名将在tracke server 重定向的URL中出现混乱。
http.domain_name=

#这个存储服务器上的Web服务器的端口 
http.server_port=8888

```
####client.conf配置

```

# 客户端连接超时时间 秒
connect_timeout=30
network_timeout=60

# 日志路径
base_path=/opt/fastdfs_client

# tracker_server can ocur more than once, and tracker_server format is
#  "host:port", host can be hostname or ip address

tracker_server=10.120.21.13:22122

#standard log level as syslog, case insensitive, value list:
### emerg for emergency
### alert
### crit for critical
### error
### warn for warning
### notice
### info
### debug
log_level=info

# 是否使用连接池
use_connection_pool = false

#池中连接最大空闲时间
connection_pool_max_idle_time = 3600


load_fdfs_parameters_from_tracker=false
use_storage_id = false
storage_ids_filename = storage_ids.conf

#HTTP settings
http.tracker_server_port=80

#use "#include" directive to include HTTP other settiongs
##include http.conf

```


####建议配置
```
建议配置
tracker.conf
port=22122  
base_path=/home/dfs 
#根据系统参数配置
max_connections=256
accept_threads=1
work_threads=4
#集群负载均衡算法（参见解释）
store_server=0
store_path=0
download_server=0
建议指定内网ip
bind_addr=192.168.91.201

#如果需要考虑后期数据迁移，建议 开启
use_storage_id = true
storage_ids_filename = storage_ids.conf
id_type_in_filename = id



storage.conf

port=23000 
base_path=/home/dfs 
store_path0=/home/dfs 
tracker_server=192.168.52.1:22122 
#tracker 开启use_storage_id = true 可以不填写，不过建议与storage_ids.conf 保持一致
group_name=
#根据系统参数配置
max_connections=256
accept_threads=1
work_threads=4

#不使用nginx时，可以不管 
http.server_port=8888  
#建议指定内网ip
bind_addr=192.168.91.201
#集群负载均衡算法（参见解释）
store_server=0
store_path=0
download_server=0





storage_ids.conf
# <id>  <group_name>  <ip_or_hostname>
100001   group1  192.168.91.201
100002   group2  192.168.91.202


```
###使用特性
备份：<br/>
根据fastdfs的设计原理，同group中storage server 会进行互备，所以开启备份保证每个group中包含一台或多台即可，另外不同group的备份时间尽量错开<br/>
也可使用 raid技术备份<br/>

迁移：<br/>
Tracker server开启 use_storage_id = true 维护storage_ids.conf 便于迁移和维护，storage server ip变更维护方便<br/>

如果开启 use_storage_id = true<br/>
storage server 迁移 base_path 和 store_path 目录<br/>
tracker server 迁移base path<br/>
修改 storage_ids.conf <br/>
并修改 相应ip配置<br/>

未开启 use_storage_id = true<br/>
storage server 迁移 base_path 和 store_path 目录<br/>
tracker server 迁移base path<br/>
并修改 相应ip配置<br/>
修改 storage base_path 和 tracker server base_path 下dat数据信息<br/>
Tracker<br/>

![avatar](https://github.com/dushitaoyuan/little-file/blob/master/t1.png)
Storage

<br/>
![avatar](https://github.com/dushitaoyuan/little-file/blob/master/t2.png)

步骤:
1.迁移storage<br/>
2.修改tracker 配置（或数据信息） 重启 tracker，迁移后的storage 加入tracker<br/>




###http访问
单机http访问
小集群可使用 little-file支持http
