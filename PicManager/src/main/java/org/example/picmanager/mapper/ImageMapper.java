package org.example.picmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.picmanager.entity.Image;

import java.util.List;

@Mapper
public interface ImageMapper extends BaseMapper<Image> {
}
