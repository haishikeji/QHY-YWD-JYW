package com.px.web.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户树
 *
 * @author 品讯科技
 * @date 2024-08
 */
@Data
public class UserTreeVO {
    private Long id;
    private String label;
    private Boolean disabled;
    private List<UserTreeVO> children;
}
