package org.liuxp.minioplus.application.schedule.task;

import lombok.extern.slf4j.Slf4j;
import org.liuxp.minioplus.api.StorageService;
import org.liuxp.minioplus.api.model.dto.FileMetadataInfoDTO;
import org.liuxp.minioplus.api.model.vo.FileMetadataInfoVo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 定时任务，演示工程定期清理文件
 * @author contact@liuxp.me
 * @since 2024/06/17
 */
@Component
@Slf4j
public class CleanTask {

    @Resource
    private StorageService storageService;

    @Scheduled(cron = "0 */1 * * * ?")
    public void clean(){

        List<FileMetadataInfoVo> fileList = storageService.list(new FileMetadataInfoDTO());

        for (FileMetadataInfoVo infoVo : fileList) {
            log.info("文件清理，文件key="+infoVo.getFileKey());
            try{
                storageService.remove(infoVo.getFileKey());
            }catch (Exception e){
                log.error(infoVo.getFileKey()+"文件清理失败",e);
            }
        }
    }

}