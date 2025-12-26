package org.example.picmanager.controller;

import org.example.picmanager.entity.AiTag;
import org.example.picmanager.service.AiTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/aitags")
@CrossOrigin
public class AiTagController {

    @Autowired
    private AiTagService aiTagService;

    // 获取图片的AI标签
    @GetMapping("/{imageId}")
    public ResponseEntity<?> getAiTags(@PathVariable int imageId) {
        try {
            List<AiTag> tags = aiTagService.getAiTagsByImageId(imageId);
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取AI标签失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 生成AI标签
    @PostMapping("/generate/{imageId}")
    public ResponseEntity<?> generateAiTags(@PathVariable int imageId) {
        try {
            System.out.println("开始生成AI标签，图片ID: " + imageId);

            Map<String, Object> result = aiTagService.generateAiTags(imageId);
            System.out.println("生成结果: " + result);

            boolean success = (Boolean) result.get("success");

            if (success) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            System.err.println("生成AI标签异常: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "系统异常: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 删除AI标签
    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteAiTag(@PathVariable int imageId,
                                         @RequestBody Map<String, String> request) {
        try {
            System.out.println("删除AI标签，图片ID: " + imageId + ", 请求: " + request);

            String tagName = request.get("tagName");
            if (tagName == null || tagName.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "标签名称不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            Map<String, Object> result = aiTagService.deleteAiTag(imageId, tagName);
            System.out.println("删除结果: " + result);

            boolean success = (Boolean) result.get("success");

            if (success) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            System.err.println("删除AI标签异常: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "系统异常: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}