package com.chb.learning.common.auth.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.chb.learning.common.auth.entity.JwtToken;
import com.chb.learning.common.entity.vo.PermissionVo;
import com.chb.learning.common.entity.vo.RoleVo;
import com.chb.learning.common.entity.vo.UserVo;
import com.chb.learning.common.feign.AdminFeign;
import com.chb.learning.common.utils.JwtUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author caihongbin
 * @date 2021/5/29 18:49
 */
public class AdminRealm extends AuthorizingRealm {

    @Autowired
    private AdminFeign adminFeign;

    /**
     * 限定这个 Realm 只处理我们自定义的 JwtToken
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && JwtToken.class.isAssignableFrom(token.getClass());
    }

    /**
     * 此处的 SimpleAuthenticationInfo 可返回任意值，密码校验时不会用到它
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken)
            throws AuthenticationException {
        JwtToken jwtToken = (JwtToken) authcToken;

        String token = jwtToken.getToken();
        // 从 JwtToken 中获取当前用户
        String username = jwtToken.getPrincipal().toString();
        if (username == null) {
            throw new AccountException("JWT token参数异常！");
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(JwtUtils.SECRET);
            JWTVerifier verifier = JWT.require(algorithm).withClaim("username", username).build();
            verifier.verify(token);
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("The Token has expired");
        } catch (TokenExpiredException e) {
            throw new AuthenticationException("The Token has expired");
        }

        // 查询数据库获取用户信息，此处使用 Map 来模拟数据库
        UserVo user = adminFeign.getUserByName(username).getData();

        // 用户不存在
        if (user == null) {
            throw new UnknownAccountException("用户不存在！");
        }

        // 用户被锁定
        if (user.getLocked()) {
            throw new LockedAccountException("该用户已被锁定,暂时无法登录！");
        }

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, user.getPassword(), getName());
        return info;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 获取当前用户
        UserVo currentUser = (UserVo) SecurityUtils.getSubject().getPrincipal();
        // UserEntity currentUser = (UserEntity) principals.getPrimaryPrincipal();
        // 查询数据库，获取用户的角色信息
        Set<RoleVo> roleSet = currentUser.getRoles();
        Set<String> roles = roleSet.stream().map(RoleVo::getName).collect(Collectors.toSet());
        // 查询数据库，获取用户的权限信息
        Set<PermissionVo> permissionSet = new HashSet<>();
        roleSet.stream().map(RoleVo::getPermissions).forEach(p -> permissionSet.addAll(p));
        Set<String> perms = permissionSet.stream().map(PermissionVo::getName).collect(Collectors.toSet());
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setRoles(roles);
        info.setStringPermissions(perms);
        return info;
    }

}
