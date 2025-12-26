package org.example.picmanager.entity;

import com.baomidou.mybatisplus.annotation.TableField;

public class AiTag {
    @TableField(value = "image_id")
    private int imageId;

    @TableField(value = "tag_name")
    private String tagName;

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
