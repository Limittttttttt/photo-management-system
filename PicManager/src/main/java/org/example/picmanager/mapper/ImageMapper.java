package org.example.picmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.picmanager.entity.Image;

@Mapper
public interface ImageMapper extends BaseMapper<Image> {
}
