package com.juejin.common.constants;

/**
 * 通用常量
 *
 * @author juejin
 */
public class CommonConstant {

    /**
     * 成功标记
     */
    public static final Integer SUCCESS = 200;

    /**
     * 失败标记
     */
    public static final Integer FAIL = 500;

    /**
     * 未授权
     */
    public static final Integer UNAUTHORIZED = 401;

    /**
     * 禁止访问
     */
    public static final Integer FORBIDDEN = 403;

    /**
     * 默认分页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大分页大小
     */
    public static final Integer MAX_PAGE_SIZE = 100;

    /**
     * Token请求头
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 用户ID请求头
     */
    public static final String USER_ID_HEADER = "X-User-Id";

}
