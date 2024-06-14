package org.liuxp.minioplus.api.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件完整性校验结果VO
 *
 * @author contact@liuxp.me
 * @since 2023-06-26
 **/
@Getter
@Setter
@Schema(description = "文件完整性校验结果")
public class CompleteResultVo {

    @Schema(description = "是否完成")
    private Boolean isComplete;

    @Schema(description = "上传任务编号")
    private String uploadTaskId;

    @Schema(description = "补传的分块信息")
    private List<FileCheckResultVo.Part> partList = new ArrayList<>();

}