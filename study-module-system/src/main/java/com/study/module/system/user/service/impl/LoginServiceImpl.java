package com.study.module.system.user.service.impl;

import com.study.module.system.user.dto.request.LoginForm;
import com.study.module.system.user.dto.response.LoginResp;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.service.LoginService;
import com.study.module.system.user.service.UserAuthorityCacheService;
import com.study.module.system.user.service.UserService;
import com.study.module.system.user.service.VerifyService;
import com.study.api.contants.OperateType;
import com.study.common.core.constants.Enable;
import com.study.common.core.constants.RedisKey;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.common.core.utils.IpUtils;
import com.yunshang.budget.common.redis.RedisService;
import com.yunshang.budget.common.security.component.JwtUtils;
import com.yunshang.budget.common.security.config.JwtTokenConfig;
import com.yunshang.budget.common.security.domain.TokenInfo;
import com.study.module.system.log.service.OperateLogCreateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    UserService userService;

    @Autowired
    JwtTokenConfig jwtTokenConfig;

    @Autowired
    RedisService redisService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    OperateLogCreateService operateLogCreateService;

    @Autowired
    UserAuthorityCacheService userAuthorityCacheService;

    @Autowired
    VerifyService verifyService;

    /**
     * 用户登录
     */
    @Override
    public LoginResp login(LoginForm request) {
        // 消费已通过独立接口校验的滑块验证码
        verifyService.consumeVerifiedSliderCaptcha(request.getSliderCaptchaToken());
        //验证用户信息
        User user = isValidationUserName(request.getUserName());
        //验证密码
        isValidationPassWord(user.getPassWord(),request.getPassWord());
        // 仅在认证成功后记录最近登录时间，供家庭协作在有效绑定后展示。
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginTime(now);
        user.setUpdateTime(now);
        userService.updateById(user);
        //生成Token
        String token = buildToken(user);
        //设置refreshToken
        setRefreshToken(user);
        //账号权限信息写入redis和线程
        userAuthorityCacheService.refreshUserAuthorityCache(user.getId());
        //写入日志
        //operateLogCreateService.createOperateLog(OperateType.LOGIN, user.getId(), IpUtils.getIpAddr());
        //返回前端token
        LoginResp loginResp = new LoginResp();
        loginResp.setToken(jwtTokenConfig.getTokenHead() + token);
        return loginResp;
    }

    /**
     * 验证密码
     */
    private void isValidationPassWord(String userPassWord,String passWord) {
        if (!passwordEncoder.matches(passWord, userPassWord)) {
            throw new LogicException(ErrorCodeConstants.LOGIN_FAIL);
        }
    }

    /**
     * 生成Token
     */
    private String buildToken(User user) {
        return JwtUtils.generatorJwtToken(buildTokenInfo(user),
                jwtTokenConfig.getSecretKey(), jwtTokenConfig.getTokenExpire());
    }

    /**
     * 设置refreshToken
     */
    private void setRefreshToken(User user) {
        String refreshToken = jwtTokenConfig.getTokenHead() + JwtUtils.generatorJwtToken(buildTokenInfo(user),
                jwtTokenConfig.getSecretKey(), jwtTokenConfig.getRefreshTokenExpire());
        redisService.set(RedisKey.SYSTEM_USER_REFRESH_TOKEN + ":" + user.getId(), refreshToken);
    }

    /**
     * 构建token信息
     */
    private TokenInfo buildTokenInfo(User user) {
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setUserId(user.getId());
        tokenInfo.setUsername(user.getUserName());
        return tokenInfo;
    }

    /**
     * 退出登录用户
     */
    @Override
    public void outLogin(HttpServletRequest request) {
        Long userId = JwtUtils.getUserIdByToken(request,jwtTokenConfig.getTokenHeader(),jwtTokenConfig.getTokenHead(),jwtTokenConfig.getSecretKey());
        if (userId != null){
            //清除对应账号的redis信息
            redisService.del(RedisKey.SYSTEM_USER_TOKEN + ":" + userId);
            redisService.del(RedisKey.SYSTEM_USER_REFRESH_TOKEN + ":" + userId);
            //写入日志
            operateLogCreateService.createOperateLog(OperateType.LOGIN_OUT, userId, IpUtils.getIpAddr());
        }
    }

    /**
     * 验证账号
     */
    private User isValidationUserName(String userName) {
        User user = userService.checkUserByUserName(userName);
        if (user.getStatus().equals(Enable.DISABLE)){
            throw new LogicException(ErrorCodeConstants.USER_DISABLED);
        }
        return user;
    }

}
