package org.liuxp.minioplus.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.liuxp.minioplus.application.entity.FileMetadataInfoEntity;

/**
 * 文件元数据信息表Mapper
 * @author contact@liuxp.me
 * @since  2024/05/22
 */
@Mapper
public interface FileMetadataInfoMapper extends BaseMapper<FileMetadataInfoEntity> {

}