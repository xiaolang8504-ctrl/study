package com.study.module.system.file.convert;

import com.study.api.dto.response.FileData;
import com.study.module.system.file.domain.UrlFileData;
import com.study.module.system.file.entity.File;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 文件转换类
 */
@Mapper
public interface FileConvert {

    FileConvert INSTANCE = Mappers.getMapper(FileConvert.class);

    /**
     * 转化为文件数据
     */
    FileData toFileData(File file);

    /**
     * 转化为文件
     */
    File toFile(UrlFileData urlFileData);
}
