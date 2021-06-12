package com.chb.learning.common.auth.entity;

import com.chb.learning.common.utils.JwtUtils;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * @author caihongbin
 * @date 2021/6/7 14:26
 */
public class JwtToken implements AuthenticationToken {


    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private boolean rememberMe;
    private String host;

    // 加密后的 JWT token串
    private String token;


    public JwtToken(String token) {
        this.token = token;
        this.username = JwtUtils.getClaimFiled(token, "username");
        this.password = JwtUtils.getClaimFiled(token, "password");
    }

    public JwtToken(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Object getPrincipal() {
        return this.username;
    }

    @Override
    public Object getCredentials() {
        return this.password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}