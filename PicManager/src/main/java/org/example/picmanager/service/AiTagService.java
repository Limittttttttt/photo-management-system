package org.example.picmanager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baidu.aip.imageclassify.AipImageClassify;
import org.example.picmanager.entity.AiTag;
import org.example.picmanager.entity.Image;
import org.example.picmanager.mapper.AiTagMapper;
import org.example.picmanager.mapper.ImageMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiTagService {
    @Autowired
    private AiTagMapper aiTagMapper;

    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    private AipImageClassify aipImageClassify;

    @Value("${baidu.ai.baike-num:5}")
    private int baikeNum;

    // 获取图片AI标签
    public List<AiTag> getAiTagsByImageId(int imageId) {
        QueryWrapper<AiTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("image_id", imageId);
        return aiTagMapper.selectList(queryWrapper);
    }

    // 为图片生成AI标签
    public Map<String, Object> generateAiTags(int imageId) {
        Map<String, Object> result = new HashMap<>();

        System.out.println("开始生成AI标签，图片ID: " + imageId);

        try {
            // 获取图片信息
            Image image = imageMapper.selectById(imageId);
            if (image == null) {
                System.err.println("图片不存在，ID: " + imageId);
                result.put("success", false);
                result.put("message", "图片不存在");
                return result;
            }

            System.out.println("获取到图片: " + image.getName() + ", 用户ID: " + image.getUserid());

            // 检查图片数据
            byte[] imageData = image.getImage();
            if (imageData == null || imageData.length == 0) {
                System.err.println("图片数据为空，图片ID: " + imageId);
                result.put("success", false);
                result.put("message", "图片数据为空");
                return result;
            }

            System.out.println("图片数据大小: " + imageData.length + " bytes");

            // 调用百度AI接口进行高级通用识别
            System.out.println("开始调用百度AI接口...");
            HashMap<String, String> options = new HashMap<>();
            options.put("baike_num", String.valueOf(baikeNum));

            JSONObject response = aipImageClassify.advancedGeneral(imageData, options);
            System.out.println("百度AI响应: " + response.toString());

            // 检查百度AI响应
            if (response.has("error_code")) {
                int errorCode = response.getInt("error_code");
                String errorMsg = response.getString("error_msg");
                System.err.println("百度AI识别失败，错误码: " + errorCode + ", 错误信息: " + errorMsg);

                result.put("success", false);
                result.put("message", "百度AI识别失败: " + errorMsg);
                result.put("errorCode", errorCode);
                return result;
            }

            // 解析响应结果
            List<AiTag> generatedTags = parseAiTagsFromResponse(imageId, response);
            System.out.println("解析到 " + generatedTags.size() + " 个AI标签");

            // 保存AI标签
            int savedCount = 0;
            List<String> savedTagNames = new ArrayList<>();

            for (AiTag aiTag : generatedTags) {
                String tagName = aiTag.getTagName();
                if (tagName != null && !tagName.trim().isEmpty()) {
                    // 检查标签是否已存在
                    QueryWrapper<AiTag> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("image_id", imageId)
                            .eq("tag_name", tagName);

                    if (aiTagMapper.selectCount(queryWrapper) == 0) {
                        // 创建新的 AiTag 对象并保存
                        AiTag newTag = new AiTag();
                        newTag.setImageId(imageId);
                        newTag.setTagName(tagName);
                        aiTagMapper.insert(newTag);
                        savedCount++;
                        savedTagNames.add(tagName);
                    }
                }
            }

            System.out.println("成功保存 " + savedCount + " 个AI标签: " + savedTagNames);

            result.put("success", true);
            result.put("message", String.format("成功生成并保存 %d 个AI标签", savedCount));
            result.put("count", savedCount);
            result.put("tags", generatedTags);

        } catch (Exception e) {
            System.err.println("生成AI标签异常，图片ID: " + imageId);
            e.printStackTrace();

            result.put("success", false);
            result.put("message", "生成AI标签失败: " + e.getMessage());
        }

        return result;
    }

    // 从百度AI响应中解析AI标签
    private List<AiTag> parseAiTagsFromResponse(int imageId, JSONObject response) {
        List<AiTag> aiTags = new ArrayList<>();

        try {
            if (response.has("result")) {
                Object resultObj = response.get("result");

                if (resultObj instanceof JSONArray resultArray) {

                    for (int i = 0; i < resultArray.length(); i++) {
                        JSONObject item = resultArray.getJSONObject(i);

                        AiTag aiTag = new AiTag();
                        aiTag.setImageId(imageId);

                        if (item.has("keyword")) {
                            String keyword = item.getString("keyword");
                            aiTag.setTagName(keyword);

                            System.out.println("解析到标签: " + keyword + ", 置信度: " +
                                    (item.has("score") ? item.getDouble("score") : "无"));

                            aiTags.add(aiTag);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析AI标签响应异常");
            e.printStackTrace();
        }

        // 如果没有识别到标签，返回一些默认标签
        if (aiTags.isEmpty()) {
            System.out.println("未识别到任何标签，使用默认标签");

            AiTag defaultTag1 = new AiTag();
            defaultTag1.setImageId(imageId);
            defaultTag1.setTagName("图像");
            aiTags.add(defaultTag1);

            AiTag defaultTag2 = new AiTag();
            defaultTag2.setImageId(imageId);
            defaultTag2.setTagName("照片");
            aiTags.add(defaultTag2);
        }

        return aiTags;
    }

    // 删除AI标签
    public Map<String, Object> deleteAiTag(int imageId, String tagName) {
        Map<String, Object> result = new HashMap<>();

        try {
            QueryWrapper<AiTag> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("image_id", imageId)
                    .eq("tag_name", tagName);

            int deleted = aiTagMapper.delete(queryWrapper);

            if (deleted > 0) {
                result.put("success", true);
                result.put("message", "删除AI标签成功");
            } else {
                result.put("success", false);
                result.put("message", "AI标签不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除AI标签失败: " + e.getMessage());
        }

        return result;
    }
}