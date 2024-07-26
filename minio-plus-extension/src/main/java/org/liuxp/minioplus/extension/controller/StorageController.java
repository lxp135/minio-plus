package org.liuxp.minioplus.extension.controller;

import cn.hutool.core.io.IoUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.liuxp.minioplus.api.StorageService;
import org.liuxp.minioplus.api.model.vo.CompleteResultVo;
import org.liuxp.minioplus.api.model.vo.FileCheckResultVo;
import org.liuxp.minioplus.api.model.vo.FilePreShardingVo;
import org.liuxp.minioplus.common.enums.MinioPlusErrorCode;
import org.liuxp.minioplus.common.enums.StorageBucketEnums;
import org.liuxp.minioplus.common.exception.MinioPlusException;
import org.liuxp.minioplus.extension.context.Response;
import org.liuxp.minioplus.extension.context.UserHolder;
import org.liuxp.minioplus.extension.dto.FileCheckDTO;
import org.liuxp.minioplus.extension.dto.FileCompleteDTO;
import org.liuxp.minioplus.extension.dto.PreShardingDTO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;

/**
 * 对象存储标准接口定义
 * 本类的方法是给前端使用的方法
 * @author contact@liuxp.me
 * @since 2024/6/18
 */
@Controller
@RequestMapping("/storage")
@Api(tags = "MinIO Plus Demo 接口")
@Slf4j
public class StorageController {

    /**
     * 重定向
     */
    private static final String REDIRECT_PREFIX = "redirect:";

    /**
     * 图标请求地址
     */
    private static final String ICON_PATH = "/storage/icon/";

    /**
     * 存储引擎Service接口定义
     */
    @Resource
    private StorageService storageService;

    /**
     * 文件预分片方法
     * 在大文件上传时，为了防止前端重复计算文件MD5值，提供该方法
     * @return 预分片结果
     */
    @ApiOperation(value = "文件预分片")
    @PostMapping("/upload/sharding")
    @ResponseBody
    public Response<FilePreShardingVo> sharding(@RequestBody @Validated PreShardingDTO preShardingDTO){

        FilePreShardingVo resultVo = storageService.sharding(preShardingDTO.getFileSize());

        return Response.success(resultVo);
    }

    /**
     * 上传任务初始化
     * 上传前的预检查：秒传、分块上传和断点续传等特性均基于该方法实现
     * @param fileCheckDTO 文件预检查入参
     * @return 检查结果
     */
    @ApiOperation(value = "上传任务初始化")
    @PostMapping("/upload/init")
    @ResponseBody
    public Response<FileCheckResultVo> init(@RequestBody @Validated FileCheckDTO fileCheckDTO) {

        // 取得当前登录用户信息
        String userId = UserHolder.get();

        FileCheckResultVo resultVo = storageService.init(fileCheckDTO.getFileMd5(),fileCheckDTO.getFullFileName(),fileCheckDTO.getFileSize(),fileCheckDTO.getIsPrivate(),userId);

        return Response.success(resultVo);
    }

    /**
     * 文件上传完成
     * @param fileKey 文件KEY
     * @param fileCompleteDTO 文件完成入参DTO
     * @return 是否成功
     */
    @ApiOperation(value = "上传完成")
    @PostMapping("/upload/complete/{fileKey}")
    @ResponseBody
    public Response<Object> complete(@PathVariable("fileKey") String fileKey, @RequestBody FileCompleteDTO fileCompleteDTO) {

        // 取得当前登录用户信息
        String userId = UserHolder.get();

        // 打印调试日志
        log.debug("合并文件开始fileKey="+fileKey+",partMd5List="+fileCompleteDTO.getPartMd5List());
        CompleteResultVo completeResultVo = storageService.complete(fileKey,fileCompleteDTO.getPartMd5List(),userId);

        return Response.success(completeResultVo);
    }

    /**
     * 文件下载
     * @param fileKey 文件KEY
     * @return 文件下载地址
     */
    @ApiOperation(value = "文件下载")
    @GetMapping("/download/{fileKey}")
    public String download(@PathVariable String fileKey)  {

        // 取得当前登录用户信息
        String userId = UserHolder.get();

        // 取得文件读取路径
        return REDIRECT_PREFIX + storageService.download(fileKey, userId);
    }

    /**
     * 获取图像
     * @param fileKey 文件KEY
     * @return 原图地址
     */
    @ApiOperation(value = "图片预览 - 原图")
    @GetMapping("/image/{fileKey}")
    public String previewOriginal(@PathVariable String fileKey) {

        // 取得当前登录用户信息
        String userId = UserHolder.get();

        // 取得文件读取路径
        return REDIRECT_PREFIX + storageService.image(fileKey, userId);
    }

    /**
     * 文件预览
     * 当文件为图片时，返回图片的缩略图
     * 当文件不是图片时，返回文件类型图标
     * @param fileKey 文件KEY
     * @return 缩略图地址
     */
    @ApiOperation(value = "图片预览 - 缩略图")
    @GetMapping("/preview/{fileKey}")
    public String previewMedium(@PathVariable String fileKey) {

        // 取得当前登录用户信息
        String userId = UserHolder.get();

        String url = storageService.preview(fileKey, userId);
        if(url.length()<10){
            // 当返回值为文件类型时，取得图标
            url = ICON_PATH + url;
        }

        // 取得文件读取路径
        return REDIRECT_PREFIX + url;
    }

    /**
     * 根据文件类型取得图标
     * @param response HttpServletResponse
     */
    @ApiOperation(value = "获取图标")
    @GetMapping("/icon/{fileType}")
    public void icon(HttpServletResponse response,@PathVariable String fileType) {
        try {

            // 根据文件后缀取得桶
            String storageBucket = StorageBucketEnums.getBucketByFileSuffix(fileType);

            ClassPathResource cpr = new ClassPathResource(storageBucket+".png");

            byte[] bytes = FileCopyUtils.copyToByteArray(cpr.getInputStream());

            response.setHeader("content-disposition", "inline");
            response.setHeader("Content-Length", String.valueOf(bytes.length));
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

            IoUtil.copy(inputStream, response.getOutputStream());
            inputStream.close();

        } catch (Exception e) {
            log.error(MinioPlusErrorCode.FILE_ICON_FAILED.getMessage(),e);
            // 图标获取失败
            throw new MinioPlusException(MinioPlusErrorCode.FILE_ICON_FAILED);
        }
    }

}