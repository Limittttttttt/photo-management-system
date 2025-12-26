package org.example.picmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.example.picmanager.entity.AiTag;

import java.util.List;

public interface AiTagMapper extends BaseMapper<AiTag> {

    // 根据图片ID查询所有AI标签
    List<AiTag> findByImageId(@Param("imageId") int imageId);

}