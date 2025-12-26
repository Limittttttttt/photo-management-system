package org.example.picmanager.controller;

import org.example.picmanager.entity.Image;
import org.example.picmanager.mapper.ImageMapper;
import org.example.picmanager.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/detail")
public class DetailController {
    @Autowired
    private ImageMapper imageMapper;
    @Autowired
    private ImageService imageService;

    // 获取图片详情
    @GetMapping("/{imageId}")
    public ResponseEntity<?> getImageDetail(@PathVariable int imageId){
        try {
            Image image = imageMapper.selectById(imageId);
            if (image == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "图片不存在"));
            }

            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("id", image.getId());
            response.put("name", image.getName());
            response.put("description", image.getDescription());
            response.put("size", image.getSize());
            response.put("userId", image.getUserid());

            // EXIF信息
            Map<String, Object> exifData = imageService.getExifInfo(image);
            response.putAll(exifData);

            // 标签
            response.put("tags", imageService.getImageTags(imageId));

            // 图片数据（Base64）
            String base64Image = "data:image/jpeg;base64," +
                    java.util.Base64.getEncoder().encodeToString(image.getImage());
            response.put("imageData", base64Image);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "获取图片详情失败: " + e.getMessage()));
        }
    }

    // 删除图片
    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable int imageId) {
        try {
            Image image = imageMapper.selectById(imageId);
            if (image == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "图片不存在"));
            }

            // 删除图片记录
            imageMapper.deleteById(imageId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "图片删除成功"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "error", "删除失败: " + e.getMessage()
                    ));
        }
    }
}
