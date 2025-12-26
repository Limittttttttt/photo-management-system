package org.example.picmanager.controller;

import org.example.picmanager.service.AiSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/aisearch")
@CrossOrigin
public class AiSearchController {

    @Autowired
    private AiSearchService aiSearchService;

    // AI搜索
    @GetMapping("/search")
    public Map<String, Object> quickSearch(
            @RequestParam Long userId,
            @RequestParam String query) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 返回包含Base64图片数据
            List<Map<String, Object>> images = aiSearchService.searchImagesWithData(userId, query);

            response.put("success", true);
            response.put("message", "搜索成功");
            response.put("images", images);
            response.put("total", images.size());
            response.put("query", query);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            response.put("images", List.of());
            response.put("total", 0);
        }

        return response;
    }
}