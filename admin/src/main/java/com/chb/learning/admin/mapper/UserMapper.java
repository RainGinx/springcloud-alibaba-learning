package com.chb.learning.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chb.learning.common.entity.po.User;
import com.chb.learning.common.entity.vo.UserVo;
import org.apache.ibatis.annotations.Param;

public interface UserMapper extends BaseMapper<User> {

    UserVo getByName(@Param("name")String name);
}
