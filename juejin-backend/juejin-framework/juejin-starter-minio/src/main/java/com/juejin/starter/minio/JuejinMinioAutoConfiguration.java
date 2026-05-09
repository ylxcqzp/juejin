package com.juejin.starter.minio;

import com.juejin.starter.minio.config.MinioConfig;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * MinIO 自动配置聚合器
 *
 * @author juejin
 */
@Configuration
@ConditionalOnClass(MinioClient.class)
@Import(MinioConfig.class)
public class JuejinMinioAutoConfiguration {
}
