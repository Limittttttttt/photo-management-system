package org.example.picmanager.controller;

import org.example.picmanager.entity.Image;
import org.example.picmanager.service.SelectedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/setting")
@CrossOrigin
public class SetController {
    @Autowired
    private SelectedService selectedService;

    // 创建用户与图片的关联
    @PostMapping("/selected/{userId}/{imageId}")
    public Map<String, Object> createRelation(@PathVariable Long userId,
                                              @PathVariable int imageId) {
        Map<String, Object> result = new HashMap<>();

        boolean isSuccess = selectedService.createRelation(userId, imageId);

        result.put("success", isSuccess);
        result.put("message", isSuccess ? "关联创建成功" : "关联创建失败（可能已存在关联）");
        return result;
    }

    // 删除关联
    @DeleteMapping("/remove/{userId}/{imageId}")
    public Map<String, Object> removeRelation(@PathVariable Long userId,
                                              @PathVariable int imageId) {
        Map<String, Object> result = new HashMap<>();

        boolean isSuccess = selectedService.removeRelation(userId, imageId);

        result.put("success", isSuccess);
        result.put("message", isSuccess ? "关联删除成功" : "关联删除失败");
        return result;
    }

    // 查询关联的图片id
    @GetMapping("/images/{userId}")
    public Map<String, Object> getImagesByUser(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户ID不能为空");
            return result;
        }

        java.util.List<Integer> imageIds = selectedService.getImageIdsByUserId(userId);

        result.put("success", true);
        result.put("data", imageIds);
        result.put("count", imageIds.size());
        return result;
    }

    // 检查用户和图片是否关联
    @GetMapping("/check/{userId}/{imageId}")
    public Map<String, Object> checkRelation(@PathVariable Long userId,
                                             @PathVariable int imageId) {
        Map<String, Object> result = new HashMap<>();

        boolean exists = selectedService.existsRelation(userId, imageId);

        result.put("success", true);
        result.put("exists", exists);
        result.put("message", exists ? "已存在关联" : "不存在关联");
        return result;
    }

    // 精选图片信息
    @GetMapping("/{imageId}/basic")
    public Map<String, Object> getImageBasicInfo(@PathVariable int imageId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Image image = selectedService.getImageBasicInfo(imageId);

            if (image != null) {
                result.put("success", true);
                result.put("data", image);
            } else {
                result.put("success", false);
                result.put("message", "图片不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取图片信息失败");
            e.printStackTrace();
        }

        return result;
    }
}
