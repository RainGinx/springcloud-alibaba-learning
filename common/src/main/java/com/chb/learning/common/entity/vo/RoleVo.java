package com.chb.learning.common.entity.vo;

import lombok.Data;

import java.util.Set;

/**
 * @author caihongbin
 * @date 2021/6/2 14:12
 */
@Data
public class RoleVo {

    private Long id;
    private String name;

    private Set<PermissionVo> permissions;
}
