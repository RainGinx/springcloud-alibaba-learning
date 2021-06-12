package com.chb.learning.common.feign;

import com.chb.learning.common.entity.vo.BaseResponse;
import com.chb.learning.common.entity.vo.UserVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "service-admin")
public interface AdminFeign {

    @GetMapping("/user/getUserByName")
    BaseResponse<UserVo> getUserByName(@RequestParam String username);
}
