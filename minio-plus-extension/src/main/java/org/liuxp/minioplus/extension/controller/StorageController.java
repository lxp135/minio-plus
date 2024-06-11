package org.liuxp.minioplus.extension.controller;

import cn.hutool.core.io.IoUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.liuxp.minioplus.api.StorageService;
import org.liuxp.minioplus.extension.dto.FileCheckDTO;
import org.liuxp.minioplus.extension.dto.FileCompleteDTO;
import org.liuxp.minioplus.api.model.vo.CompleteResultVo;
import org.liuxp.minioplus.api.model.vo.FileCheckResultVo;
import org.liuxp.minioplus.extension.context.Response;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * 对象存储标准接口定义
 * 本类的方法是给前端使用的方法
 * @author contact@liuxp.me
 */
@Controller
@RequestMapping("/storage")
@Api(tags = "TOS对象存储")
@Slf4j
public class StorageController {

    /**
     * 重定向
     */
    private static final String REDIRECT_PREFIX = "redirect:";

    /**
     * 存储引擎Service接口定义
     */
    @Resource
    private StorageService storageService;

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
        String userId = "mockUser";

        FileCheckResultVo resultVo = storageService.init(fileCheckDTO.getFileMd5(),fileCheckDTO.getFullFileName(),fileCheckDTO.getFileSize(),fileCheckDTO.getIsPrivate(),userId);

        return Response.success(resultVo);
    }

    /**
     * 文件上传完成
     * @param fileKey 文件KEY
     * @return 是否成功
     */
    @ApiOperation(value = "上传完成")
    @PostMapping("/upload/complete/{fileKey}")
    @ResponseBody
    public Response<Object> complete(@PathVariable("fileKey") String fileKey, @RequestBody FileCompleteDTO fileCompleteDTO) {

        // 取得当前登录用户信息
        String userId = "mockUser";

        // 打印调试日志
        log.debug("合并文件开始fileKey="+fileKey+",partMd5List="+fileCompleteDTO.getPartMd5List());
        CompleteResultVo completeResultVo = storageService.complete(fileKey,fileCompleteDTO.getPartMd5List(),userId);

        return Response.success(completeResultVo);
    }

    /**
     * 图片上传
     * @param fileKey 文件KEY
     * @param request 文件流
     * @return 是否成功
     */
    @ApiOperation(value = "图片上传")
    @PutMapping("/upload/image/{fileKey}")
    @ResponseBody
    public Response<Boolean> uploadImage(@PathVariable String fileKey, HttpServletRequest request) {
        Boolean isUpload = false;
        InputStream inputStream = null;
        try {
            inputStream = request.getInputStream();
            isUpload =  storageService.uploadImage(fileKey, IoUtil.readBytes(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IoUtil.close(inputStream);
        }

        return Response.success(isUpload);
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
        String userId = "mockUser";
        // 取得文件读取路径
        return REDIRECT_PREFIX + storageService.download(fileKey, userId);
    }

    /**
     * 图片原图预览
     * @param fileKey 文件KEY
     * @return 原图地址
     */
    @ApiOperation(value = "图片预览 - 原图")
    @GetMapping("/preview/original/{fileKey}")
    public String previewOriginal(@PathVariable String fileKey) {

        // 取得当前登录用户信息
        String userId = "mockUser";
        // 取得文件读取路径
        return REDIRECT_PREFIX + storageService.image(fileKey, userId);
    }

    /**
     * 图片缩略图预览
     * @param fileKey 文件KEY
     * @return 缩略图地址
     */
    @ApiOperation(value = "图片预览 - 缩略图")
    @GetMapping("/preview/medium/{fileKey}")
    public String previewMedium(@PathVariable String fileKey) {

        // 取得当前登录用户信息
        String userId = "mockUser";
        // 取得文件读取路径
        return REDIRECT_PREFIX + storageService.preview(fileKey, userId);
    }

}