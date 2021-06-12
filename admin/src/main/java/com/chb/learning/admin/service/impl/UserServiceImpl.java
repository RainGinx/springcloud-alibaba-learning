package com.chb.learning.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chb.learning.admin.mapper.UserMapper;
import com.chb.learning.admin.service.UserService;
import com.chb.learning.common.entity.po.User;
import com.chb.learning.common.entity.vo.UserVo;
import org.springframework.stereotype.Service;

/**
 * @author caihongbin
 * @date 2021/6/12 17:42
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public UserVo getUserByName(String name) {
        return baseMapper.getByName(name);
    }
}
