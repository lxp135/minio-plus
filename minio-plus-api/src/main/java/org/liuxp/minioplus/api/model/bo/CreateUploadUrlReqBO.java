package org.liuxp.minioplus.api.model.bo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 创建上传url
 *
 * @author contact@liuxp.me
 * @since  2023/6/29
 */
@Getter
@Setter
public class CreateUploadUrlReqBO {
    /**
     * 文件md5
     */
    private String fileMd5;
    /**
     * 文件名（含扩展名）
     */
    private String fullFileName;
    /**
     * "文件长度"
     */
    private Long fileSize;
    /**
     * 是否断点续传 0:否 1:是,默认非断点续传
     */
    private Boolean isSequel = Boolean.FALSE;
    /**********************************************************以下参数是断点续传必传的数据-start**************************************************/
    /**
     * 丢失的块号
     */
    private List<Integer> missPartNum;
    /**
     * 文件id
     */
    private String fileKey;
    /**
     * 需要补传的任务号
     */
    private String uploadId;
    /**
     * 存储桶
     */
    private String storageBucket;
    /**
     * 存储路径
     */
    private String storagePath;
    /**********************************************************以上参数是断点续传必传的数据-end**************************************************/

}
