package org.example.picmanager.service;

import org.example.picmanager.entity.Tag;
import org.example.picmanager.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {

    @Autowired
    private TagMapper tagMapper;

    // 获取用户的所有标签
    public List<Tag> getUserTags(Long userId) {
        return tagMapper.selectByUserId(userId);
    }

    // 删除标签
    @Transactional
    public boolean deleteTag(Long tagId, Long userId) {
        // 检查标签是否存在且属于该用户
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null || !tag.getUserId().equals(userId)) {
            return false;
        }

        // 删除标签本身
        int result = tagMapper.deleteById(tagId);
        return result > 0;
    }
}