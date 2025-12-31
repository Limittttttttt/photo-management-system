-- 创建数据库
CREATE DATABASE IF NOT EXISTS mydb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE mydb;

-- 清理现有数据（确保干净状态）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE selected;
TRUNCATE TABLE ai_tag;
TRUNCATE TABLE image_tag;
TRUNCATE TABLE tag;
TRUNCATE TABLE image;
TRUNCATE TABLE user;
SET FOREIGN_KEY_CHECKS = 1;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建图片表
CREATE TABLE IF NOT EXISTS `image` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `image` LONGBLOB NOT NULL,
    `thumbnail` LONGBLOB,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT,
    `size` INT NOT NULL,
    `exif_shoot_time` DATETIME,
    `exif_location` VARCHAR(255),
    `exif_resolution` VARCHAR(100),
    `exif_camera_model` VARCHAR(255),
    `exif_lens` VARCHAR(255),
    `exif_aperture` VARCHAR(50),
    `exif_shutter` VARCHAR(50),
    `exif_iso` VARCHAR(50),
    `exif_focal_length` VARCHAR(50),
    INDEX `idx_user_id` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建标签表
CREATE TABLE IF NOT EXISTS `tag` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `tag_name` VARCHAR(255) NOT NULL,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_tag_name` (`tag_name`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_tag` (`user_id`, `tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建图片标签关联表
CREATE TABLE IF NOT EXISTS `image_tag` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `image_id` INT NOT NULL,
    `tag_id` INT NOT NULL,
    INDEX `idx_image_id` (`image_id`),
    INDEX `idx_tag_id` (`tag_id`),
    FOREIGN KEY (`image_id`) REFERENCES `image`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_image_tag` (`image_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建AI标签表
CREATE TABLE IF NOT EXISTS `ai_tag` (
    `image_id` INT NOT NULL,
    `tag_name` VARCHAR(255) NOT NULL,
    INDEX `idx_image_id` (`image_id`),
    INDEX `idx_tag_name` (`tag_name`),
    FOREIGN KEY (`image_id`) REFERENCES `image`(`id`) ON DELETE CASCADE,
    PRIMARY KEY (`image_id`, `tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建选中表
CREATE TABLE IF NOT EXISTS `selected` (
    `user_id` BIGINT NOT NULL,
    `image_id` INT NOT NULL,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_image_id` (`image_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`image_id`) REFERENCES `image`(`id`) ON DELETE CASCADE,
    PRIMARY KEY (`user_id`, `image_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

