package com.juejin.content.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 标签VO
 *
 * @author juejin
 */
@Data
public class TagVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 标签ID */
    private Long id;

    /** 标签名称 */
    private String name;

    /** 图标 */
    private String icon;

}
