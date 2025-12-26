package org.example.picmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.picmanager.entity.Tag;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    @Select("SELECT * FROM tag WHERE user_id = #{userId}")
    List<Tag> selectByUserId(Long userId);

    @Select("SELECT * FROM tag WHERE user_id = #{userId} AND tag_name = #{tagName}")
    Tag selectByUserIdAndName(Long userId, String tagName);
}
