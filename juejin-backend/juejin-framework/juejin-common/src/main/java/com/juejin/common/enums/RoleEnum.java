package com.juejin.common.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author juejin
 */
@Getter
public enum RoleEnum {

    /**
     * 普通用户
     */
    USER(0, "普通用户", "user"),

    /**
     * 认证作者
     */
    AUTHOR(1, "认证作者", "author"),

    /**
     * 管理员
     */
    ADMIN(2, "管理员", "admin");

    private final Integer code;
    private final String name;
    private final String role;

    RoleEnum(Integer code, String name, String role) {
        this.code = code;
        this.name = name;
        this.role = role;
    }

    /**
     * 根据code获取枚举
     */
    public static RoleEnum getByCode(Integer code) {
        for (RoleEnum roleEnum : values()) {
            if (roleEnum.getCode().equals(code)) {
                return roleEnum;
            }
        }
        return USER;
    }

    /**
     * 判断是否为管理员
     */
    public static boolean isAdmin(Integer code) {
        return ADMIN.getCode().equals(code);
    }

    /**
     * 判断是否为认证作者
     */
    public static boolean isAuthor(Integer code) {
        return AUTHOR.getCode().equals(code) || ADMIN.getCode().equals(code);
    }

}
