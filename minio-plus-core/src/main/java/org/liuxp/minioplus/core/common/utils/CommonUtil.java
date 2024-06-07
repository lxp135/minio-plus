package org.liuxp.minioplus.core.common.utils;

import cn.hutool.core.date.LocalDateTimeUtil;
import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 对象存储工具类
 * @author contact@liuxp.me
 * @since  2024/05/23
 */
public class CommonUtil {

    /**
     * 取得对象名称
     * @param md5 文件MD5值
     * @return 对象名称
     */
    public static String getObjectName(String md5){
        return CommonUtil.getPathByDate() + "/" + md5;
    }

    /**
     * 根据当前时间取得路径
     * @return 路径
     */
    public static String getPathByDate(){
        return LocalDateTimeUtil.format(LocalDateTimeUtil.now(), "yyyy/MM");
    }

    /**
     * 根据文件流和重设宽度进行图片压缩
     * @param inputStream 文件流
     * @param width 宽度
     * @return 压缩后的图片文件流
     * @throws IOException IO异常
     */
    public static ByteArrayOutputStream resizeImage(InputStream inputStream, int width)throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(inputStream)
                .width(width)
                .outputQuality(0.9f)
                .toOutputStream(outputStream);

        return outputStream;
    }
}
