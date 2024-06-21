<h1 align="center">MinIO Plus</h1>
<h3 align="center">我们的愿景是成为 MinIO 最好的搭档</h3>

<p align="center">
  <a target="_blank" href="https://central.sonatype.com/search?q=me.liuxp.minio-plus-all-spring-boot-starter">
      <img src="https://img.shields.io/maven-central/v/me.liuxp/minio-plus-core.svg?label=Maven%20Central" alt="maven中央仓库版本号"/>
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

[MinIO-Plus](https://gitee.com/lxp135/minio-plus/) 是一个 [MinIO](https://github.com/minio/minio) 的二次封装与增强工具，在
MinIO 的基础上只做增强，不侵入 MinIO 代码，只为简化开发、提高效率而生。成为 MinIO 在项目中落地的润滑剂。

*我们的开源原则*

* ***我们承诺此项目使用 Apache License 2.0 开源许可证永不变更。***
* ***我们承诺此项目使用永久免费可商用，杜绝文档收费、升级收费、功能收费等情况。***
* ***我们承诺此项目绝不竞价排名，杜绝刷 star 数据、刷 fork 数据，保证项目纯洁。***

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

# 2 项目文档 | Document

* [首页](https://minioplus.liuxp.me/guide/intro.html)
* [更新日志](https://minioplus.liuxp.me/guide/released.html)
* 用户手册
  - [快速开始](https://minioplus.liuxp.me/guide/user/quick-start.html)
  - [API接口](https://minioplus.liuxp.me/guide/user/api.html)
  - [文件元数据](https://minioplus.liuxp.me/guide/user/db.html)
  - [配置文件](https://minioplus.liuxp.me/guide/user/config.html)
  - [非官方S3实现](https://minioplus.liuxp.me/guide/user/custom.html)
* 开发者手册
  - [开发计划](https://minioplus.liuxp.me/guide/developers/plan.html)
  - [构建与运行](https://minioplus.liuxp.me/guide/developers/building.html)
  - [代码结构](https://minioplus.liuxp.me/guide/developers/framework.html)
  - [提交代码](https://minioplus.liuxp.me/guide/developers/writing-code.html)
  - [编写文档](https://minioplus.liuxp.me/guide/developers/writing-documents.html)
  - [贡献者列表](https://minioplus.liuxp.me/guide/developers/contributors.html)
* 核心机制
  - [上传](https://minioplus.liuxp.me/guide/core/upload.html)
  - [下载](https://minioplus.liuxp.me/guide/core/download.html)
  - [客户端直连](https://minioplus.liuxp.me/guide/core/direct.html)
  - [缩略图](https://minioplus.liuxp.me/guide/core/preview.html)
  - [桶策略](https://minioplus.liuxp.me/guide/core/bucket.html)
  - [权限控制](https://minioplus.liuxp.me/guide/core/auth.html)
* 参考资料
  - [FAQ](https://minioplus.liuxp.me/guide/references/faq.html)
  - [MinIO S3 接口](https://minioplus.liuxp.me/guide/references/minio-s3-api.html)
* MinIO 研究
  - [MinIO 分片 ETAG 生成机制](https://minioplus.liuxp.me/guide/study/etag.html)
  - [Nginx 代理](https://minioplus.liuxp.me/guide/study/proxy.html)

*文档访问地址*

* github.io [https://lxp135.github.io/minio-plus-docs/](https://lxp135.github.io/minio-plus-docs/)
* cloudflare镜像 [http://minio-plus-docs.baldhead.cn/minio-plus-docs/](http://minio-plus-docs.baldhead.cn/minio-plus-docs/)
* 国内镜像 [https://minioplus.liuxp.me](https://minioplus.liuxp.me)

*文档仓库地址*

* [https://gitee.com/lxp135/minio-plus-docs](https://gitee.com/lxp135/minio-plus-docs/)
* [https://github.com/lxp135/minio-plus-docs](https://github.com/lxp135/minio-plus-docs/)

# 3 代码托管 | Managed Code

* [https://gitee.com/lxp135/minio-plus](https://gitee.com/lxp135/minio-plus/)
* [https://github.com/lxp135/minio-plus](https://github.com/lxp135/minio-plus/)

以上仓库中代码完全一致，各位同学可根据网络状况自行选择。

# 4 版权 | License

本项目基于 Apache License Version 2.0 开源协议，可用于商业项目。

协议内容：[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

# 5 参与贡献 | Credits

## 5.1 反馈问题

欢迎提交**ISSUE**，请写清楚问题的具体原因，重现步骤和环境。

* Gitee Issue 地址 [https://gitee.com/lxp135/minio-plus/issues](https://gitee.com/lxp135/minio-plus/issues)
* GitHub Issue 地址 [https://github.com/lxp135/minio-plus/issues](https://github.com/lxp135/minio-plus/issues)

## 5.2 微信群

![微信群](wechat_group.jpg)

如果二维码失效，可以加我的微信*movedisk_1*，我会手动拉您入群。