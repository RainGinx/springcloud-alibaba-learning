package com.chb.learning.admin.controller;

import com.chb.learning.admin.service.UserService;
import com.chb.learning.common.entity.vo.BaseResponse;
import com.chb.learning.common.entity.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author caihongbin
 * @date 2021/6/12 17:37
 */
@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("getUserByName")
    public BaseResponse<UserVo> getUserByName(@RequestParam String username){
        return new BaseResponse<>(userService.getUserByName(username));
    }
}
