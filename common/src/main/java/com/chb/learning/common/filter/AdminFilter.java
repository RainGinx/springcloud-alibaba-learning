package com.chb.learning.common.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.chb.learning.common.auth.entity.JwtToken;
import com.chb.learning.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

/**
 * @author caihongbin
 * @date 2021/6/3 14:25
 */
@Slf4j
public class AdminFilter extends BasicHttpAuthenticationFilter {

    /**
     * 前置处理
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        // 跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }

    /**
     * 后置处理
     */
    @Override
    protected void postHandle(ServletRequest request, ServletResponse response) {
        // 添加跨域支持
        this.fillCorsHeader(WebUtils.toHttp(request), WebUtils.toHttp(response));
    }

    /**
     * 过滤器拦截请求的入口方法
     * 返回 true 则允许访问
     * 返回false 则禁止访问，会进入 onAccessDenied()
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 原用来判断是否是登录请求，在本例中不会拦截登录请求，用来检测Header中是否包含 JWT token 字段
        if (this.isLoginRequest(request, response))
            return false;
        boolean allowed = false;
        try {
            // 检测Header里的 JWT token内容是否正确，尝试使用 token进行登录
            allowed = executeLogin(request, response);
        } catch (IllegalStateException e) { // not found any token
            log.error("Not found any token");
            throw new UnauthorizedException("Not found any token");
        }catch (Exception e) {
            log.error("Error occurs when login", e);
            throw new UnauthorizedException("Error occurs when login");
        }
        return allowed || super.isPermissive(mappedValue);
    }

    /**
     * 检测Header中是否包含 JWT token 字段
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        return ((HttpServletRequest) request).getHeader(JwtUtils.AUTH_HEADER) == null;
    }

    /**
     * 身份验证,检查 JWT token 是否合法
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        JwtToken jwtToken = (JwtToken)createToken(request, response);
        if (jwtToken == null) {
            String msg = "createToken method implementation returned null. A valid non-null AuthenticationToken "
                    + "must be created in order to execute a login attempt.";
            throw new IllegalStateException(msg);
        }
        try {
            String token = jwtToken.getToken();
            // 从 JwtToken 中获取当前用户
            String username = jwtToken.getPrincipal().toString();
            if (username == null) {
                throw new AccountException("JWT token参数异常！");
            }
            Algorithm algorithm = Algorithm.HMAC256(JwtUtils.SECRET);
            JWTVerifier verifier = JWT.require(algorithm).withClaim("username", username).build();
            verifier.verify(token);
            Subject subject = getSubject(request, response);
            subject.login(jwtToken); // 交给 Shiro 去进行登录验证
            return onLoginSuccess(jwtToken, subject, request, response);
        } catch (AuthenticationException e) {
            return onLoginFailure(jwtToken, e, request, response);
        }catch (TokenExpiredException e){
            //eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJwYXNzd29yZCI6IjEyMzQ1NiIsImV4cCI6MTYyMzM5MTQwMywidXNlcm5hbWUiOiJ0b20ifQ.3cQdvFFOaQnIuGj43242ltcmxOOQ5bjBSGcg8zokDoI
//            throw new UnauthorizedException("The Token has expired");
            responseError(response,401,"The Token has expired");
            return false;
        }
    }

    /**
     * 从 Header 里提取 JWT token
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String authorization = httpServletRequest.getHeader(JwtUtils.AUTH_HEADER);
        JwtToken token = new JwtToken(authorization);
        return token;
    }

    /**
     * isAccessAllowed()方法返回false，会进入该方法，表示拒绝访问
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
//        HttpServletResponse httpResponse = WebUtils.toHttp(servletResponse);
//        httpResponse.setCharacterEncoding("UTF-8");
//        httpResponse.setContentType("application/json;charset=UTF-8");
//        httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
//        PrintWriter writer = httpResponse.getWriter();
//        writer.write("{\"errCode\": 401, \"msg\": \"UNAUTHORIZED\"}");
//        fillCorsHeader(WebUtils.toHttp(servletRequest), httpResponse);
        return false;
    }

    /**
     * Shiro 利用 JWT token 登录成功，会进入该方法
     */
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request,
                                     ServletResponse response) throws Exception {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        String newToken = null;
        if (token instanceof JwtToken) {
            newToken = JwtUtils.refreshTokenExpired(((JwtToken) token).getToken(), JwtUtils.SECRET);
        }
        if (newToken != null)
            httpResponse.setHeader(JwtUtils.AUTH_HEADER, newToken);
        return true;
    }

    /**
     * Shiro 利用 JWT token 登录失败，会进入该方法
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request,
                                     ServletResponse response) {
        // 此处直接返回 false ，交给后面的  onAccessDenied()方法进行处理
        try {
            HttpServletResponse httpResponse = WebUtils.toHttp(response);
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            PrintWriter writer = httpResponse.getWriter();
            writer.write("{\"errCode\": 401, \"msg\": \""+e.getMessage()+"\"}");
            fillCorsHeader(WebUtils.toHttp(request), httpResponse);
        } catch (IOException ioException) {
            throw e;
        }
        return false;
    }

    /**
     * 添加跨域支持
     */
    protected void fillCorsHeader(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,HEAD");
        httpServletResponse.setHeader("Access-Control-Allow-Headers",
                httpServletRequest.getHeader("Access-Control-Request-Headers"));
    }

    @Override
    public void setLoginUrl(String loginUrl) {
        super.setLoginUrl(loginUrl);
    }

    /**
     * 将非法请求跳转到 /filterError/**中
     */
    private void responseError(ServletResponse response, int code,String message) {
        try {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            //设置编码，否则中文字符在重定向时会变为空字符串
            message = URLEncoder.encode(message, "UTF-8");
            //如果有项目名称路径记得加上
            httpServletResponse.sendRedirect("/error/unauthorized" + "/" + message);

        } catch (IOException e1) {
            log.error(e1.getMessage());
        }
    }
}
