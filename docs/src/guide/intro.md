<h1 align="center">MinIO Plus</h1>
<h3 align="center">我们的愿景是成为 MinIO 最好的搭档</h3>
<p>
  <a style="float: left;padding-left:110px;padding-right: 5px;" target="_blank" href="https://central.sonatype.com/search?q=me.liuxp.minio-plus-all-spring-boot-starter">
      <img src="https://img.shields.io/maven-central/v/me.liuxp/minio-plus-core.svg?label=Maven%20Central" />
  </a>
  <a style="float: left;padding-right: 5px;" target="_blank" href="https://www.apache.org/licenses/LICENSE-2.0">
      <img src="https://img.shields.io/badge/license-Apache%202-green.svg" alt="开源协议" />
  </a>
  <a style="float: left;padding-right: 5px;" target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
      <img src="https://img.shields.io/badge/JDK-8+-red.svg" alt='JDK版本'/>
  </a>
  <a style="float: left;padding-right: 5px;" href='https://gitee.com/lxp135/minio-plus'>
      <img src='https://gitee.com/lxp135/minio-plus/badge/star.svg?theme=dark' alt='star' />
  </a>
  <a style="float: left;padding-right: 5px;" href="https://gitee.com/lxp135/minio-plus">
    <img src="https://gitee.com/lxp135/minio-plus/badge/fork.svg?theme=dark" alt="Gitee fork">
  </a>
  <br />
</p>


---

# 介绍

[MinIO-Plus](https://gitee.com/lxp135/minio-plus/) 是一个 [MinIO](https://github.com/minio/minio) 的二次封装与增强工具，在
MinIO 的基础上只做增强，不侵入 MinIO 代码，只为简化开发、提高效率而生。成为 MinIO 在项目中落地的润滑剂。

---

# 特性

* **无侵入** ：只做增强不做改变，引入 `minio-plus` 不会对现有工程产生影响。
* **文件秒传** ：对每个上传的文件进行哈希摘要识别，用户上传同一个文件时，没有文件实际传输过程，做到秒传。
* **并发上传** ：将文件切分为小块。同时并发上传多个小块，最大限度地利用带宽，加快上传速度。
* **断点续传** ：在传输过程中遇到问题导致传输失败，只需重新传输未完成的小块，而不需要重新开始整个传输任务。
* **缩略图生成** ：识别文件类型，在图片上传时自动生成缩略图，缩略图大小可配置。
* **自动桶策略** ：按照文档、压缩包、音频、视频、图片等类型自动建桶，按照 `/年/月` 划分路径，避免受到操作系统文件目录体系影响导致性能下降。
* **访问权限控制** ：可支持基于用户、组的文件权限控制，保证重要文件的安全性。
* **访问链接时效** ：基于 MinIO 的临时链接创建策略，提供具备有效期并预签名的上传与下载地址。
* **客户端直连** ：前端直连 MinIO ，项目工程不做文件流的搬运，在支持以上特性的情况下提供 MinIO 原生性能。

# 目录

[[TOC]]