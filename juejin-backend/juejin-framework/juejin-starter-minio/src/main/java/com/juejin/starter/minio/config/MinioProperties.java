package com.juejin.starter.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO 配置属性
 *
 * @author juejin
 */
@Data
@ConfigurationProperties(prefix = "juejin.minio")
public class MinioProperties {

    /** MinIO 服务地址，如 http://localhost:9000 */
    private String endpoint = "http://localhost:9000";

    /** 访问密钥 */
    private String accessKey = "minioadmin";

    /** 秘密密钥 */
    private String secretKey = "minioadmin";

    /** 存储桶名称 */
    private String bucket = "juejin";

    /** 区域（默认 us-east-1） */
    private String region = "us-east-1";
}
