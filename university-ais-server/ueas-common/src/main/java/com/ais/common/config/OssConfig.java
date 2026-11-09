package com.ais.common.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置。
 *
 * <p>通过 {@code ueas.oss.*} 前缀读取配置：
 * <ul>
 *   <li>endpoint — OSS接入点，如 oss-cn-beijing.aliyuncs.com</li>
 *   <li>access-key-id — 访问密钥ID</li>
 *   <li>access-key-secret — 访问密钥Secret</li>
 *   <li>bucket-name — Bucket名称</li>
 *   <li>url-prefix — 文件访问URL前缀（可选，默认拼endpoint生成）</li>
 * </ul>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ueas.oss")
public class OssConfig {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    /** 文件存储路径前缀，如 avatars/ */
    private String prefix = "avatars/";
    /** 单个文件最大大小（字节），默认2MB */
    private long maxSize = 2 * 1024 * 1024;
    /** 允许的图片MIME类型 */
    private String[] allowedTypes = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 生成文件访问URL。
     * 如果配置了 url-prefix 则直接使用，否则根据 endpoint 和 bucketName 拼装。
     */
    public String buildFileUrl(String key) {
        return "https://" + bucketName + "." + endpoint + "/" + prefix + key;
    }
}
