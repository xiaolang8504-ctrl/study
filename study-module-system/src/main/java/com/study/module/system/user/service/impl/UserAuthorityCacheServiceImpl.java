package com.study.module.system.user.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.study.common.core.constants.RedisKey;
import com.study.module.system.user.dto.response.UserDetailResp;
import com.study.module.system.user.service.UserAuthorityCacheService;
import com.study.module.system.user.service.UserService;
import com.yunshang.budget.common.redis.RedisService;
import com.yunshang.budget.common.security.base.JwtUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户权限缓存服务实现。
 */
@Slf4j
@Service
public class UserAuthorityCacheServiceImpl implements UserAuthorityCacheService {

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private RedisService redisService;

    /**
     * 立即重建指定用户的权限缓存。
     */
    @Override
    public void refreshUserAuthorityCache(Long userId) {
        if (userId == null) {
            return;
        }
        UserDetailResp userDetail = userService.getUserDetailByUserId(userId);
        Set<String> authorities = new HashSet<>();
        authorities.addAll(userDetail.getRoles());
        authorities.addAll(userDetail.getResources());
        JwtUser jwtUser = new JwtUser(
                userDetail.getId().toString(),
                userDetail.getPhone(),
                authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()),
                userId.toString());
        if (!Boolean.TRUE.equals(redisService.set(
                RedisKey.SYSTEM_USER_TOKEN + ":" + userId, JSONObject.toJSONString(jwtUser)))) {
            throw new IllegalStateException("更新用户权限缓存失败，userId=" + userId);
        }
    }

    /**
     * 在当前事务成功提交后重建指定用户的权限缓存。
     */
    @Override
    public void refreshUserAuthorityCachesAfterCommit(Collection<Long> userIds) {
        Set<Long> distinctUserIds = distinctUserIds(userIds);
        if (distinctUserIds.isEmpty()) {
            return;
        }
        Runnable refreshTask = () -> distinctUserIds.forEach(this::refreshUserAuthorityCacheSafely);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    refreshTask.run();
                }
            });
            return;
        }
        refreshTask.run();
    }

    /**
     * 事务提交后删除访问及刷新令牌，强制用户重新登录。
     */
    @Override
    public void invalidateUserSessionsAfterCommit(Collection<Long> userIds) {
        Set<Long> distinctUserIds = distinctUserIds(userIds);
        if (distinctUserIds.isEmpty()) {
            return;
        }
        Runnable invalidateTask = () -> distinctUserIds.forEach(this::invalidateUserSessionSafely);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    invalidateTask.run();
                }
            });
            return;
        }
        invalidateTask.run();
    }

    /**
     * 清洗用户编号集合，避免空集合和空元素干扰事务回调。
     */
    private Set<Long> distinctUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashSet<>();
        }
        return userIds.stream().filter(item -> item != null)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * 单个权限缓存刷新失败时记录日志，避免影响其他受影响账号的刷新。
     */
    private void refreshUserAuthorityCacheSafely(Long userId) {
        try {
            if (!Boolean.TRUE.equals(redisService.hasKey(RedisKey.SYSTEM_USER_TOKEN + ":" + userId))) {
                return;
            }
            refreshUserAuthorityCache(userId);
        } catch (Exception e) {
            log.error("刷新用户权限缓存失败，userId={}", userId, e);
        }
    }

    /**
     * 会话撤销失败仅记录告警，避免一个账号的 Redis 故障阻断其他账号撤销。
     */
    private void invalidateUserSessionSafely(Long userId) {
        try {
            redisService.del(RedisKey.SYSTEM_USER_TOKEN + ":" + userId,
                    RedisKey.SYSTEM_USER_REFRESH_TOKEN + ":" + userId);
        } catch (Exception e) {
            log.error("撤销用户在线会话失败，userId={}", userId, e);
        }
    }
}
