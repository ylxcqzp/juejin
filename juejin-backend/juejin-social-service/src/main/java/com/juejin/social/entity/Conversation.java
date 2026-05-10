package com.juejin.social.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 私信会话实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("conversation")
public class Conversation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 会话用户1（较小ID） */
    private Long user1Id;

    /** 会话用户2（较大ID） */
    private Long user2Id;

    /** 最后一条消息内容 */
    private String lastMessage;

    /** 最后消息时间 */
    private LocalDateTime lastMessageTime;

    /** 用户1未读数 */
    private Integer user1Unread;

    /** 用户2未读数 */
    private Integer user2Unread;

}
