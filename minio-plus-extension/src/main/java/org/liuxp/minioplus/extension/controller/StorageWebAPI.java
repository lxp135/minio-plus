package org.liuxp.minioplus.extension.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.liuxp.minioplus.api.model.vo.FileCheckResultVo;
import org.liuxp.minioplus.api.model.vo.FilePreShardingVo;
import org.liuxp.minioplus.extension.context.Response;
import org.liuxp.minioplus.extension.dto.FileCheckDTO;
import org.liuxp.minioplus.extension.dto.FileCompleteDTO;
import org.liuxp.minioplus.extension.dto.PreShardingDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 对象存储标准接口定义
 * 本类的方法是给前端使用的方法
 *
 * @author contact@liuxp.me
 * @since 2024/6/18
 */
@Tag(name = "MinIO Plus 接口")
public interface StorageWebAPI {

    /**
     * 请求地址前缀
     */
    String ROOT_PATH = "/storage";

    /**
     * 图标请求地址
     */
    String ICON_PATH = "/storage/icon/";

    /**
     * 文件预分片方法
     * 在大文件上传时，为了防止前端重复计算文件MD5值，提供该方法
     *
     * @param preShardingDTO 文件预分片入参DTO
     * @return 预分片结果
     */
    @Operation(summary = "文件预分片")
    @PostMapping("/upload/sharding")
    @ResponseBody
    Response<FilePreShardingVo> sharding(@RequestBody @Validated PreShardingDTO preShardingDTO);

    /**
     * 上传任务初始化
     * 上传前的预检查：秒传、分块上传和断点续传等特性均基于该方法实现
     *
     * @param fileCheckDTO 文件预检查入参
     * @return 检查结果
     */
    @Operation(summary = "上传任务初始化")
    @PostMapping("/upload/init")
    @ResponseBody
    Response<FileCheckResultVo> init(@RequestBody @Validated FileCheckDTO fileCheckDTO);

    /**
     * 文件上传完成
     *
     * @param fileKey         文件KEY
     * @param fileCompleteDTO 文件完成入参DTO
     * @return 是否成功
     */
    @Operation(summary = "上传完成")
    @PostMapping("/upload/complete/{fileKey}")
    @ResponseBody
    Response<Object> complete(@PathVariable("fileKey") String fileKey, @RequestBody FileCompleteDTO fileCompleteDTO);

    /**
     * 文件下载
     *
     * @param fileKey 文件KEY
     * @return 文件下载地址
     */
    @Operation(summary = "文件下载")
    @GetMapping("/download/{fileKey}")
    String download(@PathVariable("fileKey") String fileKey);

    /**
     * 获取图像
     *
     * @param fileKey 文件KEY
     * @return 原图地址
     */
    @Operation(summary = "图片预览 - 原图")
    @GetMapping("/image/{fileKey}")
    String previewOriginal(@PathVariable("fileKey") String fileKey);

    /**
     * 文件预览
     * 当文件为图片时，返回图片的缩略图
     * 当文件不是图片时，返回文件类型图标
     *
     * @param fileKey 文件KEY
     * @return 缩略图地址
     */
    @Operation(summary = "图片预览 - 缩略图")
    @GetMapping("/preview/{fileKey}")
    String previewMedium(@PathVariable("fileKey") String fileKey);

    /**
     * 根据文件类型取得图标
     *
     * @param fileType 文件扩展名
     */
    @Operation(summary = "获取图标")
    @GetMapping("/icon/{fileType}")
    void icon(@PathVariable("fileType") String fileType);

}