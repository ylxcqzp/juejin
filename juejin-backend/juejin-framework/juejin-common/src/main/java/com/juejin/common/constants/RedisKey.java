package com.juejin.common.constants;

/**
 * Redis Key常量
 *
 * @author juejin
 */
public class RedisKey {

    /**
     * 用户相关
     */
    public static final String USER_INFO = "user:info:";
    public static final String USER_TOKEN = "user:token:";
    public static final String USER_FOLLOWING = "user:following:";
    public static final String USER_FOLLOWERS = "user:followers:";

    /**
     * 文章相关
     */
    public static final String ARTICLE_DETAIL = "article:detail:";
    public static final String ARTICLE_VIEW_COUNT = "article:view:count:";
    public static final String ARTICLE_LIKE_COUNT = "article:like:count:";
    public static final String ARTICLE_HOT = "article:hot";

    /**
     * 点赞相关
     */
    public static final String LIKE_ARTICLE = "like:article:";
    public static final String LIKE_COMMENT = "like:comment:";

    /**
     * Feed流
     */
    public static final String FEED_FOLLOWING = "feed:following:";
    public static final String FEED_RECOMMEND = "feed:recommend:";

    /**
     * 签到
     */
    public static final String SIGN_RECORD = "sign:record:";
    public static final String SIGN_CONTINUOUS = "sign:continuous:";

    /**
     * 限流
     */
    public static final String RATE_LIMIT = "rate:limit:";

}
