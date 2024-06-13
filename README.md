<h1 align="center">MinIO Plus</h1>
<h3 align="center">我们的愿景是成为 MinIO 最好的搭档</h3>

<p align="center">
  <a target="_blank" href="https://central.sonatype.com/search?q=me.liuxp.minio-plus-all-spring-boot-starter">
      <img src="https://img.shields.io/maven-central/v/me.liuxp/minio-plus-core.svg?label=Maven%20Central" />
  </a>
  <a target="_blank" href="https://www.apache.org/licenses/LICENSE-2.0">
      <img src="https://img.shields.io/badge/license-Apache%202-green.svg" alt="开源协议" />
  </a>
  <a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
      <img src="https://img.shields.io/badge/JDK-8+-red.svg" alt='JDK版本'/>
  </a>
  <a href='https://gitee.com/lxp135/minio-plus'>
      <img src='https://gitee.com/lxp135/minio-plus/badge/star.svg?theme=dark' alt='star' />
  </a>
  <a href="https://gitee.com/lxp135/minio-plus">
    <img src="https://gitee.com/lxp135/minio-plus/badge/fork.svg?theme=dark" alt="Gitee fork">
  </a>
  <br />
</p>

---

# 0 简介 | Intro

[MinIO-Plus](https://gitee.com/lxp135/minio-plus/) 是一个 [MinIO](https://github.com/minio/minio) 的二次封装与增强工具，在 MinIO 的基础上只做增强，不侵入 MinIO 代码，只为简化开发、提高效率而生。成为 MinIO 在项目中落地的润滑剂。

/TODO 演示地址

# 1 特性 | Feature

* **无侵入** ：只做增强不做改变，引入它不会对现有工程产生影响，如丝般顺滑。
* **文件秒传** ：对每个上传的文件进行哈希摘要识别，用户上传同一个文件时，没有文件实际传输过程，做到秒传。
* **并发上传** ：将文件切分为小块。同时并发上传多个小块，最大限度地利用带宽，加快上传速度。
* **断点续传** ：在传输过程中遇到问题导致传输失败，只需重新传输未完成的小块，而不需要重新开始整个传输任务。
* **缩略图生成** ：识别文件类型，在图片上传时自动生成缩略图，缩略图大小可配置。
* **自动桶策略** ：按照文档、压缩包、音频、视频、图片等类型自动建桶，按照`/年/月`划分路径，避免受到操作系统文件目录体系影响导致性能下降。
* **访问权限控制** ：可支持基于用户、组的文件权限控制，保证重要文件的安全性。
* **访问链接时效** ：基于 MinIO 的临时链接创建策略，提供具备有效期并预签名的上传与下载地址。
* **前端直连** ：前端直连 MinIO ，项目工程不做文件流的搬运，在支持以上特性的情况下提供 MinIO 原生性能。

# 2 功能设计 | Function Design

项目定位为一个MinIO的Java语言SDK，非独立部署服务，并支持spring-boot自动装配。
用户自行实现数据存储部分，项目仅提供MySQL默认实现。

![模块划分](doc/image/模块划分.png)

* minio-plus-api：MinIO Plus 对外提供的能力接口定义，可以理解为 Service 层接口定义
* minio-plus-core：核心业务逻辑包，可以理解为 minio-plus-api 包的接口实现
* minio-plus-extension：扩展包，该包封装了Controller相关接口，可以理解为 minio-plus-api 包的接口的 Controller 层封装，帮助项目使用时开箱即用
* minio-plus-common：工具类、配置类包
* minio-s3-api
  * minio-s3-api-definition：MinIO Plus 使用的S3规范接口定义
  * minio-s3-api-official：原生SDK实现
  * minio-s3-api-custom：自主实现
* minio-plus-spring-boot-starter
  * minio-plus-all-spring-boot-starter：包含core、extension、common、api
  * minio-plus-core-spring-boot-starter：包含core、common、api
* minio-plus-application
  * minio-plus-application-official：使用原生MinIO SDK与元数据使用MySQL数据库的示例工程
  * minio-plus-application-custom：使用自主实现S3规范与数据使用MySQL数据库的示例工程
  
## 2.1 文件下载 | File Download

![文件下载逻辑时序图](doc/image/文件下载时序图.png)

浏览器向服务端发起文件读取请求，服务端会根据fileKey入参取得文件的元数据信息。获取文件元数据信息后，根据元数据信息中的是否私有字段和所有者字段判断是否具备文件读取权限。

* 当用户具备读取权限时，服务端请求MinIO服务器获取经过预签名的文件访问地址返回给浏览器。
* 当用户不具备读取权限时，返回给浏览器无访问权限提示信息。

浏览器拿到真实文件地址后，读取文件并显示或下载。

## 2.2 文件上传 | File Upload

/TODO 上传流程活动图

### 2.2.1 秒传

![秒传时序图](doc/image/秒传时序图.png)

当用户重复上传相同的文件时，每次都需要执行一次完整的文件上传操作，这造成了文件上传过程的冗余，即浪费了用户的时间和服务器的网络IO，重复文件又占用了不必要的服务器磁盘空间。
针对以上两个问题，minio-plus支持文件秒传特性，解决了传统文件上传中重复文件上传时的问题，提高了文件传输的效率和用户体验，同时减少了文件服务器的存储空间占用。
实现文件秒传的技术问题主要涉及文件唯一标识的生成和文件重复性检测。

* 文件唯一标识生成：在浏览器端，使用MD5哈希算法对待传输文件进行哈希值编码。编码结果为一字符串，作为文件的唯一标识。
* 文件重复性检测：在服务器端，根据接收到的文件唯一标识在数据库中进行搜索。如果在数据库中找到相同的文件唯一标识，那么判断该文件存在且无需再进行文件传输。

### 2.2.2 分片上传

分片上传是一种将大文件划分为多个片段并发或按序上传的技术。它有以下几个好处：

* 提高传输速度：当上传的文件比较大时，将大文件进行分块，同时并发上传多个小块，而不是一整个大文件按顺序上传。这样可以最大限度地利用带宽，从而加快上传速度。
* 支持断点续传：分块上传是断点续传技术的前置条件，要想实现断点续传，必须先支持分块。

![文件上传时序图](doc/image/文件上传时序图.png)

### 2.2.3 断点续传

断点续传依赖于分片技术，是提高可用性的重要手段，优点如下：

* 节省时间、减少网络IO、减少磁盘IO：在文件传输过程中遇到问题导致传输失败时，只需重新传输未完成的分片，而不需要重新开始整个传输任务。
* 增加传输的可靠性：可以避免由于网络波动或其他原因导致整个文件需要重新传输的情况。再也不怕意外断网。在大文件传输时，尤其有用。
* 随时暂停和恢复：用户可以在传输过程中暂停传输或者中断传输，断点续传可以方便地恢复传输任务。

## 2.3 前端直连

当用户进行文件流的上传和下载时，直接访问MinIO服务器（可配置Nginx代理）。

使用minioclient的GetPresignedObjectUrlArgs方法，入参是一个GetPresignedObjectUrlArgs对象，该对象包含了以下属性：

* bucketName：要访问的桶名。
* objectName：要访问的对象名。
* expires：URL的过期时间，单位为秒。

该方法的返回值是一个字符串类型的URL，可以用于访问指定的对象，示例：

```
http://127.0.0.1:9000/test/test123
?response-content-type=application%2Fmsword%22&response-content-disposition=attachment%3Bfilename%3D%22xxx.doc%22
&X-Amz-Algorithm=AWS4-HMAC-SHA256
&X-Amz-Credential=minioadmin%2F20230620%2Fus-east-1%2Fs3%2Faws4_request
&X-Amz-Date=20230620T071735Z
&X-Amz-Expires=60&X-Amz-SignedHeaders=host
&X-Amz-Signature=5be3535042ffe72fedee8a283e7a5afbc2b068c595c16800cf57f089ed891cc5
```

每次前端请求文件时，都会生成预签名文件地址，文件地址中，有日期、时效、签名。MinIO会进行验签，保证安全性。

## 2.4 缩略图

在图片上传时，自动生成缩略图，压缩时比例按照图片原始比例不做变化。使用相同的md5名称分别存入原始图片桶和缩略图桶中。

* 缩略图：默认按照宽度300像素进行等比例压缩

PS：原图尺寸小于缩略图压缩尺寸时，储存原图。

## 2.5 桶策略

* 文档（document）：txt、rtf、ofd、doc、docx、xls、xlsx、ppt、pptx、pdf
* 压缩包（package）：zip、rar、7z、tar、wim、gz、bz2
* 音频（ audio ）：mp3、wav、flac、acc、ogg、aiff、m4a、wma、midi
* 视频（ video ）：mp4、avi、mov、wmv、flv、mkv、mpeg、mpg 、rmvb
* 图片 – 原始（ image ）：jpeg、jpg、png、bmp、webp、gif
* 图片 – 缩略图（ image-preview ）：默认按照宽度 300 像素压缩缩
* 其他（ other ） ：未在上述格式中的文件

其他规则：文件在桶中存储时，按照 /年/月 划分路径。用以规避Linux ext3文件系统下单个目录最多创建32000个目录的问题，参考了阿里云OSS的处理办法。

# 3 接口设计 | Interface Design

## 3.1 Service 层接口

提供文件上传、下载、删除等接口。

### 3.1.1 文件上传任务初始化

### 3.1.2 上传完成

### 3.1.3 取得文件下载地址

### 3.1.4 取得原图地址

### 3.1.5 取得缩略图地址

### 3.1.6 查询文件元数据

### 3.1.7 列表查询文件元数据

### 3.1.8 从字节数组创建文件

### 3.1.9 从输入流创建文件

### 3.1.10 从外部url创建文件

### 3.1.11 读取文件

### 3.1.12 删除文件

### 3.1.13 文件上传（MinIO原生接口）

### 3.1.14 文件下载（MinIO原生接口）

## 3.2 Controller 层接口

如没有特殊需求，也可以不写 Controller 层接口，minio-plus-extension 中提供了 Controller 层接口定义。

### 3.2.1 文件上传任务初始化

### 3.2.2 上传完成

### 3.2.3 文件下载

调用 3.1.3 取得文件下载地址后，返回前端时进行302跳转。

### 3.2.4 图片预览 - 原图

调用 3.1.3 取得原图地址后，返回前端时进行302跳转。

### 3.2.5 图片预览 - 缩略图

调用 3.1.3 取得缩略图地址后，返回前端时进行302跳转。

## 3.3 引用 MinIO 接口

这里给出本项目引用的 MinIO 接口列表。

### 3.3.1 bucketExists 检查文件桶是否存在

### 3.3.2 makeBucket 创建文件桶

### 3.3.3 createMultipartUpload 创建分片上传

### 3.3.4 completeMultipartUpload 合并文件

### 3.3.5 listParts 查询已上传的分片列表

### 3.3.6 getPresignedObjectUrl 获取上传、下载、预览图链接

### 3.3.7 putObject 上传

### 3.3.8 getObject 下载

### 3.3.9 removeObject 删除

# 4 数据库设计 | Database Design

## 4.1 文件元数据信息表 | file_metadata_info

| Name           | Type     | Length | Not Null | Virtual | Key  | Comment                |
| ---------------- | ---------- | -------- | ---------- | --------- | ------ | ------------------------ |
| id             | bigint   | 20     | True     | False   | True | 自增ID                 |
| file_key       | varchar  | 50     | True     | False   |      | 文件KEY                |
| file_md5       | varchar  | 50     | False    | False   |      | 文件MD5值              |
| file_name      | varchar  | 255    | True     | False   |      | 文件名                 |
| file_mime_type | varchar  | 50     | False    | False   |      | MIME类型               |
| file_suffix    | varchar  | 20     | False    | False   |      | 文件后缀               |
| file_size      | bigint   | 20     | False    | False   |      | 文件大小               |
| is_preview     | tinyint  | 1      | False    | False   |      | 预览图 0:无 1:有       |
| is_private     | tinyint  | 1      | False    | False   |      | 是否私有 0:否 1:是     |
| bucket         | varchar  | 20     | True     | False   |      | 存储桶                 |
| bucket_path    | varchar  | 20     | True     | False   |      | 存储桶路径             |
| upload_id      | varchar  | 255    | False    | False   |      | 上传任务id             |
| is_finished    | tinyint  | 1      | True     | False   |      | 状态 0:未完成 1:已完成 |
| is_part        | tinyint  | 1      | False    | False   |      | 是否分块 0:否 1:是     |
| part_number    | int      | 4      | False    | False   |      | 分块数量               |
| create_time    | datetime |        | True     | False   |      | 创建时间               |
| create_user    | varchar  | 255    | True     | False   |      | 创建用户               |
| update_time    | datetime |        | True     | False   |      | 更新时间               |
| update_user    | varchar  | 255    | True     | False   |      | 更新用户               |

```
CREATE TABLE `file_metadata_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `file_key` varchar(50) NOT NULL COMMENT '文件KEY',
  `file_md5` varchar(50) DEFAULT NULL COMMENT '文件md5',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_mime_type` varchar(50) DEFAULT NULL COMMENT 'MIME类型',
  `file_suffix` varchar(20) DEFAULT NULL COMMENT '文件后缀',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小',
  `is_preview` tinyint(1) DEFAULT '0' COMMENT '预览图 0:无 1:有',
  `is_private` tinyint(1) DEFAULT '0' COMMENT '是否私有 0:否 1:是',
  `bucket` varchar(20) NOT NULL COMMENT '存储桶',
  `bucket_path` varchar(20) NOT NULL COMMENT '存储桶路径',
  `upload_id` varchar(255) DEFAULT NULL COMMENT '上传任务id',
  `is_finished` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态 0:未完成 1:已完成',
  `is_part` tinyint(1) DEFAULT NULL COMMENT '是否分块 0:否 1:是',
  `part_number` int(4) DEFAULT NULL COMMENT '分块数量',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user` varchar(255) NOT NULL COMMENT '创建用户',
  `update_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user` varchar(255) NOT NULL COMMENT '更新用户',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `INDEX_KEY` (`file_key`),
  KEY `INDEX_MD5` (`file_md5`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件元数据信息表';
```
# 5 开发计划 | Plan

简单画了一个要实现内容的看板

![开发计划](doc/image/开发计划.png)


# 6 使用 | Getting Started

## 6.1 使用 minio-plus-all-spring-boot-starter

## 6.2 使用 minio-plus-core-spring-boot-starter

# 7 代码托管 | Managed Code

* [https://gitee.com/lxp135/minio-plus](https://gitee.com/lxp135/minio-plus/)
* [https://github.com/lxp135/minio-plus](https://github.com/lxp135/minio-plus/)

以上仓库中代码完全一致，各位同学可根据网络状况自行选择。

# 8 版权 | License

本项目基于 Apache License Version 2.0 开源协议，可用于商业项目。

协议内容：[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

# 9 参与贡献 | Credits

## 9.1 编写代码

* 在 Gitee Fork 项目到自己的仓库
* 把 fork 过去的项目也就是你的项目 pull 到你的本地
* 修改代码
* commit 代码，push 到自己的库
* 登录 Gitee 在你首页可以看到一个 pull request 按钮，点击它，填写一些说明信息，然后提交
* 等待维护者合并或者关闭

## 9.2 反馈问题

欢迎提交**ISSUE**，请写清楚问题的具体原因，重现步骤和环境。

* Gitee Issue 地址 [https://gitee.com/lxp135/minio-plus/issues](https://gitee.com/lxp135/minio-plus/issues)
* GitHub Issue 地址 [https://github.com/lxp135/minio-plus/issues](https://github.com/lxp135/minio-plus/issues)

## 9.3 微信群

![开发计划](doc/image/wechat_group.jpg)

如果二维码失效，可以加我的微信*movedisk_1*，我会手动拉您入群。

# 10 参考资料 | Reference

* [MinIO S3 APIs](doc/minio-s3-api.md)




