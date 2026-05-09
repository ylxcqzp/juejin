package com.juejin.operation.controller;

import com.juejin.common.exception.BusinessException;
import com.juejin.common.result.ErrorCode;
import com.juejin.common.result.Result;
import com.juejin.starter.minio.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * 文件上传控制器
 *
 * @author juejin
 */
@Slf4j
@Tag(name = "文件上传", description = "图片上传相关接口")
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /** 允许的 MIME 类型 */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );

    /** 允许的文件扩展名（深度防御） */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "svg"
    );

    /** 最大文件大小：10MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Operation(summary = "上传图片", description = "上传图片文件，返回公开访问 URL。支持 jpg/png/gif/webp/svg，最大 10MB")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadImage(
            @Parameter(description = "图片文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "用户ID（由网关注入）", hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        // 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }

        // Content-Type 校验
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        // 扩展名校验（深度防御）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
            }
        }

        // 上传到对象存储
        try {
            String url = fileStorageService.uploadFile(
                    file.getInputStream(),
                    originalFilename != null ? originalFilename : "image.png",
                    contentType);
            log.info("图片上传成功, userId={}, url={}", userId, url);
            return Result.success(url);
        } catch (IOException e) {
            log.error("读取上传文件流失失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
