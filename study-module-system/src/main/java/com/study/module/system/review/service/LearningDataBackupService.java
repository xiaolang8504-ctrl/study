package com.study.module.system.review.service;

import com.study.module.system.review.dto.response.LearningDataBackupResp;

/**
 * 学习数据备份服务。
 */
public interface LearningDataBackupService {

    /** 导出当前登录学生的个人学习数据。 */
    LearningDataBackupResp learningDataBackup();
}
