package com.study.module.system.review.service;

import com.study.module.system.review.dto.response.LearningPathResp;

public interface LearningPathService {
    LearningPathResp learningPath(String subject);
}
