package org.example.picmanager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.transaction.Transactional;
import org.example.picmanager.entity.Image;
import org.example.picmanager.entity.ImageTag;
import org.example.picmanager.entity.Tag;
import org.example.picmanager.mapper.ImageMapper;
import org.example.picmanager.mapper.ImageTagMapper;
import org.example.picmanager.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
public class ImageService {
    @Autowired
    private ImageMapper imageMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private ImageTagMapper imageTagMapper;

    @Transactional
    public Image uploadImage(Long userId, byte[] editedImage, List<String> tags, Map<String, Object> exifData, String name, String description, Integer size) throws Exception{
        // generation thumbnail
        byte[] thumbnail = generateThumbnail(editedImage);
        if (thumbnail.length > 65535) {
            throw new RuntimeException("缩略图太大，请优化图片或调整数据库字段");
        }

        // create image entity
        Image image=new Image();
        image.setUserid(userId);
        image.setImage(editedImage);
        image.setThumbnail(thumbnail);

        image.setName(name);
        if (description != null && !description.trim().isEmpty()) {
            image.setDescription(description.trim());
        }
        if (size != null) {
            image.setSize(size);
        } else {
            // 如果没有传入大小，使用图片字节数组的长度
            image.setSize(editedImage.length);
        }

        // exif
        setExifInfoFromData(image, exifData);

        // store image
        imageMapper.insert(image);

        // store tags
        if (tags != null && !tags.isEmpty()) {
            for (String tagName : tags) {
                tagName = tagName.trim();
                if (!tagName.isEmpty()) {
                    // 查找或创建标签
                    Tag tag = tagMapper.selectByUserIdAndName(userId, tagName);
                    if (tag == null) {
                        tag = new Tag();
                        tag.setUserId(userId);
                        tag.setTagName(tagName);
                        tagMapper.insert(tag);
                    }

                    // 创建关联
                    ImageTag imageTag = new ImageTag();
                    imageTag.setImageId(image.getId());
                    imageTag.setTagId(tag.getId());
                    imageTagMapper.insert(imageTag);
                }
            }
        }
        return image;
    }

    private byte[] generateThumbnail(byte[] imageBytes) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(imageBytes);
        BufferedImage originalBufferedImage = ImageIO.read(inputStream);

        if (originalBufferedImage == null) {
            throw new RuntimeException("无法读取图片");
        }

        // 计算缩略图尺寸
        int thumbnailWidth = 120;
        int thumbnailHeight = 120;
        double originalAspectRatio = (double) originalBufferedImage.getWidth() / originalBufferedImage.getHeight();
        if (originalBufferedImage.getWidth() > originalBufferedImage.getHeight()) {
            thumbnailHeight = (int) (thumbnailWidth / originalAspectRatio);
        } else {
            thumbnailWidth = (int) (thumbnailHeight * originalAspectRatio);
        }

        // 创建缩略图
        BufferedImage thumbnailImage = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = thumbnailImage.createGraphics();
        // 设置渲染质量
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(originalBufferedImage, 0, 0, thumbnailWidth, thumbnailHeight, null);
        graphics.dispose();

        // 转换为字节数组，降低JPEG质量
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("JPEG").next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        // 设置压缩质量（0.7-0.8之间可以显著减小文件大小）
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.8f);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(thumbnailImage, null, null), param);
        }
        writer.dispose();

        byte[] thumbnailBytes = baos.toByteArray();

        // 日志记录缩略图大小
        System.out.println("缩略图大小: " + thumbnailBytes.length + " bytes");

        return thumbnailBytes;
    }

    // 上传时解析
    private void setExifInfoFromData(Image image, Map<String, Object> exifData){
        if (exifData == null) return;
        try{
            // 拍摄时间
            if (exifData.get("shootTime") != null) {
                String dateStr = (String) exifData.get("shootTime");
                // 解析EXIF日期格式 "2023:01:15 14:30:22"
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
                image.setExifShootTime(dateTime);
            }

            if (exifData.get("location") != null) {
                image.setExifLocation((String) exifData.get("location"));
            }

            if (exifData.get("resolution") != null) {
                image.setExifResolution((String) exifData.get("resolution"));
            }

            if (exifData.get("cameraModel") != null) {
                image.setExifCameraModel((String) exifData.get("cameraModel"));
            }

            if (exifData.get("lens") != null) {
                image.setExifLens((String) exifData.get("lens"));
            }

            if (exifData.get("aperture") != null) {
                image.setExifAperture((String) exifData.get("aperture"));
            }

            if (exifData.get("shutter") != null) {
                image.setExifShutter((String) exifData.get("shutter"));
            }

            if (exifData.get("iso") != null) {
                image.setExifISO((String) exifData.get("iso"));
            }

            if (exifData.get("focalLength") != null) {
                image.setExifFocalLength((String) exifData.get("focalLength"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<Tag> getUserTags(Long userId) {
        return tagMapper.selectByUserId(userId);
    }

    public Map<String, Object> getExifInfo(Image image) {
        Map<String, Object> exifData = new HashMap<>();
        exifData.put("exifShootTime", image.getExifShootTime());
        exifData.put("exifLocation", image.getExifLocation());
        exifData.put("exifResolution", image.getExifResolution());
        exifData.put("exifCameraModel", image.getExifCameraModel());
        exifData.put("exifLens", image.getExifLens());
        exifData.put("exifAperture", image.getExifAperture());
        exifData.put("exifShutter", image.getExifShutter());
        exifData.put("exifISO", image.getExifISO());
        exifData.put("exifFocalLength", image.getExifFocalLength());
        return exifData;
    }

    // 获取图片的标签
    public List<Tag> getImageTags(int imageId) {
        try {
            List<Tag> tags = new ArrayList<>();
            QueryWrapper<ImageTag> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("image_id", imageId);
            List<ImageTag> imageTags = imageTagMapper.selectList(queryWrapper);

            for (ImageTag imageTag : imageTags) {
                Tag tag = tagMapper.selectById(imageTag.getTagId());
                if (tag != null) {
                    tags.add(tag);
                }
            }
            return tags;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
