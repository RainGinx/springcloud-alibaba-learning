package com.chb.learning.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chb.learning.common.entity.po.User;
import com.chb.learning.common.entity.vo.UserVo;

/**
 * @author caihongbin
 * @date 2021/6/2 10:05
 */
public interface UserService extends IService<User> {

    UserVo getUserByName(String name);
}
