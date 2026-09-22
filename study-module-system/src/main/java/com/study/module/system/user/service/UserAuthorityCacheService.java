package com.study.module.system.user.service;

import java.util.Collection;

/**
 * 用户权限缓存服务。
 */
public interface UserAuthorityCacheService {

    /**
     * 立即重建指定用户的权限缓存。
     */
    void refreshUserAuthorityCache(Long userId);

    /**
     * 在当前事务成功提交后重建指定用户的权限缓存。
     */
    void refreshUserAuthorityCachesAfterCommit(Collection<Long> userIds);

    /**
     * 在当前事务成功提交后撤销指定用户的在线会话。
     *
     * <p>适用于禁用、删除和管理员重置账号资料等场景。撤销访问令牌缓存和刷新令牌后，
     * 旧 JWT 即使尚未过期也不能再通过 Redis 在线会话校验。</p>
     */
    void invalidateUserSessionsAfterCommit(Collection<Long> userIds);
}
