package com.chb.learning.common.entity.vo;

import lombok.Data;

import java.util.Set;

/**
 * @author caihongbin
 * @date 2021/6/2 14:11
 */
@Data
public class UserVo {

    private Long id;
    private String name;
    private Integer age;
    private String email;
    private String password;
    private Boolean locked; // 账户是否被锁定


    private Set<RoleVo> roles;
}
