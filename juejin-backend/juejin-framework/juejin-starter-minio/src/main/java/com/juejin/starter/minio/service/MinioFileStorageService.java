package com.juejin.starter.minio.service;

import com.juejin.starter.minio.config.MinioProperties;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * MinIO 文件存储实现
 *
 * @author juejin
 */
@Slf4j
@RequiredArgsConstructor
public class MinioFileStorageService implements FileStorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Override
    public String uploadFile(InputStream inputStream, String originalFilename, String contentType) {
        String ext = extractExtension(originalFilename);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String objectName = String.format("uploads/%s/%s.%s", datePath, UUID.randomUUID().toString(), ext);

        try {
            // 确保 Bucket 存在
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.getBucket()).build());
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(properties.getBucket()).build());
                log.info("MinIO Bucket 已创建: {}", properties.getBucket());
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .stream(inputStream, -1, 10 * 1024 * 1024)
                            .contentType(contentType)
                            .build());

            String url = String.format("%s/%s/%s",
                    properties.getEndpoint().replaceAll("/$", ""),
                    properties.getBucket(),
                    objectName);
            log.info("文件上传成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("MinIO 上传失败: {}", originalFilename, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        String prefix = String.format("%s/%s/",
                properties.getEndpoint().replaceAll("/$", ""),
                properties.getBucket());
        if (!fileUrl.startsWith(prefix)) {
            log.warn("文件 URL 与 Bucket 不匹配，无法删除: {}", fileUrl);
            return;
        }
        String objectName = fileUrl.substring(prefix.length());
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .build());
            log.info("MinIO 对象已删除: {}", objectName);
        } catch (Exception e) {
            log.error("MinIO 删除失败: {}", fileUrl, e);
        }
    }

    /**
     * 从文件名提取扩展名，无扩展名时默认 png
     */
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "png";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
