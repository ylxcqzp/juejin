package com.juejin.starter.minio.service;

import java.io.InputStream;

/**
 * 文件存储抽象接口 — 当前实现 MinIO，预留阿里云 OSS 接口
 *
 * @author juejin
 */
public interface FileStorageService {

    /**
     * 上传文件到对象存储
     *
     * @param inputStream      文件输入流（调用方负责关闭）
     * @param originalFilename 原始文件名（用于提取扩展名）
     * @param contentType      MIME 类型，如 "image/png"
     * @return 文件公开访问 URL
     */
    String uploadFile(InputStream inputStream, String originalFilename, String contentType);

    /**
     * 根据文件 URL 删除存储对象
     *
     * @param fileUrl uploadFile 返回的完整 URL
     */
    void deleteFile(String fileUrl);
}
