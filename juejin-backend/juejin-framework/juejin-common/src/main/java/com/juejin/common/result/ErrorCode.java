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
    INTERNAL_SERVER_ERROR(500, "服务繁忙，请稍后重试"),

    // User Service Error Codes (1000-1999)
    EMAIL_ALREADY_EXISTS(1001, "Email already exists"),
    PHONE_ALREADY_EXISTS(1002, "Phone already exists"),
    INVALID_CREDENTIALS(1003, "Invalid username or password"),
    ACCOUNT_DISABLED(1004, "Account has been disabled"),
    CANNOT_FOLLOW_SELF(1005, "Cannot follow yourself"),
    ALREADY_FOLLOWING(1006, "Already following this user"),
    USER_NOT_FOUND(1007, "User not found"),
    NOT_FOLLOWING(1008, "Not following this user"),
    ACCOUNT_LOCKED(1009, "Account has been locked, please try again later"),
    VERIFICATION_CODE_ERROR(1010, "Verification code error"),
    VERIFICATION_CODE_EXPIRED(1011, "Verification code has expired"),
    VERIFICATION_CODE_TOO_FREQUENT(1012, "Verification code sending is too frequent"),
    TAG_LIMIT_EXCEEDED(1013, "Up to 5 skill tags"),
    INVALID_OLD_PASSWORD(1014, "Invalid old password"),
    PHONE_NOT_BOUND(1015, "Phone number is not bound"),
    EMAIL_NOT_BOUND(1016, "Email is not bound"),
    ACCOUNT_CANCELLATION_APPLIED(1017, "Account cancellation has been applied"),
    CANCELLATION_NOT_APPLIED(1018, "Account cancellation has not been applied"),
    ALREADY_BOUND(1019, "This account has been bound to another user"),
    NO_BINDING(1020, "No binding record found"),

    // Content Service Error Codes (2000-2999)
    ARTICLE_NOT_FOUND(2001, "Article not found"),
    ARTICLE_UNDER_REVIEW(2002, "Article is under review"),
    NO_EDIT_PERMISSION(2003, "No permission to edit this article"),
    TAG_NOT_FOUND(2004, "Tag not found"),
    TAG_ALREADY_EXISTS(2005, "Tag name already exists"),
    CATEGORY_NOT_FOUND(2006, "Category not found"),
    CATEGORY_ALREADY_EXISTS(2007, "Category name already exists"),
    DRAFT_NOT_FOUND(2008, "Draft not found"),
    DRAFT_LIMIT_EXCEEDED(2009, "Up to 50 drafts"),
    ARTICLE_UNPUBLISHED(2010, "Article is not published"),
    ALREADY_PUBLISHED(2011, "Article is already published"),

    // Social Service Error Codes (3000-3999)
    ALREADY_LIKED(3001, "Already liked"),
    NOT_LIKED(3002, "Not liked yet"),
    COMMENT_NOT_FOUND(3003, "Comment not found"),
    ALREADY_FAVORITED(3004, "Already favorited"),
    NOT_FAVORITED(3005, "Not favorited yet"),
    FOLDER_NOT_FOUND(3006, "Favorite folder not found"),
    FOLDER_ALREADY_EXISTS(3007, "Folder name already exists"),
    FOLDER_LIMIT_EXCEEDED(3008, "Up to 20 favorite folders"),
    CANNOT_DELETE_DEFAULT_FOLDER(3009, "Cannot delete the default folder"),
    NO_DELETE_PERMISSION(3010, "No permission to delete"),
    NOT_SUPPORTED(3011, "Operation not supported"),

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
