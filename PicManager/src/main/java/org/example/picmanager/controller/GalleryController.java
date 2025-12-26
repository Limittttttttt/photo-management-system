package org.example.picmanager.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.picmanager.entity.Image;
import org.example.picmanager.entity.Tag;
import org.example.picmanager.mapper.ImageMapper;
import org.example.picmanager.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gallery")
@CrossOrigin
public class GalleryController {
    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageMapper imageMapper;

    // 获取用户的所有图片
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserImages(@PathVariable Long userId) {
        try {
            QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.orderByDesc("id");
            List<Image> images = imageMapper.selectList(queryWrapper);

            List<Map<String, Object>> result = new ArrayList<>();
            for (Image image : images) {
                Map<String, Object> imageData = new HashMap<>();
                imageData.put("id", image.getId());
                imageData.put("name", image.getName());
                imageData.put("description", image.getDescription());
                imageData.put("size", image.getSize());

                // 原图
                if (image.getImage() != null) {
                    java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
                    String base64Image = encoder.encodeToString(image.getImage());
                    imageData.put("image", base64Image);
                }

                // 缩略图（Base64格式）
                if (image.getThumbnail() != null) {
                    java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
                    String base64Thumbnail = encoder.encodeToString(image.getThumbnail());
                    imageData.put("thumbnail", base64Thumbnail);
                }

                // EXIF 数据
                Map<String, Object> exifInfo = imageService.getExifInfo(image);
                imageData.putAll(exifInfo);

                // 获取并添加标签信息
                List<Tag> tags = imageService.getImageTags(image.getId());
                List<Map<String, Object>> tagList = new ArrayList<>();
                for (Tag tag : tags) {
                    Map<String, Object> tagMap = new HashMap<>();
                    tagMap.put("id", tag.getId());
                    tagMap.put("tagName", tag.getTagName());
                    tagList.add(tagMap);
                }
                imageData.put("tags", tagList);

                result.add(imageData);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("获取图片列表失败");
        }
    }
}