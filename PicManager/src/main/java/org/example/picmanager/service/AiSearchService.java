// 文件名：AISearchService.java
package org.example.picmanager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baidu.aip.imageclassify.AipImageClassify;
import org.example.picmanager.entity.Image;
import org.example.picmanager.mapper.ImageMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AiSearchService {

    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    private AipImageClassify aipImageClassify;

    // AI搜索图片
    public List<Map<String, Object>> searchImagesWithData(Long userId, String query) {
        if (query == null || query.trim().isEmpty()) {
            return getRecentImagesWithData(userId);
        }

        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Image::getUserid, userId)
                .select(Image.class,
                        i -> !i.getColumn().equals("thumbnail")) // 不查询缩略图
                .isNotNull(Image::getImage);
        List<Image> allImages = imageMapper.selectList(wrapper);

        if (allImages.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> matchedImages = new ArrayList<>();
        String queryLower = query.toLowerCase();

        for (Image image : allImages) {
            try {
                // 快速文本匹配
                if (textMatch(image, queryLower)) {
                    matchedImages.add(convertImageToMap(image));
                    continue;
                }

                // AI识别
                if (aiMatch(image, queryLower)) {
                    matchedImages.add(convertImageToMap(image));
                }
            } catch (Exception e) {
                // AI识别失败时，使用文本匹配
                if (textMatch(image, queryLower)) {
                    matchedImages.add(convertImageToMap(image));
                }
            }
        }

        return matchedImages;
    }

    // 将Image对象转换为包含Base64图片数据的Map
    private Map<String, Object> convertImageToMap(Image image) {
        Map<String, Object> imageData = new HashMap<>();

        // 基本属性
        imageData.put("id", image.getId());
        imageData.put("name", image.getName());
        imageData.put("description", image.getDescription());
        imageData.put("size", image.getSize());

        // EXIF 信息
        imageData.put("exifShootTime", image.getExifShootTime());
        imageData.put("exifLocation", image.getExifLocation());
        imageData.put("exifCameraModel", image.getExifCameraModel());
        imageData.put("exifResolution", image.getExifResolution());
        imageData.put("exifLens", image.getExifLens());
        imageData.put("exifAperture", image.getExifAperture());
        imageData.put("exifShutter", image.getExifShutter());
        imageData.put("exifISO", image.getExifISO());
        imageData.put("exifFocalLength", image.getExifFocalLength());

        // 原图 Base64
        if (image.getImage() != null && image.getImage().length > 0) {
            try {
                String base64Image = java.util.Base64.getEncoder().encodeToString(image.getImage());
                imageData.put("image", base64Image);
            } catch (Exception e) {
                imageData.put("image", "");
            }
        } else {
            imageData.put("image", "");
        }

        return imageData;
    }

    // 返回包含Base64图片数据
    private List<Map<String, Object>> getRecentImagesWithData(Long userId) {
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Image::getUserid, userId)
                .select(Image.class,
                        i -> !i.getColumn().equals("thumbnail"))
                .isNotNull(Image::getImage)
                .orderByDesc(Image::getId)
                .last("LIMIT 12");

        List<Image> images = imageMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Image image : images) {
            result.add(convertImageToMap(image));
        }

        return result;
    }

    // 文本匹配
    private boolean textMatch(Image image, String query) {
        // 检查名称
        if (image.getName() != null && image.getName().toLowerCase().contains(query)) {
            return true;
        }

        // 检查描述
        if (image.getDescription() != null && image.getDescription().toLowerCase().contains(query)) {
            return true;
        }

        // 检查EXIF信息
        if (image.getExifCameraModel() != null && image.getExifCameraModel().toLowerCase().contains(query)) {
            return true;
        }

        if (image.getExifLocation() != null && image.getExifLocation().toLowerCase().contains(query)) {
            return true;
        }

        return false;
    }

    // AI匹配
    private boolean aiMatch(Image image, String query) throws Exception {
        byte[] imageData = image.getImage();
        if (imageData == null || imageData.length == 0) {
            return false;
        }

        try {
            // 调用百度AI通用物体识别
            JSONObject response = aipImageClassify.advancedGeneral(imageData, new HashMap<>());

            if (response.has("result")) {
                return response.getJSONArray("result").toList().stream()
                        .anyMatch(obj -> {
                            Map<String, Object> result = (Map<String, Object>) obj;
                            String keyword = result.get("keyword").toString().toLowerCase();
                            Double score = Double.parseDouble(result.get("score").toString());

                            // 置信度高于0.3且关键词匹配
                            return score > 0.3 &&
                                    (keyword.contains(query) || query.contains(keyword) ||
                                            keyword.split("、|,|，").length > 0 &&
                                                    Arrays.stream(keyword.split("、|,|，"))
                                                            .anyMatch(kw -> kw.contains(query) || query.contains(kw)));
                        });
            }
        } catch (Exception e) {
            throw new Exception("AI识别失败: " + e.getMessage());
        }

        return false;
    }
}