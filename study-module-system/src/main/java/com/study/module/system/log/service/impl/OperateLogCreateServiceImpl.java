package com.study.module.system.log.service.impl;

import cn.hutool.core.text.StrFormatter;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.log.config.LogTextConfig;
import com.study.module.system.log.config.OperateLogConfig;
import com.study.module.system.log.entity.OperateLog;
import com.study.module.system.log.mapper.OperateLogMapper;
import com.study.module.system.log.service.OperateLogCreateService;
import com.study.module.system.user.service.UserService;
import com.study.api.dto.response.UserData;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 操作日志服务
 */
@Service
public class OperateLogCreateServiceImpl extends ServiceImpl<OperateLogMapper, OperateLog> implements OperateLogCreateService {

    @Autowired
    OperateLogConfig operateLogConfig;

    @Autowired
    UserService userService;

    /**
     * 创建操作日志
     */
    @Override
    public void createOperateLog(String operateType, Long operateId, String ip, Object... params) {
        Map<String, LogTextConfig> operateTypeMapAll = operateLogConfig.getOperateType();
        LogTextConfig logTextConfig = operateTypeMapAll.get(operateType);
        if (logTextConfig == null) {
            throw new LogicException(ErrorCodeConstants.OPERATE_LOG_TYPE_NOT_EXIST);
        }
        OperateLog operateLog = new OperateLog();
        operateLog.setOperateType(operateType);
        operateLog.setOperateTypeText(logTextConfig.getTypeText());
        operateLog.setOperateModuleText(logTextConfig.getModuleText());
        operateLog.setOperateContent(StrFormatter.format(logTextConfig.getContent(), params));
        // 补充创建人信息
        UserData userData = userService.checkUserByProvider(operateId);
        operateLog.setOperateId(operateId);
        operateLog.setOperateName(userData.getRealName());
        operateLog.setIpAddress(ip);
        operateLog.setCreateTime(LocalDateTime.now());
        if (!this.save(operateLog)) {
            throw new LogicException(ErrorCodeConstants.CREATE_OPERATE_LOG_FAIL);
        }
    }
}
