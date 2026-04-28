package com.juejin.common.feign;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息VO（Feign调用用）
 *
 * @author juejin
 */
@Data
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String nickname;
    private String avatar;
    private Integer level;
    private Integer role;
    private LocalDateTime createTime;

}
