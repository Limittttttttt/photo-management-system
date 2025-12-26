package org.example.picmanager.controller;

import org.example.picmanager.entity.Tag;
import org.example.picmanager.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tags")
@CrossOrigin
public class TagController {

    @Autowired
    private TagService tagService;

    // 获取用户的标签
    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserTags(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Tag> tags = tagService.getUserTags(userId);
            result.put("success", true);
            result.put("data", tags);
            result.put("message", "获取标签成功");
            result.put("total", tags.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取标签失败: " + e.getMessage());
        }
        return result;
    }

    // 删除标签
    @PostMapping("/delete")
    public Map<String, Object> deleteTag(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long tagId = Long.valueOf(params.get("tagId").toString());
            Long userId = Long.valueOf(params.get("userId").toString());

            boolean success = tagService.deleteTag(tagId, userId);
            if (success) {
                result.put("success", true);
                result.put("message", "删除标签成功");
            } else {
                result.put("success", false);
                result.put("message", "删除标签失败，标签可能不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除标签失败: " + e.getMessage());
        }
        return result;
    }
}