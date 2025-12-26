package org.example.picmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.picmanager.entity.ImageTag;

import java.util.List;

@Mapper
public interface ImageTagMapper extends BaseMapper<ImageTag> {
}
