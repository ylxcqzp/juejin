package com.juejin.common.result;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

    // User Service Error Codes (1000-1999)
    EMAIL_ALREADY_EXISTS(1001, "Email already exists"),
    PHONE_ALREADY_EXISTS(1002, "Phone already exists"),
    INVALID_CREDENTIALS(1003, "Invalid username or password"),
    ACCOUNT_DISABLED(1004, "Account has been disabled"),
    CANNOT_FOLLOW_SELF(1005, "Cannot follow yourself"),
    ALREADY_FOLLOWING(1006, "Already following this user"),
    USER_NOT_FOUND(1007, "User not found"),

    // Content Service Error Codes (2000-2999)
    ARTICLE_NOT_FOUND(2001, "Article not found"),
    ARTICLE_UNDER_REVIEW(2002, "Article is under review"),
    NO_EDIT_PERMISSION(2003, "No permission to edit this article"),
    TAG_NOT_FOUND(2004, "Tag not found"),

    // Social Service Error Codes (3000-3999)
    ALREADY_LIKED(3001, "Already liked"),
    NOT_LIKED(3002, "Not liked yet"),
    COMMENT_NOT_FOUND(3003, "Comment not found"),

    // Operation Service Error Codes (4000-4999)
    ALREADY_SIGNED(4001, "Already signed in today"),
    TASK_NOT_FOUND(4002, "Task not found"),
    TASK_ALREADY_COMPLETED(4003, "Task already completed"),

    // File Service Error Codes (5000-5999)
    FILE_UPLOAD_FAILED(5001, "File upload failed"),
    FILE_TOO_LARGE(5002, "File too large"),
    INVALID_FILE_TYPE(5003, "Invalid file type");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
