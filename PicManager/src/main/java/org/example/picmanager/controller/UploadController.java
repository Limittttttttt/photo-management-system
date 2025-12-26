package org.example.picmanager.controller;

import org.example.picmanager.entity.Image;
import org.example.picmanager.entity.Tag;
import org.example.picmanager.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
public class UploadController {

    @Autowired
    private ImageService imageService;

    @PostMapping("/upload/{userId}")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> requestData){
        try{
            // 获取Base64图片数据
            String editedImageBase64 = (String) requestData.get("editedImage");
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) requestData.get("tags");
            @SuppressWarnings("unchecked")
            Map<String, Object> exifData = (Map<String, Object>) requestData.get("exifData");

            String name = (String) requestData.get("name");
            String description = (String) requestData.get("description");
            Integer size = (Integer) requestData.get("size");

            if (editedImageBase64 == null || editedImageBase64.isEmpty()) {
                return ResponseEntity.badRequest().body("图片数据不能为空");
            }

            // 解析Base64图片数据
            String base64Data = editedImageBase64;
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1];
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // 验证图片数据
            if (imageBytes.length > 10 * 1024 * 1024) { // 10MB限制
                return ResponseEntity.badRequest().body("图片大小不能超过10MB");
            }

            // 处理图片上传，传递EXIF数据
            Image image = imageService.uploadImage(userId, imageBytes, tags, exifData, name, description, size);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "上传成功");
            response.put("imageId", image.getId());

            return ResponseEntity.ok(response);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("上传失败："+e.getMessage());
        }
    }

    @GetMapping("/tags/{userId}")
    public ResponseEntity<?> getUserTags(@PathVariable Long userId){
        try {
            List<Tag> tags = imageService.getUserTags(userId);
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("获取标签失败");
        }
    }
}
