package org.liuxp.minioplus.application.schedule.dao;


import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.liuxp.minioplus.api.model.dto.FileMetadataInfoDTO;
import org.liuxp.minioplus.api.model.dto.FileMetadataInfoSaveDTO;
import org.liuxp.minioplus.api.model.dto.FileMetadataInfoUpdateDTO;
import org.liuxp.minioplus.api.model.vo.FileMetadataInfoVo;
import org.liuxp.minioplus.application.schedule.entity.FileMetadataInfoEntity;
import org.liuxp.minioplus.application.schedule.mapper.FileMetadataInfoMapper;
import org.liuxp.minioplus.core.repository.MetadataRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 文件元数据接口实现类
 *
 * @author contact@liuxp.me
 * @since 2024/05/22
 */
@Slf4j
@Service
public class MetadataRepositoryImpl extends ServiceImpl<FileMetadataInfoMapper, FileMetadataInfoEntity> implements MetadataRepository {

    @Override
    public List<FileMetadataInfoVo> list(FileMetadataInfoDTO searchDTO) {

        // 组装查询参数
        QueryWrapper<FileMetadataInfoEntity> queryWrapper = buildParams(searchDTO);

        List<FileMetadataInfoEntity> fileMetadataInfoEntityList = super.list(queryWrapper);

        List<FileMetadataInfoVo> fileMetadataInfoVoList = new ArrayList<>();

        for (FileMetadataInfoEntity fileMetadataInfoEntity : fileMetadataInfoEntityList) {
            FileMetadataInfoVo fileMetadataInfoVo = new FileMetadataInfoVo();
            BeanUtils.copyProperties(fileMetadataInfoEntity, fileMetadataInfoVo);
            fileMetadataInfoVoList.add(fileMetadataInfoVo);
        }

        return fileMetadataInfoVoList;
    }

    @Override
    public FileMetadataInfoVo one(FileMetadataInfoDTO searchDTO) {

        // 组装查询参数
        QueryWrapper<FileMetadataInfoEntity> queryWrapper = buildParams(searchDTO);
        queryWrapper.last("limit 1");

        FileMetadataInfoEntity fileMetadataInfoEntity = super.getOne(queryWrapper);

        FileMetadataInfoVo fileMetadataInfoVo = new FileMetadataInfoVo();

        if(null!=fileMetadataInfoEntity){
            BeanUtils.copyProperties(fileMetadataInfoEntity, fileMetadataInfoVo);
        }

        return fileMetadataInfoVo;
    }

    @Override
    public FileMetadataInfoVo save(FileMetadataInfoSaveDTO saveDTO) {

        FileMetadataInfoEntity fileMetadataInfoEntity = new FileMetadataInfoEntity();
        BeanUtils.copyProperties(saveDTO, fileMetadataInfoEntity);
        fileMetadataInfoEntity.setCreateTime(new Date());
        fileMetadataInfoEntity.setUpdateTime(new Date());

        boolean result = super.save(fileMetadataInfoEntity);

        FileMetadataInfoVo fileMetadataInfoVo = new FileMetadataInfoVo();
        if(result){
            BeanUtils.copyProperties(fileMetadataInfoEntity, fileMetadataInfoVo);
        }

        return fileMetadataInfoVo;
    }

    @Override
    public FileMetadataInfoVo update(FileMetadataInfoUpdateDTO updateDTO) {

        FileMetadataInfoEntity fileMetadataInfoEntity = new FileMetadataInfoEntity();
        BeanUtils.copyProperties(updateDTO, fileMetadataInfoEntity);
        fileMetadataInfoEntity.setUpdateTime(new Date());
        boolean result = super.updateById(fileMetadataInfoEntity);

        FileMetadataInfoVo fileMetadataInfoVo = new FileMetadataInfoVo();
        if(result){
            BeanUtils.copyProperties(fileMetadataInfoEntity, fileMetadataInfoVo);
        }

        return fileMetadataInfoVo;
    }

    @Override
    public Boolean remove(Long id) {
        return super.removeById(id);
    }

    private QueryWrapper<FileMetadataInfoEntity> buildParams(FileMetadataInfoDTO searchDTO){
        // 组装查询参数
        QueryWrapper<FileMetadataInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(CharSequenceUtil.isNotBlank(searchDTO.getFileKey()),"file_key",searchDTO.getFileKey());
        queryWrapper.eq(CharSequenceUtil.isNotBlank(searchDTO.getFileMd5()),"file_md5",searchDTO.getFileMd5());
        queryWrapper.eq(CharSequenceUtil.isNotBlank(searchDTO.getBucket()),"bucket",searchDTO.getBucket());
        queryWrapper.eq(null!=searchDTO.getIsPrivate(),"is_private",searchDTO.getIsPrivate());
        queryWrapper.eq(CharSequenceUtil.isNotBlank(searchDTO.getCreateUser()),"create_user",searchDTO.getCreateUser());

        return queryWrapper;
    }
}
