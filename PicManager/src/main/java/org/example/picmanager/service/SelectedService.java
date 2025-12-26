package org.example.picmanager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.picmanager.entity.Image;
import org.example.picmanager.entity.Selected;
import org.example.picmanager.mapper.ImageMapper;
import org.example.picmanager.mapper.SelectedMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SelectedService {

    @Autowired
    private SelectedMapper selectedMapper;

    @Autowired
    private ImageMapper imageMapper;

    // 创建用户与图片的关联
    public boolean createRelation(Long userId, int imageId) {
        // 检查是否已存在
        QueryWrapper<Selected> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("image_id", imageId);
        Long count = selectedMapper.selectCount(queryWrapper);

        if (count > 0) {
            return false; // 已存在关联
        }

        Selected selected = new Selected();
        selected.setUserId(userId);
        selected.setImageId(imageId);

        int result = selectedMapper.insert(selected);
        return result > 0;
    }

    // 检查用户和图片是否已存在关联
    public boolean existsRelation(Long userId, int imageId) {
        QueryWrapper<Selected> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("image_id", imageId);
        Long count = selectedMapper.selectCount(queryWrapper);
        return count > 0;
    }

    // 删除用户和图片的关联
    public boolean removeRelation(Long userId, int imageId) {
        QueryWrapper<Selected> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("image_id", imageId);
        int result = selectedMapper.delete(queryWrapper);
        return result > 0;
    }

    // 根据用户ID查询其关联的所有图片ID
    public List<Integer> getImageIdsByUserId(Long userId) {
        QueryWrapper<Selected> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .select("image_id"); // 只查询需要的字段

        List<Selected> selectedList = selectedMapper.selectList(queryWrapper);

        return selectedList.stream()
                .map(Selected::getImageId)
                .collect(Collectors.toList());
    }

    // 获取图片和标题和描述
    public Image getImageBasicInfo(int id) {
        LambdaQueryWrapper<Image> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .select(
                        Image::getName,
                        Image::getDescription,
                        Image::getImage
                )
                .eq(Image::getId, id);

        return imageMapper.selectOne(queryWrapper);
    }
}