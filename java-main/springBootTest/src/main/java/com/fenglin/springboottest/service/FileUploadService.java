package com.fenglin.springboottest.service;

import com.fenglin.springboottest.exception.BusinessException;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.util.StringUtils;

/**
 * 文件上传服务：负责头像/图片的校验、裁切、压缩与落盘。
 * 流程：校验格式/大小 → 居中裁剪为正方形 → 缩放至配置边长 → 压缩为 JPEG。
 * 文件保存在本地磁盘（file.upload.dir），通过 UploadConfig 暴露为 /uploads/** 静态资源。
 */
@Service
public class FileUploadService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.upload.url-prefix:/uploads/}")
    private String urlPrefix;

    /** 输出正方形头像边长（像素） */
    @Value("${file.upload.avatar-size:256}")
    private int avatarSize;

    /** JPEG 压缩质量 0.0~1.0 */
    @Value("${file.upload.image-quality:0.85}")
    private double imageQuality;

    /** 允许的最大图片大小（与 application 中 multipart 上限保持一致） */
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    /** 允许的图片 MIME 类型（ImageIO 默认可解码的格式） */
    private static final String[] ALLOWED_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/bmp"
    };

    /**
     * 保存上传的图片（自动裁剪/压缩），返回可访问的 URL（形如 /uploads/avatar_1_a1b2c3.jpg）。
     *
     * @param file       上传的文件
     * @param namePrefix 文件名前缀（如 avatar_1），用于区分业务与用户
     * @return 图片访问地址
     */
    public String uploadImage(MultipartFile file, String namePrefix) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("未检测到文件，请先选择图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !isAllowed(contentType)) {
            throw new BusinessException("仅支持 JPG / PNG / GIF / BMP 等图片格式");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("图片过大，请控制在 10MB 以内");
        }

        // 目标目录：解析为绝对路径，确保与静态资源映射一致
        Path dir = Paths.get(uploadDir).toAbsolutePath();
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new BusinessException("上传目录创建失败：" + e.getMessage());
        }

        // 输出统一为 JPEG，文件名固定 .jpg
        String filename = namePrefix + "_" + UUID.randomUUID().toString().replace("-", "") + ".jpg";
        Path target = dir.resolve(filename);

        try {
            // 1) 读取原图；2) 居中裁剪为正方形；3) 缩放并压缩为 JPEG
            BufferedImage src = ImageIO.read(file.getInputStream());
            if (src == null) {
                throw new BusinessException("无法解析该图片，请换一张再试");
            }
            int w = src.getWidth();
            int h = src.getHeight();
            int side = Math.min(w, h);
            BufferedImage square = src.getSubimage((w - side) / 2, (h - side) / 2, side, side);

            Thumbnails.of(square)
                    .size(avatarSize, avatarSize)
                    .outputFormat("jpg")
                    .outputQuality(imageQuality)
                    .toFile(target.toFile());
        } catch (IOException e) {
            throw new BusinessException("图片处理失败：" + e.getMessage());
        }

        // URL 前缀保证以 / 结尾
        String prefix = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
        return prefix + filename;
    }

    private boolean isAllowed(String contentType) {
        for (String t : ALLOWED_TYPES) {
            if (t.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按访问 URL 删除已管理的图片（用于更换头像时清理旧文件）。
     * 仅当 URL 来自本服务的 url-prefix 且文件名安全时才执行删除，避免误删 / 路径穿越。
     *
     * @param url 头像访问地址（形如 /uploads/avatar_1_xxx.jpg）
     */
    public void deleteImageByUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return;
        }
        String prefix = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
        if (!url.startsWith(prefix)) {
            return;
        }
        String filename = url.substring(prefix.length());
        // 防路径穿越：文件名不得为空或包含目录分隔符 / ..
        if (filename.isEmpty() || filename.contains("/")
                || filename.contains("\\") || filename.contains("..")) {
            return;
        }
        Path target = Paths.get(uploadDir).toAbsolutePath().resolve(filename);
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // 清理失败不影响主流程（如文件已被手动移除）
        }
    }
}
