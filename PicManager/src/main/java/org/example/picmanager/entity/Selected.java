package org.example.picmanager.entity;

import com.baomidou.mybatisplus.annotation.TableField;

public class Selected {
    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "image_id")
    private int imageId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
