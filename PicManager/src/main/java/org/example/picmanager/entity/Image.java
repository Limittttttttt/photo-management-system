package org.example.picmanager.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;

public class Image {
    @TableId(type = IdType.AUTO)
    private int id;

    @TableField(value = "user_id")
    private Long userId;

    private byte[] image;
    private byte[] thumbnail;

    private String name;
    private String description;
    private int size;

    @TableField(value = "exif_shoot_time")
    private LocalDateTime exifShootTime;

    @TableField(value = "exif_location")
    private String exifLocation;

    @TableField(value = "exif_resolution")
    private String exifResolution;

    @TableField(value = "exif_camera_model")
    private String exifCameraModel;

    @TableField(value = "exif_lens")
    private String exifLens;

    @TableField(value = "exif_aperture")
    private String exifAperture;

    @TableField(value = "exif_shutter")
    private String exifShutter;

    @TableField(value = "exif_iso")
    private String exifISO;

    @TableField(value = "exif_focal_length")
    private String exifFocalLength;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getUserid() {
        return userId;
    }

    public void setUserid(Long userId) {
        this.userId = userId;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public LocalDateTime getExifShootTime() {
        return exifShootTime;
    }

    public void setExifShootTime(LocalDateTime exifShootTime) {
        this.exifShootTime = exifShootTime;
    }

    public String getExifLocation() {
        return exifLocation;
    }

    public void setExifLocation(String exifLocation) {
        this.exifLocation = exifLocation;
    }

    public String getExifResolution() {
        return exifResolution;
    }

    public void setExifResolution(String exifResolution) {
        this.exifResolution = exifResolution;
    }

    public String getExifCameraModel() {
        return exifCameraModel;
    }

    public void setExifCameraModel(String exifCameraModel) {
        this.exifCameraModel = exifCameraModel;
    }

    public String getExifLens() {
        return exifLens;
    }

    public void setExifLens(String exifLens) {
        this.exifLens = exifLens;
    }

    public String getExifAperture() {
        return exifAperture;
    }

    public void setExifAperture(String exifAperture) {
        this.exifAperture = exifAperture;
    }

    public String getExifShutter() {
        return exifShutter;
    }

    public void setExifShutter(String exifShutter) {
        this.exifShutter = exifShutter;
    }

    public String getExifISO() {
        return exifISO;
    }

    public void setExifISO(String exifISO) {
        this.exifISO = exifISO;
    }

    public String getExifFocalLength() {
        return exifFocalLength;
    }

    public void setExifFocalLength(String exifFocalLength) {
        this.exifFocalLength = exifFocalLength;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
