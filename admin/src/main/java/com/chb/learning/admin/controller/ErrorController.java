package com.chb.learning.admin.controller;

import com.chb.learning.common.entity.vo.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

/**
 * @author caihongbin
 * @date 2021/6/12 18:02
 */
@RestController
@RequestMapping("/error")
public class ErrorController {

    @GetMapping(path = "/unauthorized/{message}")
    public BaseResponse<String> unauthorized(@PathVariable String message) throws UnsupportedEncodingException {
        return new BaseResponse<>(401, "error: "+message,null);
    }

}