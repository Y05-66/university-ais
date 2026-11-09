package com.ais.system.controller;

import com.ais.common.config.OssConfig;
import com.ais.common.result.R;
import com.aliyun.oss.OSS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器。
 *
 * <p>处理头像等文件的上传，上传到阿里云OSS并返回公网访问URL。
 * 同时提供本地缓存文件的读取接口。
 *
 * <h3>接口说明</h3>
 * <ul>
 *   <li>POST /upload/avatar — 上传头像，返回OSS访问URL</li>
 *   <li>GET /upload/avatar/{filename} — 获取本地缓存的头像文件</li>
 * </ul>
 */
@Tag(name = "文件上传")
@RestController
@RequestMapping("/upload")
public class UploadController {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    @Value("${ueas.avatar.dir:C:/uploads/avatars/}")
    private String avatarDir;

    public UploadController(OSS ossClient, OssConfig ossConfig) {
        this.ossClient = ossClient;
        this.ossConfig = ossConfig;
        // 确保本地缓存目录存在
        try {
            String dir = avatarDir != null ? avatarDir : "C:/uploads/avatars/";
            Path path = Paths.get(dir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (Exception e) {
            // 静默处理
        }
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.fail("文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !List.of(ossConfig.getAllowedTypes()).contains(contentType)) {
            return R.fail("只支持 JPG、PNG、GIF、WEBP 格式的图片");
        }
        if (file.getSize() > ossConfig.getMaxSize()) {
            return R.fail("文件大小不能超过 2MB");
        }

        try {
            String ext = getExtension(contentType);
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
            String objectKey = ossConfig.getPrefix() + fileName;

            // 上传到 OSS
            ossClient.putObject(ossConfig.getBucketName(), objectKey, file.getInputStream());

            // 保存到本地缓存
            Path localPath = Paths.get(avatarDir, fileName);
            Files.copy(file.getInputStream(), localPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String url = ossConfig.buildFileUrl(fileName);
            return R.ok(url);
        } catch (IOException e) {
            return R.fail("上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取本地头像文件")
    @GetMapping("/avatar/{filename:.+}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(avatarDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = getContentType(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String getContentType(String filename) {
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "jpeg", "jpg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
