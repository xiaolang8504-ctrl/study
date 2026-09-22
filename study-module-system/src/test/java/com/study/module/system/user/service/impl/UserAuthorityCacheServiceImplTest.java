package com.study.module.system.user.service.impl;

import com.study.common.core.constants.RedisKey;
import com.yunshang.budget.common.redis.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.mockito.Mockito.verify;

/**
 * 账户状态变化必须撤销访问和刷新令牌，避免旧会话继续访问学生数据。
 */
@ExtendWith(MockitoExtension.class)
class UserAuthorityCacheServiceImplTest {

    @Mock
    private RedisService redisService;

    @InjectMocks
    private UserAuthorityCacheServiceImpl userAuthorityCacheService;

    @Test
    void shouldInvalidateBothTokensForEveryAffectedUser() {
        userAuthorityCacheService.invalidateUserSessionsAfterCommit(Arrays.asList(7L, 8L, 7L, null));

        verify(redisService).del(RedisKey.SYSTEM_USER_TOKEN + ":7", RedisKey.SYSTEM_USER_REFRESH_TOKEN + ":7");
        verify(redisService).del(RedisKey.SYSTEM_USER_TOKEN + ":8", RedisKey.SYSTEM_USER_REFRESH_TOKEN + ":8");
    }
}
