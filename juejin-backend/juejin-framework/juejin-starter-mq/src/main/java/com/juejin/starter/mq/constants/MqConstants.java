package com.juejin.starter.mq.constants;

/**
 * MQ常量定义
 *
 * @author juejin
 */
public class MqConstants {

    /**
     * 交换机
     */
    public static class Exchange {
        public static final String DIRECT = "juejin.direct";
        public static final String TOPIC = "juejin.topic";
        public static final String FANOUT = "juejin.fanout";
    }

    /**
     * 队列
     */
    public static class Queue {
        public static final String USER_REGISTER = "user.register.queue";
        public static final String ARTICLE_PUBLISH = "article.publish.queue";
        public static final String LIKE_RECORD = "like.record.queue";
        public static final String COMMENT_NOTIFY = "comment.notify.queue";
    }

    /**
     * 路由键
     */
    public static class RoutingKey {
        public static final String USER_REGISTER = "user.register";
        public static final String ARTICLE_PUBLISH = "article.publish";
        public static final String LIKE_RECORD = "like.record";
        public static final String COMMENT_NOTIFY = "comment.notify";
    }

}
