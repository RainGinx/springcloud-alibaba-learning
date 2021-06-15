package com.chb.learning.admin.controller;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.chb.learning.common.auth.entity.JwtToken;
import com.chb.learning.common.entity.po.User;
import com.chb.learning.common.entity.vo.BaseResponse;
import com.chb.learning.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author caihongbin
 * @date 2021/5/31 16:24
 */
@RestController
@RequestMapping("")
@Slf4j
public class LoginController {

    @PostMapping("/login")
    public BaseResponse<Object> login(@RequestBody User user, HttpServletResponse response) {
        BaseResponse<Object> ret = new BaseResponse<>();
        if (StringUtils.isEmpty(user.getName()) || StringUtils.isEmpty(user.getPassword())) {

            ret.setData("请输入用户名和密码！");
            return ret;
        }
        //用户认证信息
        // 若登录成功，签发 JWT token
        String jwtToken = JwtUtils.sign(user.getName(), user.getPassword(), JwtUtils.SECRET);
        Subject subject = SecurityUtils.getSubject();
        JwtToken token = new JwtToken(user.getName(), user.getPassword());
        token.setToken(jwtToken);
        boolean loginSuccess = false;
        String msg;
        try {
            subject.login(token);
            msg = "登录成功。";
            loginSuccess = true;
        } catch (UnknownAccountException uae) { // 账号不存在
            msg = "用户名与密码不匹配，请检查后重新输入！";
        } catch (IncorrectCredentialsException ice) { // 账号与密码不匹配
            msg = "用户名与密码不匹配，请检查后重新输入！";
        } catch (LockedAccountException lae) { // 账号已被锁定
            msg = "该账户已被锁定，如需解锁请联系管理员！";
        } catch (AuthenticationException ae) { // 其他身份验证异常
            msg = "登录异常，请联系管理员！";
        }catch (TokenExpiredException e){
            msg ="登录过期，请重新登录";
        }

        if (loginSuccess) {
            // 将签发的 JWT token 设置到 HttpServletResponse 的 Header 中
            response.setHeader(JwtUtils.AUTH_HEADER, jwtToken);
            //
            ret.setErrCode(0);
            ret.setMsg(msg);
            ret.setData(jwtToken);
            return ret;
        } else {
            ret.setErrCode(401);
            ret.setMsg(msg);
            return ret;
        }
    }

    @RequiresRoles("admin")
    @GetMapping("/admin")
    public String admin() {
        return "admin success!";
    }

    @RequiresPermissions("query")
    @GetMapping("/index")
    public String index() {
        return "index success!";
    }

    @RequiresPermissions("add")
    @GetMapping("/add")
    public String add() {
        return "add success!";
    }

    @GetMapping("error")
    @ResponseBody
    public BaseResponse<String> error(HttpServletRequest request){
        Map<String, String[]> error = request.getParameterMap();
        return new BaseResponse<>(Integer.valueOf(error.get("status")[0]),error.get("message")[0],null);
    }
}
