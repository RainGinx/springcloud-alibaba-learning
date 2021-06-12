package com.chb.learning.common.entity.po;

import lombok.Data;

/**
 * @author caihongbin
 * @date 2021/5/29 14:56
 */
@Data
public class User {

    private Long id;
    private String name;
    private Integer age;
    private String email;
    private String password;

}
