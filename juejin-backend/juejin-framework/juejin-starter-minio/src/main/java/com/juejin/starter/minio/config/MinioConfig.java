package com.juejin.starter.minio.config;

import com.juejin.starter.minio.service.FileStorageService;
import com.juejin.starter.minio.service.MinioFileStorageService;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置类 — 创建 MinioClient 和 FileStorageService Bean
 *
 * @author juejin
 */
@Configuration
@ConditionalOnClass(MinioClient.class)
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    @ConditionalOnProperty(prefix = "juejin.minio", name = "endpoint")
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .region(properties.getRegion())
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "juejin.minio", name = "endpoint")
    public FileStorageService fileStorageService(MinioClient minioClient, MinioProperties properties) {
        return new MinioFileStorageService(minioClient, properties);
    }
}
